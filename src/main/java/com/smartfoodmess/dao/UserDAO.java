package com.smartfoodmess.dao;

import com.smartfoodmess.exception.DatabaseException;
import com.smartfoodmess.model.User;
import com.smartfoodmess.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;

/**
 * JDBC access for the {@code users} table.
 *
 * <p>All queries use {@link PreparedStatement} - user input is NEVER
 * concatenated into SQL, so the application is safe from SQL injection.</p>
 */
public class UserDAO {

    /** Inserts a new account and fills the generated id back into the object. */
    public User insert(User user) {
        final String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getLong(1));
                }
            }
            return user;
        } catch (SQLException e) {
            if (isDuplicateKey(e)) {
                throw log(e, "That username is already registered. Please choose another one.");
            }
            throw log(e, "Could not save the user account.");
        }
    }

    /** Finds an account by username, or returns {@code null}. */
    public User findByUsername(String username) {
        final String sql = "SELECT id, username, password, role FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw log(e, "Could not read user accounts.");
        }
    }

    /** Finds an account by id, or returns {@code null}. */
    public User findById(long id) {
        final String sql = "SELECT id, username, password, role FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw log(e, "Could not read the user account.");
        }
    }

    /** Updates the stored password hash of an account. */
    public boolean updatePassword(long userId, String hashedPassword) {
        final String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw log(e, "Could not update the password.");
        }
    }

    /** Deletes an account (child rows are removed by FK rules / service). */
    public boolean delete(long userId) {
        final String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw log(e, "Could not delete the user account.");
        }
    }

    // ---- helpers --------------------------------------------------------

    private User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("role"));
    }

    /** MySQL error 1062 = duplicate unique key. */
    private boolean isDuplicateKey(SQLException e) {
        return e instanceof SQLIntegrityConstraintViolationException || e.getErrorCode() == 1062;
    }

    /** Prints the technical error for developers, returns a friendly one. */
    private DatabaseException log(SQLException e, String friendlyMessage) {
        e.printStackTrace();
        return new DatabaseException(friendlyMessage, e);
    }
}
