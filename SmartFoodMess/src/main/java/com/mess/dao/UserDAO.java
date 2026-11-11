package com.mess.dao;

import com.mess.model.User;
import com.mess.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the {@code users} table.
 * Handles credential validation, student registration, listing and deletion.
 * Implements the {@link UserOperations} contract used by the servlet layer.
 */
public class UserDAO implements UserOperations {

    private static final String VALIDATE_USER_SQL =
            "SELECT user_id, name, email, password, role, created_at "
                    + "FROM users WHERE email = ? AND password = ?";

    private static final String FIND_BY_EMAIL_SQL =
            "SELECT user_id, name, email, password, role, created_at FROM users WHERE email = ?";

    private static final String INSERT_USER_SQL =
            "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";

    private static final String ALL_STUDENTS_SQL =
            "SELECT user_id, name, email, password, role, created_at FROM users "
                    + "WHERE role = 'STUDENT' ORDER BY name";

    private static final String DELETE_STUDENT_SQL =
            "DELETE FROM users WHERE user_id = ? AND role = 'STUDENT'";

    private static final String UPDATE_NAME_SQL =
            "UPDATE users SET name = ? WHERE user_id = ?";

    /**
     * Verifies user credentials against the database.
     *
     * @return a fully populated {@link User} on success, or {@code null}
     *         when the credentials do not match or an error occurs
     */
    @Override
    public User validateUser(String email, String password) {
        User user = null;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(VALIDATE_USER_SQL)) {

            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    user = mapRow(resultSet, false);
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            throw new IllegalStateException("Unable to validate login against the database.", e);
        }

        return user;
    }

    /**
     * Returns {@code true} when an account with the given email already exists.
     */
    @Override
    public boolean isEmailRegistered(String email) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_EMAIL_SQL)) {

            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Creates a new user account (used for student self-registration).
     */
    @Override
    public boolean registerUser(User user) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_USER_SQL)) {

            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setString(3, user.getPassword());
            preparedStatement.setString(4, user.getRole() == null ? "STUDENT" : user.getRole());
            return preparedStatement.executeUpdate() > 0;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Returns all accounts with the STUDENT role.
     */
    @Override
    public List<User> getAllStudents() {
        List<User> students = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(ALL_STUDENTS_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                students.add(mapRow(resultSet, true));
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    /**
     * Deletes a student account (admins cannot be deleted through this path).
     */
    @Override
    public boolean deleteStudent(int userId) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_STUDENT_SQL)) {

            preparedStatement.setInt(1, userId);
            return preparedStatement.executeUpdate() > 0;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Updates the display name for the signed-in account.
     */
    public boolean updateName(int userId, String name) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_NAME_SQL)) {

            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, userId);
            return preparedStatement.executeUpdate() > 0;
        } catch (ClassNotFoundException | SQLException e) {
            throw new IllegalStateException("Unable to update the account name.", e);
        }
    }

    private User mapRow(ResultSet resultSet, boolean includePassword) throws SQLException {
        User user = new User();
        user.setUserId(resultSet.getInt("user_id"));
        user.setName(resultSet.getString("name"));
        user.setEmail(resultSet.getString("email"));
        user.setPassword(resultSet.getString("password"));
        user.setRole(resultSet.getString("role"));
        user.setCreatedAt(resultSet.getTimestamp("created_at"));
        return user;
    }
}