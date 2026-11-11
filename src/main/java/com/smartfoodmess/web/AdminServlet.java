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

import java.io.IOException;
import java.util.Map;

/**
 * Small helper endpoint used by the dashboard pages.
 *
 * <pre>
 *   GET /admin - any logged-in user -> JSON:
 *     { loggedIn, username, role, displayName, studentId }
 *     plus (administrators only):
 *     { countStudents, countMenu, countMeals }
 * </pre>
 *
 * <p>The dashboards call this once after loading to (a) show the welcome
 * name, (b) detect the role and (c) redirect to the login page when nobody
 * is logged in - a missing session simply returns HTTP 401.</p>
 */
@WebServlet(name = "AdminServlet", urlPatterns = {"/admin"})
public class AdminServlet extends HttpServlet {

    private final MessOperations service = new MessService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = AuthUtil.requireLogin(req, resp);
        if (user == null) {
            return; // 401 sent by AuthUtil -> pages redirect to login
        }

        try {
            String displayName = "Administrator";
            long studentId = AuthUtil.getLoggedStudentId(req);

            if (user.isStudent()) {
                Student student = service.getStudentByUsername(user.getUsername());
                if (student != null) {
                    displayName = student.getName();
                } else {
                    displayName = user.getUsername();
                }
            }

            if (user.isAdmin()) {
                Map<String, Long> counts = service.getDashboardCounts();
                JsonUtil.write(resp, 200, JsonUtil.object(
                        JsonUtil.field("loggedIn", true),
                        JsonUtil.field("username", user.getUsername()),
                        JsonUtil.field("role", user.getRole()),
                        JsonUtil.field("displayName", displayName),
                        JsonUtil.field("studentId", studentId),
                        JsonUtil.field("countStudents", counts.getOrDefault("students", 0L)),
                        JsonUtil.field("countMenu", counts.getOrDefault("menu", 0L)),
                        JsonUtil.field("countMeals", counts.getOrDefault("meals", 0L))));
            } else {
                JsonUtil.write(resp, 200, JsonUtil.object(
                        JsonUtil.field("loggedIn", true),
                        JsonUtil.field("username", user.getUsername()),
                        JsonUtil.field("role", user.getRole()),
                        JsonUtil.field("displayName", displayName),
                        JsonUtil.field("studentId", studentId)));
            }
        } catch (DatabaseException e) {
            AuthUtil.writeError(resp, 500, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            AuthUtil.writeError(resp, 500, "Something went wrong while loading the dashboard.");
        }
    }
}
