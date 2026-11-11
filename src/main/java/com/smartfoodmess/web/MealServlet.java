package com.smartfoodmess.web;

import com.smartfoodmess.exception.DatabaseException;
import com.smartfoodmess.exception.InvalidInputException;
import com.smartfoodmess.model.MealRecord;
import com.smartfoodmess.model.User;
import com.smartfoodmess.service.MessOperations;
import com.smartfoodmess.service.MessService;
import com.smartfoodmess.util.AuthUtil;
import com.smartfoodmess.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Meal records: students see their own records, admins see (and manage)
 * all records.
 *
 * <pre>
 *   GET  /meals - STUDENT -> own records, ADMIN -> every record (JSON)
 *   POST /meals - ADMIN only, action = updateStatus | delete -> JSON result
 * </pre>
 */
@WebServlet(name = "MealServlet", urlPatterns = {"/meals"})
public class MealServlet extends HttpServlet {

    private final MessOperations service = new MessService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = AuthUtil.requireLogin(req, resp);
        if (user == null) {
            return;
        }

        try {
            List<MealRecord> records;
            if (user.isAdmin()) {
                records = service.getAllMealRecords();   // admin: everything
            } else {
                records = service.getMealRecordsOf(      // student: own rows only
                        AuthUtil.getLoggedStudentId(req));
            }

            List<String> jsonItems = new ArrayList<>();
            for (MealRecord r : records) {
                jsonItems.add(JsonUtil.object(
                        JsonUtil.field("mealId", r.getMealId()),
                        JsonUtil.field("studentId", r.getStudentId()),
                        JsonUtil.field("studentName", r.getStudentName()),
                        JsonUtil.field("menuId", r.getMenuId()),
                        JsonUtil.field("foodName", r.getFoodName()),
                        JsonUtil.field("mealType", r.getMealType()),
                        JsonUtil.field("mealDate", r.getMealDate()),
                        JsonUtil.field("quantity", r.getQuantity()),
                        JsonUtil.field("status", r.getStatus()),
                        JsonUtil.field("totalPrice", r.getTotalPrice())));
            }
            JsonUtil.write(resp, 200, JsonUtil.array(jsonItems));
        } catch (InvalidInputException e) {
            AuthUtil.writeError(resp, 400, e.getMessage());
        } catch (DatabaseException e) {
            AuthUtil.writeError(resp, 500, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            AuthUtil.writeError(resp, 500, "Something went wrong while loading meal records.");
        }
    }

    /** Change status / delete a record - administrators only. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = AuthUtil.requireAdmin(req, resp);
        if (user == null) {
            return;
        }

        String action = req.getParameter("action");
        try {
            if ("updateStatus".equals(action)) {
                long mealId = parseId(req.getParameter("mealId"), "meal record");
                String status = req.getParameter("status");
                service.updateMealStatus(mealId, status);
                writeSuccess(resp, "Meal status updated.");

            } else if ("delete".equals(action)) {
                long mealId = parseId(req.getParameter("mealId"), "meal record");
                service.deleteMealRecord(mealId);
                writeSuccess(resp, "Meal record deleted.");

            } else {
                AuthUtil.writeError(resp, 400, "Unknown action.");
            }
        } catch (InvalidInputException e) {
            AuthUtil.writeError(resp, 400, e.getMessage());
        } catch (DatabaseException e) {
            AuthUtil.writeError(resp, 500, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            AuthUtil.writeError(resp, 500, "Something went wrong while saving the meal record.");
        }
    }

    // ---- helpers --------------------------------------------------------

    private void writeSuccess(HttpServletResponse resp, String message) throws IOException {
        JsonUtil.write(resp, 200, JsonUtil.object(
                JsonUtil.field("success", true),
                JsonUtil.field("message", message)));
    }

    private long parseId(String text, String name) throws InvalidInputException {
        if (text == null || text.isBlank()) {
            throw new InvalidInputException("Missing " + name + " id.");
        }
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Invalid " + name + " id.");
        }
    }
}
