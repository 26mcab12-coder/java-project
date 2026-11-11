package com.mess.servlet;

import com.mess.dao.UserDAO;
import com.mess.model.User;
import com.mess.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
/**
 * Admin: view all student accounts and delete them.
 */
@WebServlet("/admin/students")
public class AdminStudentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.requireLogin(request, response, "ADMIN")) {
            return;
        }
        response.sendRedirect(request.getContextPath() + "/admin-students.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.requireLogin(request, response, "ADMIN")) {
            return;
        }

        String message = null;
        String errorMessage = null;

        if ("delete".equals(request.getParameter("action"))) {
            int userId = parseId(request.getParameter("userId"));
            if (userId > 0 && userDAO.deleteStudent(userId)) {
                message = "Student deleted.";
            } else {
                errorMessage = "Could not delete that student.";
            }
        } else {
            errorMessage = "Unknown action requested.";
        }

        if (message == null) {
            SessionUtil.setFlashError(request, errorMessage);
        } else {
            SessionUtil.setFlash(request, message);
        }
        response.sendRedirect(request.getContextPath() + "/admin-students.html");
    }

    private int parseId(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}