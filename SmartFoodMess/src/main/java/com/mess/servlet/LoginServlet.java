package com.mess.servlet;

import com.mess.dao.UserDAO;
import com.mess.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Handles user login.
 *
 * GET  -> redirects to the static login.html page
 * POST -> validates credentials and routes the user to the
 *         appropriate dashboard based on their role.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/login.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || email.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            redirectToLogin(request, response, "missing");
            return;
        }

        User user;
        try {
            user = userDAO.validateUser(email.trim(), password);
        } catch (IllegalStateException e) {
            getServletContext().log("Login database access failed.", e);
            redirectToLogin(request, response, "database");
            return;
        }

        if (user != null) {
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);
            session.setAttribute("role", user.getRole());

            if ("ADMIN".equals(user.getRole())) {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            } else {
                response.sendRedirect(request.getContextPath() + "/student/dashboard");
            }
        } else {
            redirectToLogin(request, response, "invalid");
        }
    }

    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response, String error)
            throws IOException {
        response.sendRedirect(request.getContextPath() + "/login.html?error=" + error);
    }
}