package com.smartfoodmess.model;

import com.smartfoodmess.exception.InvalidInputException;

/**
 * Represents a student of the mess.
 *
 * <p>A student profile is always linked to a {@link User} login account
 * through {@code userId}. The {@code username} is copied from that account
 * so the object can also be used before/without a join.</p>
 */
public class Student {

    private long studentId;
    private long userId;
    private String name;
    private String email;
    private String phone;
    private String username;

    public Student() {
    }

    public Student(long studentId, long userId, String name, String email,
                   String phone, String username) {
        this.studentId = studentId;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.username = username;
    }

    // ---- validation -----------------------------------------------------

    /**
     * Validates this student profile.
     *
     * @throws InvalidInputException with a user friendly message.
     */
    public void validate() throws InvalidInputException {
        if (name == null || name.isBlank()) {
            throw new InvalidInputException("Student name must not be empty.");
        }
        name = name.trim();
        if (name.length() > 100) {
            throw new InvalidInputException("Student name must be 100 characters or less.");
        }

        if (email == null || email.isBlank()) {
            throw new InvalidInputException("Email must not be empty.");
        }
        email = email.trim();
        if (!isValidEmail(email)) {
            throw new InvalidInputException("Please enter a valid email address (example: student@example.com).");
        }
        if (email.length() > 100) {
            throw new InvalidInputException("Email must be 100 characters or less.");
        }

        if (phone == null || phone.isBlank()) {
            throw new InvalidInputException("Phone number must not be empty.");
        }
        phone = phone.trim().replaceAll("[\\s\\-]", "");
        if (!isValidPhone(phone)) {
            throw new InvalidInputException("Phone number must contain 7 to 15 digits only.");
        }

        if (username == null || username.isBlank()) {
            throw new InvalidInputException("Username must not be empty.");
        }
        username = username.trim();
        if (username.length() > 50) {
            throw new InvalidInputException("Username must be 50 characters or less.");
        }

        if (studentId < 0 || userId < 0) {
            throw new InvalidInputException("Invalid student record id.");
        }
    }

    /** Basic email check: something@something.tld */
    public static boolean isValidEmail(String value) {
        if (value == null) {
            return false;
        }
        return value.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /** Phone must be 7-15 digits (spaces and dashes are ignored). */
    public static boolean isValidPhone(String value) {
        if (value == null) {
            return false;
        }
        return value.matches("^[0-9]{7,15}$");
    }

    // ---- getters / setters ---------------------------------------------

    public long getStudentId() {
        return studentId;
    }

    public void setStudentId(long studentId) {
        this.studentId = studentId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "Student{studentId=" + studentId + ", name='" + name + "', email='" + email
                + "', phone='" + phone + "', username='" + username + "'}";
    }
}
