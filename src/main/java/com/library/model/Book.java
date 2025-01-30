package com.library.model;

public class Book {
    private int id;
    private String title;
    private String author;
    private int copiesAvailable;
    private String isbn;

    public Book(int id, String title, String author, int copiesAvailable, String isbn) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.copiesAvailable = copiesAvailable;
        this.isbn = isbn;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public int getCopiesAvailable() { return copiesAvailable; }
    public void setCopiesAvailable(int copiesAvailable) { this.copiesAvailable = copiesAvailable; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", copiesAvailable=" + copiesAvailable +
                ", isbn='" + isbn + '\'' +
                '}';
    }
} 