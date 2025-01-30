# Library Management System

A Java-based library management system that uses MSSQL for data storage and implements efficient data retrieval patterns.

## Features

- Book management (add, search, track copies)
- User management (add, find by email)
- Loan tracking (borrow and return books)
- Efficient data retrieval with database indexing
- Transaction management for data consistency
- Logging for operations tracking

## Prerequisites

- Java 17 or higher
- Maven
- Microsoft SQL Server
- MSSQL JDBC Driver

## Database Setup

1. Create a new database named `library` in your MSSQL Server
2. The application will automatically create the necessary tables on startup

## Configuration

The database connection settings are in `DatabaseConnection.java`:
- Server: localhost
- Authentication: MSSQL
- Username: AxR
- Password: 2025makeris

## Building the Project

```bash
mvn clean install
```

## Running the Application

```bash
mvn exec:java -Dexec.mainClass="com.library.Main"
```

## Project Structure

```
src/main/java/com/library/
├── model/
│   ├── Book.java
│   └── User.java
├── util/
│   └── DatabaseConnection.java
├── Library.java
└── Main.java
```

## Database Schema

### Books Table
- id (INT, Primary Key)
- title (VARCHAR)
- author (VARCHAR)
- copies_available (INT)
- isbn (VARCHAR, Unique)

### Users Table
- id (INT, Primary Key)
- name (VARCHAR)
- email (VARCHAR, Unique)
- membership_status (VARCHAR)

### Loans Table
- id (INT, Primary Key)
- user_id (INT, Foreign Key)
- book_id (INT, Foreign Key)
- loan_date (DATETIME)
- return_date (DATETIME)

## Performance Optimizations

1. Database Indexing
   - Indexed book title and author for faster searches
   - Indexed user email for quick lookups

2. Connection Management
   - Single connection instance for better resource utilization
   - Proper connection closing in finally blocks

3. Transaction Management
   - ACID compliance for loan operations
   - Proper rollback handling

## Error Handling

- Comprehensive exception handling
- Logging with SLF4J and Logback
- Transaction rollback on failures 