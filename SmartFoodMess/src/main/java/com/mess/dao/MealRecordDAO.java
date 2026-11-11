package com.mess.dao;

import com.mess.model.MealRecord;
import com.mess.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for the {@code meal_records} table.
 */
public class MealRecordDAO {

    private static volatile boolean expenseColumnReady;

    private static final String GET_STATUS_SQL =
            "SELECT status FROM meal_records WHERE student_id = ? AND meal_date = ? AND meal_type = ?";

    private static final String RECORDS_BY_STUDENT_SQL =
            "SELECT r.record_id, r.student_id, r.meal_date, r.meal_type, r.status, r.expense, u.name AS student_name " +
                    "FROM meal_records r JOIN users u ON r.student_id = u.user_id " +
                    "WHERE r.student_id = ? " +
                    "ORDER BY r.meal_date DESC, FIELD(r.meal_type, 'BREAKFAST','LUNCH','DINNER')";

    private static final String ALL_RECORDS_SQL =
            "SELECT r.record_id, r.student_id, r.meal_date, r.meal_type, r.status, r.expense, u.name AS student_name " +
                    "FROM meal_records r JOIN users u ON r.student_id = u.user_id " +
                    "ORDER BY r.meal_date DESC, FIELD(r.meal_type, 'BREAKFAST','LUNCH','DINNER')";

    private static final String MENU_PRICE_SQL =
            "SELECT price FROM menu WHERE day_of_week = UPPER(DAYNAME(?)) AND meal_type = ?";

    private static final String MARK_CONSUMED_SQL =
            "UPDATE meal_records SET status = 'CONSUMED' WHERE record_id = ?";

    private static final String DELETE_RECORD_SQL =
            "DELETE FROM meal_records WHERE record_id = ?";

    private static final String TODAY_COUNTS_SQL =
            "SELECT status, COUNT(*) AS cnt FROM meal_records WHERE meal_date = ? GROUP BY status";

    private static final String TODAY_EXPENSES_BY_MEAL_SQL =
            "SELECT meal_type, SUM(expense) AS total, " +
                    "SUM(CASE WHEN expense IS NULL THEN 1 ELSE 0 END) AS missing_rates " +
                    "FROM meal_records WHERE meal_date = ? AND status <> 'OPTED_OUT' GROUP BY meal_type";

    /**
     * Returns the current status for a student/date/meal, or {@code null}
     * when no record exists yet.
     */
    public String getStatus(int studentId, LocalDate date, String mealType) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(GET_STATUS_SQL)) {

            preparedStatement.setInt(1, studentId);
            preparedStatement.setDate(2, Date.valueOf(date));
            preparedStatement.setString(3, mealType);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? resultSet.getString("status") : null;
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Inserts a new meal record, or updates the status when a record already
     * exists for the same student, date and meal type.
     */
    public boolean upsert(int studentId, LocalDate date, String mealType, String status) {
        ensureExpenseColumn();
        String sql = "INSERT INTO meal_records (student_id, meal_date, meal_type, status, expense) " +
                "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE status = ?, expense = ?";
        try (Connection connection = DBConnection.getConnection()) {
            BigDecimal expense = "OPTED_OUT".equals(status) ? BigDecimal.ZERO : getMenuPrice(connection, date, mealType);
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                preparedStatement.setInt(1, studentId);
                preparedStatement.setDate(2, Date.valueOf(date));
                preparedStatement.setString(3, mealType);
                preparedStatement.setString(4, status);
                setExpense(preparedStatement, 5, expense);
                preparedStatement.setString(6, status);
                setExpense(preparedStatement, 7, expense);
                return preparedStatement.executeUpdate() > 0;
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Returns the full meal history for one student (most recent first).
     */
    public List<MealRecord> getRecordsByStudent(int studentId) {
        ensureExpenseColumn();
        List<MealRecord> records = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(RECORDS_BY_STUDENT_SQL)) {

            preparedStatement.setInt(1, studentId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    MealRecord record = mapRow(resultSet);
                    record.setStudentName(resultSet.getString("student_name"));
                    records.add(record);
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    /**
     * Returns every meal record joined with the student name (admin view).
     */
    public List<MealRecord> getAllRecords() {
        ensureExpenseColumn();
        List<MealRecord> records = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(ALL_RECORDS_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                MealRecord record = mapRow(resultSet);
                record.setStudentName(resultSet.getString("student_name"));
                records.add(record);
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    /**
     * Marks a meal record as CONSUMED (admin: student has eaten the meal).
     */
    public boolean markConsumed(int recordId) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(MARK_CONSUMED_SQL)) {

            preparedStatement.setInt(1, recordId);
            return preparedStatement.executeUpdate() > 0;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes a meal record.
     */
    public boolean deleteRecord(int recordId) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_RECORD_SQL)) {

            preparedStatement.setInt(1, recordId);
            return preparedStatement.executeUpdate() > 0;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Returns daily bill/receipt details for a student.
     */
    public Map<String, Object> getDailyBill(int studentId, LocalDate date) {
        ensureExpenseColumn();
        String sql = "SELECT meal_date, meal_type, status, expense FROM meal_records " +
                "WHERE student_id = ? AND meal_date = ? AND status <> 'OPTED_OUT' " +
                "ORDER BY FIELD(meal_type, 'BREAKFAST','LUNCH','DINNER')";
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        Map<String, BigDecimal> byMeal = new LinkedHashMap<>();
        byMeal.put("BREAKFAST", BigDecimal.ZERO);
        byMeal.put("LUNCH", BigDecimal.ZERO);
        byMeal.put("DINNER", BigDecimal.ZERO);
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, studentId);
            preparedStatement.setDate(2, Date.valueOf(date));
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    Date mealDate = resultSet.getDate("meal_date");
                    item.put("mealDate", mealDate == null ? null : mealDate.toLocalDate());
                    String mealType = resultSet.getString("meal_type");
                    item.put("mealType", mealType);
                    item.put("status", resultSet.getString("status"));
                    BigDecimal expense = resultSet.getBigDecimal("expense");
                    if (expense == null) {
                        expense = BigDecimal.ZERO;
                    }
                    item.put("expense", expense);
                    total = total.add(expense);
                    if (mealType != null) {
                        byMeal.merge(mealType, expense, BigDecimal::add);
                    }
                    items.add(item);
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        result.put("items", items);
        result.put("total", total);
        result.put("byMeal", byMeal);
        result.put("date", date);
        result.put("count", items.size());
        return result;
    }
    public Map<String, Integer> getTodayCounts(LocalDate date) {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("OPTED_IN", 0);
        counts.put("OPTED_OUT", 0);
        counts.put("CONSUMED", 0);

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(TODAY_COUNTS_SQL)) {

            preparedStatement.setDate(1, Date.valueOf(date));
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    counts.put(resultSet.getString("status"), resultSet.getInt("cnt"));
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    /**
     * Returns today's charged expenses by meal type. A null value means at
     * least one opted-in or consumed record has no matching menu price.
     */
    public Map<String, BigDecimal> getTodayExpensesByMeal(LocalDate date) {
        Map<String, BigDecimal> expenses = new HashMap<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(TODAY_EXPENSES_BY_MEAL_SQL)) {

            preparedStatement.setDate(1, Date.valueOf(date));
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String mealType = resultSet.getString("meal_type");
                    BigDecimal total = resultSet.getBigDecimal("total");
                    int missingRates = resultSet.getInt("missing_rates");
                    expenses.put(mealType, missingRates > 0 ? null : (total == null ? BigDecimal.ZERO : total));
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            throw new IllegalStateException("Could not retrieve today's meal expenses.", e);
        }
        return expenses;
    }

    private MealRecord mapRow(ResultSet resultSet) throws SQLException {
        Date mealDate = resultSet.getDate("meal_date");
        MealRecord record = new MealRecord(
                resultSet.getInt("record_id"),
                resultSet.getInt("student_id"),
                mealDate == null ? null : mealDate.toLocalDate(),
                resultSet.getString("meal_type"),
                resultSet.getString("status"));
        record.setExpense(resultSet.getBigDecimal("expense"));
        return record;
    }

    private BigDecimal getMenuPrice(Connection connection, LocalDate date, String mealType) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(MENU_PRICE_SQL)) {
            preparedStatement.setDate(1, Date.valueOf(date));
            preparedStatement.setString(2, mealType);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? resultSet.getBigDecimal("price") : null;
            }
        }
    }

    private void setExpense(PreparedStatement preparedStatement, int index, BigDecimal expense) throws SQLException {
        if (expense == null) {
            preparedStatement.setNull(index, Types.DECIMAL);
        } else {
            preparedStatement.setBigDecimal(index, expense);
        }
    }

    private void ensureExpenseColumn() {
        if (!expenseColumnReady) {
            synchronized (MealRecordDAO.class) {
                if (!expenseColumnReady) {
                    ensureExpenseColumnExists();
                }
            }
        }
        backfillMissingExpenses();
    }

    private void ensureExpenseColumnExists() {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement check = connection.prepareStatement("SHOW COLUMNS FROM meal_records LIKE 'expense'");
             ResultSet result = check.executeQuery()) {
            if (!result.next()) {
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate("ALTER TABLE meal_records ADD COLUMN expense DECIMAL(10,2) NULL");
                }
            }
            expenseColumnReady = true;
        } catch (ClassNotFoundException | SQLException e) {
            throw new IllegalStateException("Could not prepare the meal_records table for expenses.", e);
        }
    }

    private void backfillMissingExpenses() {
        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "UPDATE meal_records r LEFT JOIN menu m " +
                    "ON m.day_of_week = UPPER(DAYNAME(r.meal_date)) AND m.meal_type = r.meal_type " +
                    "SET r.expense = CASE WHEN r.status = 'OPTED_OUT' THEN 0.00 ELSE m.price END " +
                    "WHERE r.expense IS NULL AND (r.status = 'OPTED_OUT' OR m.price IS NOT NULL)");
        } catch (ClassNotFoundException | SQLException e) {
            throw new IllegalStateException("Could not calculate missing meal expenses.", e);
        }
    }

    /**
     * Returns the total revenue (sum of expenses) for a given date, excluding OPTED_OUT meals.
     */
    public BigDecimal getRevenue(LocalDate date) {
        String sql = "SELECT COALESCE(SUM(expense),0) FROM meal_records WHERE meal_date = ? AND status <> 'OPTED_OUT'";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }
}