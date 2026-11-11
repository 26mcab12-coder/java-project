package com.mess.util;

import com.mess.model.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Helper methods for working with the logged-in user's {@link HttpSession}.
 * Used by servlets to enforce role-based access and to pass one-shot
 * (flash) messages between a POST redirect and the next page.
 */
public final class SessionUtil {

    private SessionUtil() {
        // Prevent instantiation.
    }

    /**
     * Returns the currently logged-in user, or {@code null} if there is none.
     */
    public static User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object user = session.getAttribute("user");
        return (user instanceof User) ? (User) user : null;
    }

    /**
     * Guards a page/servlet behind a login and (optionally) a specific role.
     *
     * @return {@code true} if the request may continue, {@code false} if the
     *         caller was redirected to the login page (or home page when the
     *         logged-in role is wrong).
     */
    public static boolean requireLogin(HttpServletRequest request, HttpServletResponse response, String role)
            throws IOException {
        User user = getCurrentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        if (role != null && !role.equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/");
            return false;
        }
        return true;
    }

    /**
     * Stores a one-shot message that survives a redirect.
     */
    public static void setFlash(HttpServletRequest request, String message) {
        HttpSession session = request.getSession(true);
        session.setAttribute("flashMessage", message);
        session.setAttribute("flashError", false);
    }

    public static void setFlashError(HttpServletRequest request, String message) {
        HttpSession session = request.getSession(true);
        session.setAttribute("flashMessage", message);
        session.setAttribute("flashError", true);
    }

    /**
     * Returns and removes the one-shot flash message, or {@code null}.
     */
    public static String consumeFlash(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object message = session.getAttribute("flashMessage");
        session.removeAttribute("flashMessage");
        return message == null ? null : message.toString();
    }

    public static boolean consumeFlashError(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Object error = session.getAttribute("flashError");
        session.removeAttribute("flashError");
        return Boolean.TRUE.equals(error);
    }
}