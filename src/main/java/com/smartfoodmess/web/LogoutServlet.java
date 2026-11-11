package com.smartfoodmess.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Logs the user out.
 *
 * <p>{@code GET /logout} invalidates the whole session (so the session id
 * can no longer be reused) and sends the browser back to the home page.</p>
 */
@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false); // do not create a new one
        if (session != null) {
            session.invalidate(); // remove ALL session data
        }
        resp.sendRedirect(req.getContextPath() + "/index.html");
    }

    /** Also accept POST so a logout button can use either method. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doGet(req, resp);
    }
}
