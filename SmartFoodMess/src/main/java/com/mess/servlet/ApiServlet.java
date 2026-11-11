package com.mess.servlet;

import com.mess.dao.MealRecordDAO;
import com.mess.dao.MenuDAO;
import com.mess.dao.UserDAO;
import com.mess.model.MealRecord;
import com.mess.model.Menu;
import com.mess.model.User;
import com.mess.util.JsonUtil;
import com.mess.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Supplies session-protected JSON data to the static HTML application pages. */
@WebServlet("/api/*")
public class ApiServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String[] DAYS = {
            "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"
    };
    private static final String[] MEAL_TYPES = {"BREAKFAST", "LUNCH", "DINNER"};

    private final UserDAO userDAO = new UserDAO();
    private final MenuDAO menuDAO = new MenuDAO();
    private final MealRecordDAO mealRecordDAO = new MealRecordDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getPathInfo();
        String role = "/account".equals(path) ? null
                : path != null && path.startsWith("/student/") ? "STUDENT" : "ADMIN";
        if (!SessionUtil.requireLogin(request, response, role)) {
            return;
        }

        User user = SessionUtil.getCurrentUser(request);
        Object data;
        if ("/admin/dashboard".equals(path)) {
            data = adminDashboard(request, user);
        } else if ("/admin/menu".equals(path)) {
            data = withFlash(request, mapOf("menus", menuList()), user);
        } else if ("/admin/students".equals(path)) {
            data = withFlash(request, mapOf("students", studentList()), user);
        } else if ("/admin/records".equals(path)) {
            data = withFlash(request, mapOf("records", recordList(mealRecordDAO.getAllRecords())), user);
        } else if ("/student/dashboard".equals(path)) {
            data = studentDashboard(request, user);
        } else if ("/student/bill".equals(path)) {
            String dateParam = request.getParameter("date");
            LocalDate billDate = (dateParam == null || dateParam.isBlank())
                    ? LocalDate.now()
                    : LocalDate.parse(dateParam, DateTimeFormatter.ISO_LOCAL_DATE);
            data = withFlash(request, mealRecordDAO.getDailyBill(user.getUserId(), billDate), user);
        } else if ("/admin/revenue".equals(path)) {
            String dateParam = request.getParameter("date");
            LocalDate revenueDate = (dateParam == null || dateParam.isBlank())
                    ? LocalDate.now()
                    : LocalDate.parse(dateParam, DateTimeFormatter.ISO_LOCAL_DATE);
            data = withFlash(request, revenueResponse(revenueDate), user);
        } else if ("/account".equals(path)) {
            Map<String, Object> account = new LinkedHashMap<>();
            account.put("email", user.getEmail());
            account.put("role", user.getRole());
            account.put("createdAt", user.getCreatedAt() == null ? null
                    : user.getCreatedAt().toLocalDateTime().toLocalDate()
                            .format(DateTimeFormatter.ISO_LOCAL_DATE));
            data = withFlash(request, account, user);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        writeJson(response, data);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!"/account".equals(request.getPathInfo())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (!SessionUtil.requireLogin(request, response, null)) {
            return;
        }

        User user = SessionUtil.getCurrentUser(request);
        String name = request.getParameter("name");
        name = name == null ? "" : name.trim();
        if (name.isEmpty() || name.length() > 100) {
            SessionUtil.setFlashError(request, "Enter a name between 1 and 100 characters.");
            response.sendRedirect(request.getContextPath() + "/account-settings.html");
            return;
        }

        try {
            if (!userDAO.updateName(user.getUserId(), name)) {
                SessionUtil.setFlashError(request, "The account could not be found. Please sign in again.");
                response.sendRedirect(request.getContextPath() + "/account-settings.html");
                return;
            }
        } catch (IllegalStateException e) {
            getServletContext().log("Account name update failed.", e);
            SessionUtil.setFlashError(request, "Unable to save your account settings. Please try again.");
            response.sendRedirect(request.getContextPath() + "/account-settings.html");
            return;
        }

        user.setName(name);
        SessionUtil.setFlash(request, "Your account settings have been saved.");
        response.sendRedirect(request.getContextPath() + "/account-settings.html");
    }

    private Map<String, Object> adminDashboard(HttpServletRequest request, User user) {
        LocalDate today = LocalDate.now();
        Map<String, Integer> counts = mealRecordDAO.getTodayCounts(today);
        Map<String, BigDecimal> expensesByMeal = mealRecordDAO.getTodayExpensesByMeal(today);
        BigDecimal todayExpenseTotal = BigDecimal.ZERO;
        boolean missingExpenseRate = false;
        for (String mealType : MEAL_TYPES) {
            BigDecimal mealExpense = expensesByMeal.getOrDefault(mealType, BigDecimal.ZERO);
            if (mealExpense == null) {
                missingExpenseRate = true;
            } else {
                todayExpenseTotal = todayExpenseTotal.add(mealExpense);
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", user.getName());
        result.put("today", today);
        result.put("totalStudents", userDAO.getAllStudents().size());
        result.put("totalMenus", menuDAO.getAllMenus().size());
        result.put("optedInCount", counts.getOrDefault("OPTED_IN", 0));
        result.put("optedOutCount", counts.getOrDefault("OPTED_OUT", 0));
        result.put("consumedCount", counts.getOrDefault("CONSUMED", 0));
        result.put("todayExpenseTotal", missingExpenseRate ? null : todayExpenseTotal);
        result.put("todayExpensesByMeal", expensesByMeal);
        return withFlash(request, result, user);
    }

    private Map<String, Object> studentDashboard(HttpServletRequest request, User user) {
        LocalDate today = LocalDate.now();
        List<Menu> menus = menuDAO.getAllMenus();
        Map<String, Object> statuses = new LinkedHashMap<>();
        for (String mealType : MEAL_TYPES) {
            statuses.put(mealType, mealRecordDAO.getStatus(user.getUserId(), today, mealType));
        }
        List<MealRecord> history = mealRecordDAO.getRecordsByStudent(user.getUserId());
        BigDecimal expenseTotal = BigDecimal.ZERO;
        Map<String, BigDecimal> dailyExpenseTotals = new LinkedHashMap<>();
        Set<String> incompleteExpenseDates = new HashSet<>();
        for (MealRecord record : history) {
            String mealDate = record.getMealDate().toString();
            dailyExpenseTotals.putIfAbsent(mealDate, BigDecimal.ZERO);
            if (record.getExpense() != null) {
                expenseTotal = expenseTotal.add(record.getExpense());
                dailyExpenseTotals.merge(mealDate, record.getExpense(), BigDecimal::add);
            } else {
                incompleteExpenseDates.add(mealDate);
            }
        }
        for (String mealDate : incompleteExpenseDates) {
            dailyExpenseTotals.put(mealDate, null);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", user.getName());
        result.put("today", today);
        result.put("days", DAYS);
        result.put("mealTypes", MEAL_TYPES);
        result.put("menus", menuList(menus));
        result.put("statuses", statuses);
        result.put("history", recordList(history));
        result.put("expenseTotal", expenseTotal);
        result.put("dailyExpenseTotals", dailyExpenseTotals);
        result.put("todayExpenseTotal",
                dailyExpenseTotals.containsKey(today.toString())
                        ? dailyExpenseTotals.get(today.toString())
                        : BigDecimal.ZERO);
        return withFlash(request, result, user);
    }

    private List<Map<String, Object>> menuList() {
        return menuList(menuDAO.getAllMenus());
    }

    private List<Map<String, Object>> menuList(List<Menu> menus) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Menu menu : menus) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("menuId", menu.getMenuId());
            item.put("dayOfWeek", menu.getDayOfWeek());
            item.put("mealType", menu.getMealType());
            item.put("items", menu.getItems());
            item.put("price", menu.getPrice());
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> studentList() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (User student : userDAO.getAllStudents()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("userId", student.getUserId());
            item.put("name", student.getName());
            item.put("email", student.getEmail());
            item.put("createdAt", student.getCreatedAt());
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> recordList(List<MealRecord> records) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (MealRecord record : records) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("recordId", record.getRecordId());
            item.put("studentId", record.getStudentId());
            item.put("studentName", record.getStudentName());
            item.put("mealDate", record.getMealDate());
            item.put("mealType", record.getMealType());
            item.put("status", record.getStatus());
            item.put("expense", record.getExpense());
            result.add(item);
        }
        return result;
    }

    private Map<String, Object> revenueResponse(LocalDate date) {
        BigDecimal revenue = mealRecordDAO.getRevenue(date);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("date", date);
        result.put("revenue", revenue);
        return result;
    }

    private Map<String, Object> withFlash(HttpServletRequest request, Map<String, Object> data, User user) {
        data.put("name", user.getName());
        data.put("flashError", SessionUtil.consumeFlashError(request));
        data.put("flashMessage", SessionUtil.consumeFlash(request));
        return data;
    }

    private Map<String, Object> mapOf(String key, Object value) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put(key, value);
        return result;
    }

    private void writeJson(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(JsonUtil.stringify(data));
    }

}
