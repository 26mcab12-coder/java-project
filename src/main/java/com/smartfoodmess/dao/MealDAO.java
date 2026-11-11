package com.smartfoodmess.dao;

import com.smartfoodmess.exception.DatabaseException;
import com.smartfoodmess.model.Meal;
import com.smartfoodmess.model.MealRecord;
import com.smartfoodmess.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC access for the {@code meals} table.
 *
 * <p>Read queries join {@code students}, {@code menu} and (optionally)
 * {@code bookings} so the web pages get ready-to-display
 * {@link MealRecord} objects.</p>
 */
public class MealDAO {

    private static final String RECORD_SELECT =
            "SELECT m.meal_id, m.student_id, s.name AS student_name, m.menu_id, mn.food_name, "
                    + "m.meal_type, m.meal_date, m.quantity, m.status, "
                    + "COALESCE(b.total_price, 0) AS total_price "
                    + "FROM meals m "
                    + "JOIN students s ON s.student_id = m.student_id "
                    + "JOIN menu mn ON mn.menu_id = m.menu_id "
                    + "LEFT JOIN bookings b ON b.booking_id = m.booking_id ";

    private static final String RECORD_ORDER = "ORDER BY m.meal_date DESC, m.meal_id DESC";

    /** Inserts a new meal record and fills the generated id back in. */
    public Meal insert(Meal meal) {
        final String sql = "INSERT INTO meals (student_id, menu_id, booking_id, meal_date, meal_type, quantity, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, meal.getStudentId());
            ps.setLong(2, meal.getMenuId());
            if (meal.getBookingId() > 0) {
                ps.setLong(3, meal.getBookingId());
            } else {
                ps.setNull(3, Types.BIGINT); // not linked to a booking
            }
            ps.setDate(4, Date.valueOf(meal.getMealDate()));
            ps.setString(5, meal.getMealType());
            ps.setInt(6, meal.getQuantity());
            ps.setString(7, meal.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    meal.setMealId(keys.getLong(1));
                }
            }
            return meal;
        } catch (SQLException e) {
            throw log(e, "Could not save the meal record.");
        }
    }

    /** All meal records (admin view), newest date first. */
    public List<MealRecord> findAllRecords() {
        final String sql = RECORD_SELECT + RECORD_ORDER;
        return queryRecords(sql, null);
    }

    /** Meal records of one student (student view). */
    public List<MealRecord> findRecordsByStudent(long studentId) {
        final String sql = RECORD_SELECT + "WHERE m.student_id = ? " + RECORD_ORDER;
        return queryRecords(sql, studentId);
    }

    /**
     * Returns the linked booking id of a meal, or {@code null} when the meal
     * does not exist or has no booking link.
     */
    public Long getBookingId(long mealId) {
        final String sql = "SELECT booking_id FROM meals WHERE meal_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, mealId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long value = rs.getLong("booking_id");
                    return rs.wasNull() ? null : value;
                }
                return null;
            }
        } catch (SQLException e) {
            throw log(e, "Could not read the meal record.");
        }
    }

    /** Updates the status of one meal. Returns false when the id is unknown. */
    public boolean updateStatus(long mealId, String status) {
        final String sql = "UPDATE meals SET status = ? WHERE meal_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, mealId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw log(e, "Could not update the meal record.");
        }
    }

    /** Deletes one meal record. Returns false when the id is unknown. */
    public boolean delete(long mealId) {
        final String sql = "DELETE FROM meals WHERE meal_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, mealId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw log(e, "Could not delete the meal record.");
        }
    }

    /** Number of meal records (used by the admin dashboard). */
    public long count() {
        final String sql = "SELECT COUNT(*) FROM meals";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException e) {
            throw log(e, "Could not count meal records.");
        }
    }

    // ---- helpers --------------------------------------------------------

    /** Runs a RECORD_SELECT query, optionally filtering by student id. */
    private List<MealRecord> queryRecords(String sql, Long studentId) {
        List<MealRecord> records = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (studentId != null) {
                ps.setLong(1, studentId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(mapRecord(rs));
                }
            }
            return records;
        } catch (SQLException e) {
            throw log(e, "Could not read meal records.");
        }
    }

    private MealRecord mapRecord(ResultSet rs) throws SQLException {
        Date mealDate = rs.getDate("meal_date");
        return new MealRecord(
                rs.getLong("meal_id"),
                rs.getLong("student_id"),
                rs.getString("student_name"),
                rs.getLong("menu_id"),
                rs.getString("food_name"),
                rs.getString("meal_type"),
                (mealDate != null) ? mealDate.toLocalDate() : null,
                rs.getInt("quantity"),
                rs.getString("status"),
                rs.getDouble("total_price"));
    }

    private DatabaseException log(SQLException e, String friendlyMessage) {
        e.printStackTrace();
        return new DatabaseException(friendlyMessage, e);
    }
}
