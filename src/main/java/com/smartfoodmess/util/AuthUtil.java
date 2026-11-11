package com.smartfoodmess.util;

import com.smartfoodmess.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Reusable session / authorization helper.
 *
 * <p>Every servlet that needs protection calls one of the
 * {@code requireXxx(...)} methods first. If the check fails, the helper
 * already sends a correct JSON error (401 = not logged in, 403 = wrong
 * role) and the servlet simply returns. This keeps security rules in
 * ONE place instead of repeating them in every servlet.</p>
 *
 * <p>Session contents:</p>
 * <ul>
 *   <li>{@value #SESSION_USER}       - the logged in {@link User}</li>
 *   <li>{@value #SESSION_STUDENT_ID} - student id (students only)</li>
 * </ul>
 */
public final class AuthUtil {

    /** Session attribute that stores the logged-in User object. */
    public static final String SESSION_USER = "user";

    /** Session attribute that stores the student id (a Long). */
    public static final String SESSION_STUDENT_ID = "studentId";

    private AuthUtil() {
        // utility class - cannot be instantiated
    }

    /** Returns the logged-in user, or {@code null} if nobody is logged in. */
    public static User getLoggedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false); // false = do not create a session
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(SESSION_USER);
        return (value instanceof User) ? (User) value : null;
    }

    /** Returns the logged-in student id, or 0 when not available. */
    public static long getLoggedStudentId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return 0L;
        }
        Object value = session.getAttribute(SESSION_STUDENT_ID);
        return (value instanceof Long) ? (Long) value : 0L;
    }

    /**
     * Requires any logged-in user.
     *
     * @return the user, or {@code null} after sending a 401 JSON response
     */
    public static User requireLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = getLoggedUser(req);
        if (user == null) {
            writeError(resp, 401, "You must be logged in to do this.");
            return null;
        }
        return user;
    }

    /**
     * Requires an administrator session.
     *
     * @return the user, or {@code null} after sending 401/403 JSON response
     */
    public static User requireAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = requireLogin(req, resp);
        if (user == null) {
            return null;
        }
        if (!user.isAdmin()) {
            writeError(resp, 403, "Administrator access is required for this action.");
            return null;
        }
        return user;
    }

    /**
     * Requires a student session.
     *
     * @return the user, or {@code null} after sending 401/403 JSON response
     */
    public static User requireStudent(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = requireLogin(req, resp);
        if (user == null) {
            return null;
        }
        if (!user.isStudent()) {
            writeError(resp, 403, "Student access is required for this action.");
            return null;
        }
        return user;
    }

    /** Sends a standard JSON error response: {@code {success:false, error:...}} */
    public static void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        JsonUtil.write(resp, status, JsonUtil.object(
                JsonUtil.field("success", false),
                JsonUtil.field("error", message)));
    }
}
