package com.library;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.library.cache.Cache;
import com.library.model.Book;
import com.library.model.User;
import com.library.util.DatabaseConnection;

public class Library {
    private static final Logger logger = LoggerFactory.getLogger(Library.class);
    private final Connection connection;
    
    // Cache for frequently accessed data (30 seconds TTL)
    private final Cache<String, User> userCache;
    private final Cache<String, Book> bookCache;
    private final Cache<String, List<Book>> searchCache;

    public Library() {
        this.connection = DatabaseConnection.getConnection();
        this.userCache = new Cache<>(30);  // 30 seconds cache
        this.bookCache = new Cache<>(30);
        this.searchCache = new Cache<>(30);
        initializeTables();
    }

    private void initializeTables() {
        Connection conn = null;
        try {
            conn = connection;
            Statement stmt = conn.createStatement();
            
            // Drop existing tables in reverse order of dependencies
            logger.info("Dropping existing tables if they exist...");
            
            // Use dynamic SQL to drop all foreign key constraints
            List<String> dropStatements = new ArrayList<>();
            ResultSet rs = stmt.executeQuery(
                "SELECT " +
                "    'ALTER TABLE ' + OBJECT_SCHEMA_NAME(parent_object_id) + '.' + OBJECT_NAME(parent_object_id) + " +
                "    ' DROP CONSTRAINT ' + name AS DropStmt " +
                "FROM sys.foreign_keys"
            );
            
            while (rs.next()) {
                dropStatements.add(rs.getString("DropStmt"));
            }
            rs.close();
            
            // Execute all drop statements
            for (String dropStmt : dropStatements) {
                stmt.execute(dropStmt);
            }
            
            // Drop tables
            stmt.execute("DROP TABLE IF EXISTS loans;");
            stmt.execute("DROP TABLE IF EXISTS books;");
            stmt.execute("DROP TABLE IF EXISTS users;");
            
            // Create Users table first (no dependencies)
            logger.info("Creating users table...");
            stmt.execute(
                "CREATE TABLE users (" +
                "    id INT PRIMARY KEY IDENTITY(1,1)," +
                "    name VARCHAR(255) NOT NULL," +
                "    email VARCHAR(255) UNIQUE NOT NULL," +
                "    membership_status VARCHAR(50) NOT NULL" +
                ");"
            );
            
            stmt.execute(
                "CREATE INDEX idx_users_email ON users(email);"
            );

            // Create Books table (no dependencies)
            logger.info("Creating books table...");
            stmt.execute(
                "CREATE TABLE books (" +
                "    id INT PRIMARY KEY IDENTITY(1,1)," +
                "    title VARCHAR(255) NOT NULL," +
                "    author VARCHAR(255) NOT NULL," +
                "    copies_available INT NOT NULL," +
                "    isbn VARCHAR(13) UNIQUE NOT NULL" +
                ");"
            );
            
            stmt.execute(
                "CREATE INDEX idx_books_title_author ON books(title, author);"
            );

            // Create Loans table last (depends on both users and books)
            logger.info("Creating loans table...");
            stmt.execute(
                "CREATE TABLE loans (" +
                "    id INT PRIMARY KEY IDENTITY(1,1)," +
                "    user_id INT NOT NULL," +
                "    book_id INT NOT NULL," +
                "    loan_date DATETIME DEFAULT GETDATE()," +
                "    return_date DATETIME," +
                "    FOREIGN KEY (user_id) REFERENCES users(id)," +
                "    FOREIGN KEY (book_id) REFERENCES books(id)" +
                ");"
            );

            logger.info("Database tables initialized successfully");
        } catch (SQLException e) {
            logger.error("Error initializing database tables", e);
            throw new RuntimeException("Failed to initialize database tables", e);
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                logger.error("Error closing connection", e);
            }
        }
    }

    // Book operations
    public void addBook(Book book) {
        Connection conn = null;
        try {
            conn = connection;
            PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO books (title, author, copies_available, isbn) VALUES (?, ?, ?, ?)"
            );
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setInt(3, book.getCopiesAvailable());
            pstmt.setString(4, book.getIsbn());
            pstmt.executeUpdate();
            
            // Cache the new book
            bookCache.put(book.getIsbn(), book);
            // Invalidate search cache as results might change
            searchCache.invalidateAll();
            
            logger.info("Book added successfully: {}", book.getTitle());
        } catch (SQLException e) {
            logger.error("Error adding book", e);
            throw new RuntimeException("Failed to add book", e);
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                logger.error("Error closing connection", e);
            }
        }
    }

    public List<Book> searchBooks(String query) {
        // Try to get from cache first
        return searchCache.get(query.toLowerCase(), key -> {
            List<Book> books = new ArrayList<>();
            Connection conn = null;
            try {
                conn = connection;
                PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT * FROM books WHERE title LIKE ? OR author LIKE ?"
                );
                String searchPattern = "%" + key + "%";
                pstmt.setString(1, searchPattern);
                pstmt.setString(2, searchPattern);
                
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    Book book = new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("copies_available"),
                        rs.getString("isbn")
                    );
                    books.add(book);
                    // Cache individual books
                    bookCache.put(book.getIsbn(), book);
                }
            } catch (SQLException e) {
                logger.error("Error searching books", e);
                throw new RuntimeException("Failed to search books", e);
            } finally {
                try {
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    logger.error("Error closing connection", e);
                }
            }
            return books;
        });
    }

    // User operations
    public void addUser(User user) {
        Connection conn = null;
        try {
            conn = connection;
            PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO users (name, email, membership_status) VALUES (?, ?, ?)"
            );
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getMembershipStatus());
            pstmt.executeUpdate();
            
            // Cache the new user
            userCache.put(user.getEmail(), user);
            
            logger.info("User added successfully: {}", user.getName());
        } catch (SQLException e) {
            logger.error("Error adding user", e);
            throw new RuntimeException("Failed to add user", e);
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                logger.error("Error closing connection", e);
            }
        }
    }

    public Optional<User> findUserByEmail(String email) {
        // Try to get from cache first
        return Optional.ofNullable(userCache.get(email, key -> {
            Connection conn = null;
            try {
                conn = connection;
                PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT * FROM users WHERE email = ?"
                );
                pstmt.setString(1, key);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("membership_status")
                    );
                }
                return null;
            } catch (SQLException e) {
                logger.error("Error finding user", e);
                throw new RuntimeException("Failed to find user", e);
            } finally {
                try {
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    logger.error("Error closing connection", e);
                }
            }
        }));
    }

    // Loan operations
    public void borrowBook(int userId, int bookId) {
        Connection conn = null;
        try {
            conn = connection;
            conn.setAutoCommit(false);
            
            // Check if book is available
            PreparedStatement checkStmt = conn.prepareStatement(
                "SELECT * FROM books WHERE id = ?"
            );
            checkStmt.setInt(1, bookId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next() && rs.getInt("copies_available") > 0) {
                // Update book availability
                PreparedStatement updateStmt = conn.prepareStatement(
                    "UPDATE books SET copies_available = copies_available - 1 WHERE id = ?"
                );
                updateStmt.setInt(1, bookId);
                updateStmt.executeUpdate();
                
                // Create loan record
                PreparedStatement loanStmt = conn.prepareStatement(
                    "INSERT INTO loans (user_id, book_id) VALUES (?, ?)"
                );
                loanStmt.setInt(1, userId);
                loanStmt.setInt(2, bookId);
                loanStmt.executeUpdate();
                
                conn.commit();
                
                // After successfully borrowing a book, we need to update our caches:
                // 1. Create new Book object with decremented copies_available count
                // 2. Update the book cache using ISBN as key to reflect new availability
                // 3. Invalidate search cache since results would be stale
                Book book = new Book(
                    rs.getInt("id"),
                    rs.getString("title"), 
                    rs.getString("author"),
                    rs.getInt("copies_available") - 1, // Decrement available copies
                    rs.getString("isbn")
                );
                bookCache.put(book.getIsbn(), book); // Update book in cache by ISBN
                searchCache.invalidateAll(); // Clear search results since availability changed
                
                logger.info("Book borrowed successfully: User ID {}, Book ID {}", userId, bookId);
            } else {
                conn.rollback();
                throw new RuntimeException("Book not available for borrowing");
            }
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                logger.error("Error rolling back transaction", rollbackEx);
            }
            logger.error("Error borrowing book", e);
            throw new RuntimeException("Failed to borrow book", e);
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Error closing connection", e);
            }
        }
    }

    public void returnBook(int userId, int bookId) {
        Connection conn = null;
        try {
            conn = connection;
            conn.setAutoCommit(false);
            
            // Update loan record
            PreparedStatement updateLoanStmt = conn.prepareStatement(
                "UPDATE loans SET return_date = GETDATE() WHERE user_id = ? AND book_id = ? AND return_date IS NULL"
            );
            updateLoanStmt.setInt(1, userId);
            updateLoanStmt.setInt(2, bookId);
            int updatedRows = updateLoanStmt.executeUpdate();
            
            if (updatedRows > 0) {
                // Get book details
                PreparedStatement bookStmt = conn.prepareStatement(
                    "SELECT * FROM books WHERE id = ?"
                );
                bookStmt.setInt(1, bookId);
                ResultSet rs = bookStmt.executeQuery();
                
                if (rs.next()) {
                    // Update book availability
                    PreparedStatement updateBookStmt = conn.prepareStatement(
                        "UPDATE books SET copies_available = copies_available + 1 WHERE id = ?"
                    );
                    updateBookStmt.setInt(1, bookId);
                    updateBookStmt.executeUpdate();
                    
                    // Update cache
                    Book book = new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("copies_available") + 1,
                        rs.getString("isbn")
                    );
                    bookCache.put(book.getIsbn(), book);
                    searchCache.invalidateAll();
                }
                
                conn.commit();
                logger.info("Book returned successfully: User ID {}, Book ID {}", userId, bookId);
            } else {
                conn.rollback();
                throw new RuntimeException("No active loan found for this book and user");
            }
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                logger.error("Error rolling back transaction", rollbackEx);
            }
            logger.error("Error returning book", e);
            throw new RuntimeException("Failed to return book", e);
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Error closing connection", e);
            }
        }
    }
} 