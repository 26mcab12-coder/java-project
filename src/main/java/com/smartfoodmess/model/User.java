package com.smartfoodmess.model;

import com.smartfoodmess.exception.InvalidInputException;

/**
 * Represents a login account of the system.
 *
 * <p>Every account has a unique username, a password and a role.
 * A user is either a {@code STUDENT} or an {@code ADMIN}.</p>
 *
 * <p>Note: the password kept in this class is the <b>stored</b> value
 * (already hashed by {@link com.smartfoodmess.util.PasswordUtil}).
 * Raw passwords are only used for a few seconds during registration/login.</p>
 */
public class User {

    // ---- role constants -------------------------------------------------
    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_ADMIN = "ADMIN";

    /** Minimum length accepted for a new password. */
    public static final int MIN_PASSWORD_LENGTH = 6;

    // ---- fields (encapsulation: private + getters/setters) -------------
    private long id;
    private String username;
    private String password;
    private String role;

    /** No-argument constructor (useful for frameworks / result mapping). */
    public User() {
    }

    /** Creates a new user that has not been saved yet (id = 0). */
    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    /** Creates a user object for an existing database record. */
    public User(long id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // ---- validation -----------------------------------------------------

    /**
     * Validates this user.
     *
     * @throws InvalidInputException if any field is invalid; the message is
     *         user friendly and can be shown on the web page.
     */
    public void validate() throws InvalidInputException {
        if (username == null || username.isBlank()) {
            throw new InvalidInputException("Username must not be empty.");
        }
        username = username.trim();
        if (username.length() > 50) {
            throw new InvalidInputException("Username must be 50 characters or less.");
        }
        if (username.contains(" ")) {
            throw new InvalidInputException("Username must not contain spaces.");
        }

        if (password == null || password.isBlank()) {
            throw new InvalidInputException("Password must not be empty.");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidInputException(
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long.");
        }

        if (!ROLE_STUDENT.equals(role) && !ROLE_ADMIN.equals(role)) {
            throw new InvalidInputException("Role must be STUDENT or ADMIN.");
        }
    }

    /** Returns true when this account belongs to an administrator. */
    public boolean isAdmin() {
        return ROLE_ADMIN.equals(role);
    }

    /** Returns true when this account belongs to a student. */
    public boolean isStudent() {
        return ROLE_STUDENT.equals(role);
    }

    // ---- getters / setters ---------------------------------------------

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    /** Password is never printed - it is sensitive data. */
    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', role='" + role + "'}";
    }
}
