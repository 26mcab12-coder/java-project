package com.smartfoodmess.web;

import com.smartfoodmess.exception.AuthenticationException;
import com.smartfoodmess.exception.DatabaseException;
import com.smartfoodmess.exception.InvalidInputException;
import com.smartfoodmess.model.Student;
import com.smartfoodmess.model.User;
import com.smartfoodmess.service.MessOperations;
import com.smartfoodmess.service.MessService;
import com.smartfoodmess.util.AuthUtil;
import com.smartfoodmess.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Handles login for both students and administrators.
 *
 * <pre>
 *   GET  /login  - returns the current session as JSON (used by pages to
 *                  check "am I logged in?"); {loggedIn:false} if not.
 *   POST /login  - validates username/password against MySQL, creates the
 *                  HttpSession and redirects to the correct dashboard:
 *                  STUDENT -> student-dashboard.html
 *                  ADMIN   -> admin-dashboard.html
 * </pre>
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private final MessOperations service = new MessService();

    /** Used by pages to detect an existing session (auto-redirect, guards). */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = AuthUtil.getLoggedUser(req);
        if (user == null) {
            JsonUtil.write(resp, 200, JsonUtil.object(
                    JsonUtil.field("loggedIn", false)));
            return;
        }
        JsonUtil.write(resp, 200, JsonUtil.object(
                JsonUtil.field("loggedIn", true),
                JsonUtil.field("username", user.getUsername()),
                JsonUtil.field("role", user.getRole()),
                JsonUtil.field("studentId", AuthUtil.getLoggedStudentId(req))));
    }

    /** Traditional form POST - redirect based, works even without JavaScript. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        try {
            User user = service.login(username, password);

            // Put the user into the session - all later pages check this.
            HttpSession session = req.getSession(true);
            session.setAttribute(AuthUtil.SESSION_USER, user);

            if (user.isStudent()) {
                Student student = service.getStudentByUsername(user.getUsername());
                if (student != null) {
                    session.setAttribute(AuthUtil.SESSION_STUDENT_ID, student.getStudentId());
                }
                resp.sendRedirect(req.getContextPath() + "/student-dashboard.html");
            } else {
                session.removeAttribute(AuthUtil.SESSION_STUDENT_ID);
                resp.sendRedirect(req.getContextPath() + "/admin-dashboard.html");
            }
        } catch (AuthenticationException | InvalidInputException | DatabaseException e) {
            // All three carry safe, user-friendly messages (no SQL details).
            resp.sendRedirect(req.getContextPath() + "/login.html?error="
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        }
    }

    /** Servlets are not HTML pages - send the browser to the real page. */
    @Override
    protected void doGetLegacyRedirect(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/login.html");
    }
}
