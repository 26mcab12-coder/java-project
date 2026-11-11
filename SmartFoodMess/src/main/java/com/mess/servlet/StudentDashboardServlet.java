package com.mess.servlet;

import com.mess.util.SessionUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
/**
 * Serves the static student dashboard to authenticated students.
 */
@WebServlet("/student/dashboard")
public class StudentDashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        if (!SessionUtil.requireLogin(request, response, "STUDENT")) {
            return;
        }

        response.sendRedirect(request.getContextPath() + "/student-dashboard.html");
    }
}