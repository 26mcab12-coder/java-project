package com.smartfoodmess.web;

import com.smartfoodmess.exception.DatabaseException;
import com.smartfoodmess.exception.InvalidInputException;
import com.smartfoodmess.model.MenuItem;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Menu display (students) and menu management (admin).
 *
 * <pre>
 *   GET  /menu - any logged-in user -> JSON list of menu items
 *   POST /menu - ADMIN only, action = add | update | delete -> JSON result
 * </pre>
 */
@WebServlet(name = "MenuServlet", urlPatterns = {"/menu"})
public class MenuServlet extends HttpServlet {

    private final MessOperations service = new MessService();

    /** Returns the menu as JSON. Students and admins may both read it. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = AuthUtil.requireLogin(req, resp);
        if (user == null) {
            return; // 401 already sent by AuthUtil
        }

        try {
            List<MenuItem> items = service.getAllMenu();
            LocalDate today = LocalDate.now();
            List<String> jsonItems = new ArrayList<>();
            for (MenuItem item : items) {
                jsonItems.add(JsonUtil.object(
                        JsonUtil.field("id", item.getId()),
                        JsonUtil.field("foodName", item.getFoodName()),
                        JsonUtil.field("mealType", item.getMealType()),
                        JsonUtil.field("price", item.getPrice()),
                        JsonUtil.field("availableDate", item.getAvailableDate()),
                        JsonUtil.field("available", item.isAvailableOn(today))));
            }
            JsonUtil.write(resp, 200, JsonUtil.array(jsonItems));
        } catch (DatabaseException e) {
            AuthUtil.writeError(resp, 500, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            AuthUtil.writeError(resp, 500, "Something went wrong while loading the menu.");
        }
    }

    /** Add / update / delete a menu item - administrators only. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = AuthUtil.requireAdmin(req, resp);
        if (user == null) {
            return; // 401/403 already sent
        }

        String action = req.getParameter("action");
        try {
            if ("add".equals(action)) {
                service.addMenuItem(
                        req.getParameter("foodName"),
                        req.getParameter("mealType"),
                        req.getParameter("price"),
                        req.getParameter("date"));
                writeSuccess(resp, "Menu item added.");

            } else if ("update".equals(action)) {
                long menuId = parseId(req.getParameter("menuId"), "menu item");
                service.updateMenuItem(
                        menuId,
                        req.getParameter("foodName"),
                        req.getParameter("mealType"),
                        req.getParameter("price"),
                        req.getParameter("date"));
                writeSuccess(resp, "Menu item updated.");

            } else if ("delete".equals(action)) {
                long menuId = parseId(req.getParameter("menuId"), "menu item");
                service.deleteMenuItem(menuId);
                writeSuccess(resp, "Menu item deleted.");

            } else {
                AuthUtil.writeError(resp, 400, "Unknown action.");
            }
        } catch (InvalidInputException e) {
            AuthUtil.writeError(resp, 400, e.getMessage());
        } catch (DatabaseException e) {
            // friendly message (e.g. "already exists" or FK protection)
            AuthUtil.writeError(resp, 500, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            AuthUtil.writeError(resp, 500, "Something went wrong while saving the menu item.");
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
