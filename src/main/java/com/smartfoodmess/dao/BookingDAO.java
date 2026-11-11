package com.smartfoodmess.dao;

import com.smartfoodmess.exception.DatabaseException;
import com.smartfoodmess.model.Booking;
import com.smartfoodmess.model.BookingRecord;
import com.smartfoodmess.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC access for the {@code bookings} table.
 */
public class BookingDAO {

    private static final String RECORD_SELECT =
            "SELECT b.booking_id, b.student_id, s.name AS student_name, b.menu_id, mn.food_name, "
                    + "mn.meal_type, mn.available_date, b.booking_date, b.quantity, b.total_price, b.status "
                    + "FROM bookings b "
                    + "JOIN students s ON s.student_id = b.student_id "
                    + "JOIN menu mn ON mn.menu_id = b.menu_id ";

    private static final String RECORD_ORDER = "ORDER BY b.booking_date DESC, b.booking_id DESC";

    /** Inserts a new booking and fills the generated id back in. */
    public Booking insert(Booking booking) {
        final String sql = "INSERT INTO bookings (student_id, menu_id, booking_date, quantity, total_price, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, booking.getStudentId());
            ps.setLong(2, booking.getMenuId());
            ps.setDate(3, Date.valueOf(booking.getBookingDate()));
            ps.setInt(4, booking.getQuantity());
            ps.setDouble(5, booking.getTotalPrice());
            ps.setString(6, booking.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    booking.setBookingId(keys.getLong(1));
                }
            }
            return booking;
        } catch (SQLException e) {
            throw log(e, "Could not save the booking.");
        }
    }

    /**
     * Returns the booking id when this student already booked this exact
     * menu item, otherwise {@code null} (used to block double bookings).
     */
    public Long findIdByStudentAndMenu(long studentId, long menuId) {
        final String sql = "SELECT booking_id FROM bookings WHERE student_id = ? AND menu_id = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, studentId);
            ps.setLong(2, menuId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong("booking_id") : null;
            }
        } catch (SQLException e) {
            throw log(e, "Could not check existing bookings.");
        }
    }

    /** All bookings (admin view) with joined names. */
    public List<BookingRecord> findAllRecords() {
        final String sql = RECORD_SELECT + RECORD_ORDER;
        return queryRecords(sql, null);
    }

    /** Bookings of one student (student view). */
    public List<BookingRecord> findRecordsByStudent(long studentId) {
        final String sql = RECORD_SELECT + "WHERE b.student_id = ? " + RECORD_ORDER;
        return queryRecords(sql, studentId);
    }

    /** Updates the status of one booking (kept in sync with its meal). */
    public boolean updateStatus(long bookingId, String status) {
        final String sql = "UPDATE bookings SET status = ? WHERE booking_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw log(e, "Could not update the booking status.");
        }
    }

    /** Deletes one booking. Returns false when the id is unknown. */
    public boolean delete(long bookingId) {
        final String sql = "DELETE FROM bookings WHERE booking_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw log(e, "Could not delete the booking.");
        }
    }

    // ---- helpers --------------------------------------------------------

    /** Runs a RECORD_SELECT query, optionally filtering by student id. */
    private List<BookingRecord> queryRecords(String sql, Long studentId) {
        List<BookingRecord> records = new ArrayList<>();
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
            throw log(e, "Could not read bookings.");
        }
    }

    private BookingRecord mapRecord(ResultSet rs) throws SQLException {
        Date availableDate = rs.getDate("available_date");
        Date bookingDate = rs.getDate("booking_date");
        return new BookingRecord(
                rs.getLong("booking_id"),
                rs.getLong("student_id"),
                rs.getString("student_name"),
                rs.getLong("menu_id"),
                rs.getString("food_name"),
                rs.getString("meal_type"),
                (availableDate != null) ? availableDate.toLocalDate() : null,
                (bookingDate != null) ? bookingDate.toLocalDate() : null,
                rs.getInt("quantity"),
                rs.getDouble("total_price"),
                rs.getString("status"));
    }

    private DatabaseException log(SQLException e, String friendlyMessage) {
        e.printStackTrace();
        return new DatabaseException(friendlyMessage, e);
    }
}
