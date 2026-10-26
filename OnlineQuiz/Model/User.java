package com.quiz.model;

public class User {
    private final int id;
    private final String username, fullName, role;

    public User(int id, String username, String fullName, String role) {
        this.id = id; this.username = username; this.fullName = fullName; this.role = role;
    }
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public boolean isAdmin() { return "ADMIN".equals(role); }
}
