package com.mess.servlet;

import com.mess.dao.MealRecordDAO;
import com.mess.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
/**
 * Admin: view all meal records, mark meals as consumed, or delete records.
 */
@WebServlet("/admin/records")
public class AdminRecordServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final MealRecordDAO mealRecordDAO = new MealRecordDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.requireLogin(request, response, "ADMIN")) {
            return;
        }
        response.sendRedirect(request.getContextPath() + "/admin-records.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.requireLogin(request, response, "ADMIN")) {
            return;
        }

        String action = request.getParameter("action");
        int recordId = parseId(request.getParameter("recordId"));
        String message = null;
        String errorMessage = null;

        if ("consume".equals(action)) {
            if (recordId > 0 && mealRecordDAO.markConsumed(recordId)) {
                message = "Meal record marked as CONSUMED.";
            } else {
                errorMessage = "Could not mark that record as consumed.";
            }
        } else if ("delete".equals(action)) {
            if (recordId > 0 && mealRecordDAO.deleteRecord(recordId)) {
                message = "Meal record deleted.";
            } else {
                errorMessage = "Could not delete that record.";
            }
        } else {
            errorMessage = "Unknown action requested.";
        }

        if (message == null) {
            SessionUtil.setFlashError(request, errorMessage);
        } else {
            SessionUtil.setFlash(request, message);
        }
        response.sendRedirect(request.getContextPath() + "/admin-records.html");
    }

    private int parseId(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}