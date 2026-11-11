package com.mess.servlet;

import com.mess.dao.MenuDAO;
import com.mess.model.Menu;
import com.mess.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Admin: view and manage the weekly menu.
 * GET  -> list all menu entries (supports ?edit=menuId to pre-fill the form)
 * POST -> add / update / delete a menu entry (based on the "action" parameter)
 */
@WebServlet("/admin/menu")
public class AdminMenuServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Set<String> DAYS = new HashSet<>(Arrays.asList(
            "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"));

    private static final Set<String> MEAL_TYPES = new HashSet<>(Arrays.asList(
            "BREAKFAST", "LUNCH", "DINNER"));

    private final MenuDAO menuDAO = new MenuDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.requireLogin(request, response, "ADMIN")) {
            return;
        }

        String editId = request.getParameter("edit");
        if (editId != null) {
            int menuId = parseId(editId);
            if (menuId > 0) {
                response.sendRedirect(request.getContextPath() + "/admin-menu.html?edit=" + menuId);
                return;
            }
        }
        response.sendRedirect(request.getContextPath() + "/admin-menu.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.requireLogin(request, response, "ADMIN")) {
            return;
        }

        String action = request.getParameter("action");
        String message = null;
        String errorMessage = null;

        if ("delete".equals(action)) {
            int menuId = parseId(request.getParameter("menuId"));
            if (menuId > 0 && menuDAO.deleteMenu(menuId)) {
                message = "Menu entry deleted.";
            } else {
                errorMessage = "Could not delete that menu entry.";
            }
        } else {
            String dayOfWeek = request.getParameter("dayOfWeek");
            String mealType = request.getParameter("mealType");
            String items = request.getParameter("items") == null ? "" : request.getParameter("items").trim();
            BigDecimal price = parsePrice(request.getParameter("price"));

            if (!DAYS.contains(dayOfWeek) || !MEAL_TYPES.contains(mealType) || items.isEmpty() || price == null || price.compareTo(BigDecimal.ZERO) < 0) {
                errorMessage = "Please fill in day, meal type, items and a valid Indian Rupee price.";
            } else if ("update".equals(action)) {
                int menuId = parseId(request.getParameter("menuId"));
                if (menuId > 0 && menuDAO.updateMenu(new Menu(menuId, dayOfWeek, mealType, items, price))) {
                    message = "Menu entry updated.";
                } else {
                    errorMessage = "Could not update that menu entry.";
                }
            } else if ("add".equals(action)) {
                if (menuDAO.addMenu(new Menu(0, dayOfWeek, mealType, items, price))) {
                    message = "Menu entry added.";
                } else {
                    errorMessage = "A menu entry for " + dayOfWeek + " " + mealType + " already exists.";
                }
            } else {
                errorMessage = "Unknown action requested.";
            }
        }

        if (message == null) {
            SessionUtil.setFlashError(request, errorMessage);
        } else {
            SessionUtil.setFlash(request, message);
        }
        response.sendRedirect(request.getContextPath() + "/admin-menu.html");
    }

    private int parseId(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private BigDecimal parsePrice(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}