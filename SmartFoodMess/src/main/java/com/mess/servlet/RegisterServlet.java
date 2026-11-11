package com.mess.servlet;

import com.mess.dao.UserDAO;
import com.mess.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Handles new student registration.
 * GET  -> shows the registration form
 * POST -> validates input and creates the account (role: STUDENT)
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/register.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        String normalizedEmail = email == null ? "" : email.trim();
        String normalizedName = name == null ? "" : name.trim();

        if (normalizedName.length() < 3) {
            redirectToRegister(request, response, "name");
            return;
        } else if (normalizedEmail.isEmpty() || !EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            redirectToRegister(request, response, "email");
            return;
        } else if (password == null || password.length() < 6) {
            redirectToRegister(request, response, "password");
            return;
        } else if (!password.equals(confirmPassword)) {
            redirectToRegister(request, response, "mismatch");
            return;
        } else if (userDAO.isEmailRegistered(normalizedEmail)) {
            redirectToRegister(request, response, "exists");
            return;
        }

        User newUser = new User(0, normalizedName, normalizedEmail, password, "STUDENT", null);
        if (userDAO.registerUser(newUser)) {
            response.sendRedirect(request.getContextPath() + "/login.html?registered=true");
        } else {
            redirectToRegister(request, response, "failed");
        }
    }

    private void redirectToRegister(HttpServletRequest request, HttpServletResponse response, String error)
            throws IOException {
        response.sendRedirect(request.getContextPath() + "/register.html?error=" + error);
    }
}