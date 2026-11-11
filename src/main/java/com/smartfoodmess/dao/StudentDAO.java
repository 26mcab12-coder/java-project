package com.smartfoodmess.dao;

import com.smartfoodmess.exception.DatabaseException;
import com.smartfoodmess.model.Student;
import com.smartfoodmess.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC access for the {@code students} table.
 *
 * <p>The table stores {@code user_id}; the login username lives in the
 * {@code users} table, so read queries join both tables.</p>
 */
public class StudentDAO {

    /** Columns selected by every read query (students + username from users). */
    private static final String SELECT_COLS =
            "SELECT s.student_id, s.user_id, s.name, s.email, s.phone, u.username "
                    + "FROM students s JOIN users u ON u.id = s.user_id ";

    /** Inserts a profile linked to an existing user; fills generated id. */
    public Student insert(Student student) {
        final String sql = "INSERT INTO students (name, email, phone, user_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, student.getName());
            ps.setString(2, student.getEmail());
            ps.setString(3, student.getPhone());
            ps.setLong(4, student.getUserId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    student.setStudentId(keys.getLong(1));
                }
            }
            return student;
        } catch (SQLException e) {
            if (isDuplicateKey(e)) {
                throw log(e, "That email is already registered for another student.");
            }
            throw log(e, "Could not save the student profile.");
        }
    }

    /** All students, sorted by id. */
    public List<Student> findAll() {
        final String sql = SELECT_COLS + "ORDER BY s.student_id";
        List<Student> students = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                students.add(map(rs));
            }
            return students;
        } catch (SQLException e) {
            throw log(e, "Could not read the student list.");
        }
    }

    /** Finds a student by profile id, or returns {@code null}. */
    public Student findById(long studentId) {
        final String sql = SELECT_COLS + "WHERE s.student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw log(e, "Could not read the student profile.");
        }
    }

    /** Finds a student by login username, or returns {@code null}. */
    public Student findByUsername(String username) {
        final String sql = SELECT_COLS + "WHERE u.username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw log(e, "Could not read the student profile.");
        }
    }

    /** Finds a student by email (case-insensitive), or returns {@code null}. */
    public Student findByEmail(String email) {
        final String sql = SELECT_COLS + "WHERE LOWER(s.email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw log(e, "Could not check the email address.");
        }
    }

    /** Updates name / email / phone of an existing profile. */
    public boolean update(Student student) {
        final String sql = "UPDATE students SET name = ?, email = ?, phone = ? WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getName());
            ps.setString(2, student.getEmail());
            ps.setString(3, student.getPhone());
            ps.setLong(4, student.getStudentId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (isDuplicateKey(e)) {
                throw log(e, "That email is already used by another student.");
            }
            throw log(e, "Could not update the student profile.");
        }
    }

    /**
     * Deletes a student profile. Their meals/bookings disappear through the
     * {@code ON DELETE CASCADE} foreign keys, and the linked login account is
     * removed separately by {@link UserDAO#delete(long)}.
     */
    public boolean delete(long studentId) {
        final String sql = "DELETE FROM students WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw log(e, "Could not delete the student profile.");
        }
    }

    /** Number of students (used by the admin dashboard). */
    public long count() {
        final String sql = "SELECT COUNT(*) FROM students";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException e) {
            throw log(e, "Could not count students.");
        }
    }

    // ---- helpers --------------------------------------------------------

    private Student map(ResultSet rs) throws SQLException {
        return new Student(
                rs.getLong("student_id"),
                rs.getLong("user_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("username"));
    }

    private boolean isDuplicateKey(SQLException e) {
        return e instanceof SQLIntegrityConstraintViolationException || e.getErrorCode() == 1062;
    }

    private DatabaseException log(SQLException e, String friendlyMessage) {
        e.printStackTrace();
        return new DatabaseException(friendlyMessage, e);
    }
}
