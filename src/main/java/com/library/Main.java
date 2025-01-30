package com.library;

import com.library.model.Book;
import com.library.model.User;

public class Main {
    public static void main(String[] args) {
        Library library = new Library();

        // Add some sample books
        Book book1 = new Book(0, "The Great Gatsby", "F. Scott Fitzgerald", 5, "9780743273565");
        Book book2 = new Book(0, "To Kill a Mockingbird", "Harper Lee", 3, "9780446310789");
        
        library.addBook(book1);
        library.addBook(book2);

        // Add a user
        User user = new User(0, "John Doe", "john@example.com", "ACTIVE");
        library.addUser(user);

        // Search for books
        System.out.println("Searching for 'Gatsby':");
        library.searchBooks("Gatsby").forEach(System.out::println);

        // Find user
        System.out.println("\nFinding user by email:");
        library.findUserByEmail("john@example.com").ifPresent(System.out::println);
    }
} 