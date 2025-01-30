package com.library.model;

public class User {
    private int id;
    private String name;
    private String email;
    private String membershipStatus;

    public User(int id, String name, String email, String membershipStatus) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.membershipStatus = membershipStatus;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMembershipStatus() { return membershipStatus; }
    public void setMembershipStatus(String membershipStatus) { this.membershipStatus = membershipStatus; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", membershipStatus='" + membershipStatus + '\'' +
                '}';
    }
} 