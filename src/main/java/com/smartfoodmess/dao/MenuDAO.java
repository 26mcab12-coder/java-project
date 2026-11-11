package com.smartfoodmess.dao;

import com.smartfoodmess.exception.DatabaseException;
import com.smartfoodmess.model.MenuItem;
import com.smartfoodmess.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC access for the {@code menu} table (daily food menu).
 */
public class MenuDAO {

    private static final String COLS = "menu_id, food_name, meal_type, price, available_date";

    /** Inserts a menu item and fills the generated id back in. */
    public MenuItem insert(MenuItem item) {
        final String sql = "INSERT INTO menu (food_name, meal_type, price, available_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, item.getFoodName());
            ps.setString(2, item.getMealType());
            ps.setDouble(3, item.getPrice());
            ps.setDate(4, Date.valueOf(item.getAvailableDate()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    item.setId(keys.getLong(1));
                }
            }
            return item;
        } catch (SQLException e) {
            if (isDuplicateKey(e)) {
                throw log(e, "This menu item already exists for the same meal type and date.");
            }
            throw log(e, "Could not save the menu item.");
        }
    }

    /** All menu items, newest date first. */
    public List<MenuItem> findAll() {
        final String sql = "SELECT " + COLS + " FROM menu ORDER BY available_date DESC, meal_id DESC";
        List<MenuItem> items = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(map(rs));
            }
            return items;
        } catch (SQLException e) {
            throw log(e, "Could not read the menu.");
        }
    }

    /** Finds one menu item by id, or returns {@code null}. */
    public MenuItem findById(long menuId) {
        final String sql = "SELECT " + COLS + " FROM menu WHERE menu_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, menuId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw log(e, "Could not read the menu item.");
        }
    }

    /** Updates an existing menu item. */
    public boolean update(MenuItem item) {
        final String sql = "UPDATE menu SET food_name = ?, meal_type = ?, price = ?, available_date = ? "
                + "WHERE menu_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getFoodName());
            ps.setString(2, item.getMealType());
            ps.setDouble(3, item.getPrice());
            ps.setDate(4, Date.valueOf(item.getAvailableDate()));
            ps.setLong(5, item.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (isDuplicateKey(e)) {
                throw log(e, "Another menu item with this name, meal type and date already exists.");
            }
            throw log(e, "Could not update the menu item.");
        }
    }

    /**
     * Deletes a menu item. Fails (with a friendly message) when meal or
     * booking records still reference it - that is the database's
     * referential protection doing its job.
     */
    public boolean delete(long menuId) {
        final String sql = "DELETE FROM menu WHERE menu_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, menuId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                throw log(e, "Cannot delete: existing meal or booking records use this menu item.");
            }
            throw log(e, "Could not delete the menu item.");
        }
    }

    /** Number of menu items (used by the admin dashboard). */
    public long count() {
        final String sql = "SELECT COUNT(*) FROM menu";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException e) {
            throw log(e, "Could not count menu items.");
        }
    }

    // ---- helpers --------------------------------------------------------

    private MenuItem map(ResultSet rs) throws SQLException {
        Date date = rs.getDate("available_date");
        LocalDate availableDate = (date != null) ? date.toLocalDate() : null;
        return new MenuItem(
                rs.getLong("menu_id"),
                rs.getString("food_name"),
                rs.getString("meal_type"),
                rs.getDouble("price"),
                availableDate);
    }

    private boolean isDuplicateKey(SQLException e) {
        return e instanceof SQLIntegrityConstraintViolationException || e.getErrorCode() == 1062;
    }

    private DatabaseException log(SQLException e, String friendlyMessage) {
        e.printStackTrace();
        return new DatabaseException(friendlyMessage, e);
    }
}
