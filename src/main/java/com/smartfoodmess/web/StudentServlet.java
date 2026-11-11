package com.smartfoodmess.web;

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
import java.util.ArrayList;
import java.util.List;

/**
 * Student management (admin) and "my profile" (student).
 *
 * <pre>
 *   GET  /students                  - ADMIN only -> JSON list of students
 *   GET  /students?action=profile   - STUDENT only -> own profile JSON
 *   POST /students                  - ADMIN only, action = add|update|delete
 * </pre>
 *
 * <p>Every branch checks the session first through {@link AuthUtil}, so a
 * student can never read the full list or call an admin action (403 is
 * returned instead).</p>
 */
@WebServlet(name = "StudentServlet", urlPatterns = {"/students"})
public class StudentServlet extends HttpServlet {

    private final MessOperations service = new MessService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = req.getParameter("action");

        if ("profile".equals(action)) {
            readOwnProfile(req, resp);
            return;
        }

        // Anything else = full student list -> administrators only
        User user = AuthUtil.requireAdmin(req, resp);
        if (user == null) {
            return;
        }
        try {
            List<Student> students = service.getAllStudents();
            List<String> jsonItems = new ArrayList<>();
            for (Student s : students) {
                jsonItems.add(JsonUtil.object(
                        JsonUtil.field("studentId", s.getStudentId()),
                        JsonUtil.field("userId", s.getUserId()),
                        JsonUtil.field("name", s.getName()),
                        JsonUtil.field("email", s.getEmail()),
                        JsonUtil.field("phone", s.getPhone()),
                        JsonUtil.field("username", s.getUsername())));
            }
            JsonUtil.write(resp, 200, JsonUtil.array(jsonItems));
        } catch (DatabaseException e) {
            AuthUtil.writeError(resp, 500, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            AuthUtil.writeError(resp, 500, "Something went wrong while loading students.");
        }
    }

    /** add / update / delete - administrators only. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = AuthUtil.requireAdmin(req, resp);
        if (user == null) {
            return;
        }

        String action = req.getParameter("action");
        try {
            if ("add".equals(action)) {
                // Creates users + students rows (same rules as registration)
                Student student = service.registerStudent(
                        req.getParameter("name"),
                        req.getParameter("email"),
                        req.getParameter("phone"),
                        req.getParameter("username"),
                        req.getParameter("password"));
                JsonUtil.write(resp, 201, JsonUtil.object(
                        JsonUtil.field("success", true),
                        JsonUtil.field("message", "Student added."),
                        JsonUtil.field("studentId", student.getStudentId())));

            } else if ("update".equals(action)) {
                long studentId = parseId(req.getParameter("studentId"), "student");
                service.updateStudent(
                        studentId,
                        req.getParameter("name"),
                        req.getParameter("email"),
                        req.getParameter("phone"),
                        req.getParameter("password")); // blank = keep old password
                writeSuccess(resp, "Student updated.");

            } else if ("delete".equals(action)) {
                long studentId = parseId(req.getParameter("studentId"), "student");
                service.deleteStudent(studentId);
                writeSuccess(resp, "Student deleted.");

            } else {
                AuthUtil.writeError(resp, 400, "Unknown action.");
            }
        } catch (InvalidInputException e) {
            AuthUtil.writeError(resp, 400, e.getMessage());
        } catch (DatabaseException e) {
            AuthUtil.writeError(resp, 500, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            AuthUtil.writeError(resp, 500, "Something went wrong while saving the student.");
        }
    }

    // ---- helpers --------------------------------------------------------

    /** Returns the profile of the logged-in student. */
    private void readOwnProfile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = AuthUtil.requireStudent(req, resp);
        if (user == null) {
            return;
        }
        try {
            Student student = service.getStudentByUsername(user.getUsername());
            if (student == null) {
                AuthUtil.writeError(resp, 404, "Student profile not found.");
                return;
            }
            JsonUtil.write(resp, 200, JsonUtil.object(
                    JsonUtil.field("studentId", student.getStudentId()),
                    JsonUtil.field("userId", student.getUserId()),
                    JsonUtil.field("name", student.getName()),
                    JsonUtil.field("email", student.getEmail()),
                    JsonUtil.field("phone", student.getPhone()),
                    JsonUtil.field("username", student.getUsername())));
        } catch (DatabaseException e) {
            AuthUtil.writeError(resp, 500, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            AuthUtil.writeError(resp, 500, "Something went wrong while loading your profile.");
        }
    }

    private void writeSuccess(HttpServletResponse resp, String message) throws IOException {
        JsonUtil.write(resp, 200, JsonUtil.object(
                JsonUtil.field("success", true),
                JsonUtil.field("message", message)));
    }

    private long parseId(String text, String name) throws InvalidInputException {
        if (text == null || text.isBlank()) {
            throw new InvalidInputException("Missing " + name + " id.");
        }
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Invalid " + name + " id.");
        }
    }
}
