package com.mess.servlet;

import com.mess.dao.MealRecordDAO;
import com.mess.model.User;
import com.mess.util.SessionUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles a student's opt-in / opt-out action for a meal on a given date.
 * POST only; redirects back to the student dashboard with a flash message.
 */
@WebServlet("/student/opt")
public class MealOptServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Set<String> MEAL_TYPES =
            new HashSet<>(Arrays.asList("BREAKFAST", "LUNCH", "DINNER"));

    private final MealRecordDAO mealRecordDAO = new MealRecordDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        if (!SessionUtil.requireLogin(request, response, "STUDENT")) {
            return;
        }

        User user = SessionUtil.getCurrentUser(request);
        String action = request.getParameter("action");
        String mealType = request.getParameter("mealType");
        String dateStr = request.getParameter("date");

        LocalDate date;
        try {
            date = (dateStr == null || dateStr.isEmpty()) ? LocalDate.now() : LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            date = LocalDate.now();
        }

        boolean isOptIn = "optin".equals(action);
        boolean isOptOut = "optout".equals(action);
        boolean isValidMeal = mealType != null && MEAL_TYPES.contains(mealType);
        boolean isValidDate = !date.isBefore(LocalDate.now());

        if (!isValidMeal || (!isOptIn && !isOptOut) || !isValidDate) {
            SessionUtil.setFlashError(request, "Invalid opt-in/opt-out request. Please try again.");
            response.sendRedirect(request.getContextPath() + "/student/dashboard");
            return;
        }

        String status = isOptIn ? "OPTED_IN" : "OPTED_OUT";
        boolean success = mealRecordDAO.upsert(user.getUserId(), date, mealType, status);

        if (success) {
            SessionUtil.setFlash(request,
                    "You have opted " + (isOptIn ? "IN" : "OUT") + " for " + mealType + " on " + date + ".");
        } else {
            SessionUtil.setFlashError(request, "Something went wrong while saving your choice.");
        }

        response.sendRedirect(request.getContextPath() + "/student/dashboard");
    }
}