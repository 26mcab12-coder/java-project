package com.smartfoodmess.web;

import com.smartfoodmess.exception.DatabaseException;
import com.smartfoodmess.exception.InvalidInputException;
import com.smartfoodmess.model.Student;
import com.smartfoodmess.service.MessOperations;
import com.smartfoodmess.service.MessService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Handles student registration (register.html form).
 *
 * <pre>
 *   GET  /register - redirects to the HTML page
 *   POST /register - validates input, rejects duplicate username/email,
 *                    inserts users + students rows, then redirects to
 *                    login.html?registered=1
 * </pre>
 *
 * <p>Registration always creates a STUDENT account. The administrator
 * account is inserted by database/schema.sql (there is no public admin
 * registration on purpose).</p>
 */
@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {

    private final MessOperations service = new MessService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/register.html");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");

        try {
            // Server-side check of the confirm field (JS does this too,
            // but never trust the browser).
            if (password == null || !password.equals(confirmPassword)) {
                throw new InvalidInputException("Passwords do not match.");
            }

            Student student = service.registerStudent(name, email, phone, username, password);

            // Success -> go to login page with a friendly confirmation.
            resp.sendRedirect(req.getContextPath() + "/login.html?registered=1&username="
                    + URLEncoder.encode(student.getUsername(), StandardCharsets.UTF_8));
        } catch (InvalidInputException | DatabaseException e) {
            // Friendly message back into the form page (URL encoded).
            resp.sendRedirect(req.getContextPath() + "/register.html?error="
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        }
    }
}
