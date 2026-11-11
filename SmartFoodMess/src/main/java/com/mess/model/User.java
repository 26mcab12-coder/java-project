package com.mess.model;

import java.sql.Timestamp;

/**
 * Represents a user account (ADMIN or STUDENT) in the system.
 * Mirrors the {@code users} table in the mess_db database.
 */
public class User {

    private int userId;
    private String name;
    private String email;
    private String password;
    private String role;
    private Timestamp createdAt;

    public User() {
    }

    public User(int userId, String name, String email, String password, String role, Timestamp createdAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.createdAt = createdAt;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{userId=" + userId + ", name='" + name + '\'' + ", email='" + email + '\''
                + ", role='" + role + '\'' + ", createdAt=" + createdAt + '}';
    }
}