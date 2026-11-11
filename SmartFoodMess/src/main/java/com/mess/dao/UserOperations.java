package com.mess.dao;

import com.mess.model.User;

import java.util.List;

/**
 * Contract for user-related operations in the mess system:
 * validation / authentication and account management.
 *
 * <p>Implemented by {@link UserDAO}. Because the servlets code against
 * this fixed set of operations (design by contract), the concrete
 * data-access implementation can be changed, extended or tested in
 * isolation without touching the web layer.</p>
 */
public interface UserOperations {

    /**
     * Verifies credentials against the database.
     *
     * @return a fully populated {@link User} on success, or {@code null}
     *         when the credentials do not match
     */
    User validateUser(String email, String password);

    /**
     * @return {@code true} when an account with the given email already exists
     */
    boolean isEmailRegistered(String email);

    /**
     * @return {@code true} when the account was created successfully
     */
    boolean registerUser(User user);

    /**
     * @return all accounts with the STUDENT role
     */
    List<User> getAllStudents();

    /**
     * @return {@code true} when the student account was deleted
     */
    boolean deleteStudent(int userId);
}