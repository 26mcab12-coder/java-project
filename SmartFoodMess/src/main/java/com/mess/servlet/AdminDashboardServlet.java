package com.mess.servlet;

import com.mess.util.SessionUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
/**
 * Serves the static admin dashboard to authenticated administrators.
 */
@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        if (!SessionUtil.requireLogin(request, response, "ADMIN")) {
            return;
        }

        response.sendRedirect(request.getContextPath() + "/admin-dashboard.html");
    }
}