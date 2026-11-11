package com.mess.dao;

import com.mess.model.Menu;
import com.mess.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the {@code menu} table.
 */
public class MenuDAO {

    private static volatile boolean priceColumnReady;

    private static final String ALL_MENUS_SQL =
            "SELECT menu_id, day_of_week, meal_type, items, price FROM menu " +
                    "ORDER BY FIELD(day_of_week, 'SUNDAY','MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY'), " +
                    "FIELD(meal_type, 'BREAKFAST','LUNCH','DINNER')";

    private static final String INSERT_MENU_SQL =
            "INSERT INTO menu (day_of_week, meal_type, items, price) VALUES (?, ?, ?, ?)";

    private static final String UPDATE_MENU_SQL =
            "UPDATE menu SET day_of_week = ?, meal_type = ?, items = ?, price = ? WHERE menu_id = ?";

    private static final String DELETE_MENU_SQL =
            "DELETE FROM menu WHERE menu_id = ?";

    private static final String EXISTS_SQL =
            "SELECT menu_id FROM menu WHERE day_of_week = ? AND meal_type = ?";

    /**
     * Returns every menu entry, ordered by day of week and then meal type.
     */
    public List<Menu> getAllMenus() {
        List<Menu> menus = new ArrayList<>();
        ensurePriceColumn();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(ALL_MENUS_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                menus.add(mapRow(resultSet));
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return menus;
    }

    /**
     * Inserts a new menu entry. Returns {@code false} if one already exists
     * for the same day and meal type (kept unique via the database constraint).
     */
    public boolean addMenu(Menu menu) {
        ensurePriceColumn();
        if (menuExists(menu.getDayOfWeek(), menu.getMealType())) {
            return false;
        }
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_MENU_SQL)) {

            preparedStatement.setString(1, menu.getDayOfWeek());
            preparedStatement.setString(2, menu.getMealType());
            preparedStatement.setString(3, menu.getItems());
            preparedStatement.setBigDecimal(4, menu.getPrice());
            return preparedStatement.executeUpdate() > 0;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Updates an existing menu entry.
     */
    public boolean updateMenu(Menu menu) {
        ensurePriceColumn();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_MENU_SQL)) {

            preparedStatement.setString(1, menu.getDayOfWeek());
            preparedStatement.setString(2, menu.getMealType());
            preparedStatement.setString(3, menu.getItems());
            preparedStatement.setBigDecimal(4, menu.getPrice());
            preparedStatement.setInt(5, menu.getMenuId());
            return preparedStatement.executeUpdate() > 0;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes a menu entry by id.
     */
    public boolean deleteMenu(int menuId) {
        ensurePriceColumn();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_MENU_SQL)) {

            preparedStatement.setInt(1, menuId);
            return preparedStatement.executeUpdate() > 0;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean menuExists(String dayOfWeek, String mealType) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(EXISTS_SQL)) {

            preparedStatement.setString(1, dayOfWeek);
            preparedStatement.setString(2, mealType);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void ensurePriceColumn() {
        if (priceColumnReady) {
            return;
        }
        synchronized (MenuDAO.class) {
            if (priceColumnReady) {
                return;
            }
            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement check = connection.prepareStatement("SHOW COLUMNS FROM menu LIKE 'price'");
                 ResultSet result = check.executeQuery()) {
                if (!result.next()) {
                    try (PreparedStatement alter = connection.prepareStatement(
                            "ALTER TABLE menu ADD COLUMN price DECIMAL(10,2) NOT NULL DEFAULT 0.00")) {
                        alter.executeUpdate();
                    }
                }
                priceColumnReady = true;
            } catch (ClassNotFoundException | SQLException e) {
                throw new IllegalStateException("Could not prepare the menu table for prices.", e);
            }
        }
    }

    private Menu mapRow(ResultSet resultSet) throws SQLException {
        return new Menu(
                resultSet.getInt("menu_id"),
                resultSet.getString("day_of_week"),
                resultSet.getString("meal_type"),
                resultSet.getString("items"),
                resultSet.getBigDecimal("price"));
    }
}