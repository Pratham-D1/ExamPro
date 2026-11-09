package com.exampro.controller;

import com.exampro.dao.AuditLogDAO;
import com.exampro.model.AuditDashboardStats;
import com.exampro.model.AuditLog;
import com.exampro.model.User;
import com.exampro.util.CsrfUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * AdminAuditServlet – Enterprise Security Audit Dashboard & Log Inspection Controller.
 * ══════════════════════════════════════════════════════════════════════════════════════
 * Provides read-only access to system activity logs, security events, and administrative audit trails.
 */
@WebServlet(name = "AdminAuditServlet", urlPatterns = {"/admin/audit-logs"})
public class AdminAuditServlet extends HttpServlet {

    private static final int PAGE_SIZE = 25;

    private final AuditLogDAO auditDAO = new AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Read query filter parameters
        String search = request.getParameter("search");
        String role = request.getParameter("role");
        String module = request.getParameter("module");
        String action = request.getParameter("action");
        String status = request.getParameter("status");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");

        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isBlank()) {
            try {
                page = Math.max(1, Integer.parseInt(pageStr.trim()));
            } catch (NumberFormatException ignored) {}
        }

        int totalCount = auditDAO.count(search, role, module, action, status, startDate, endDate);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / PAGE_SIZE));
        if (page > totalPages) page = totalPages;

        int offset = (page - 1) * PAGE_SIZE;
        List<AuditLog> logs = auditDAO.findAll(search, role, module, action, status, startDate, endDate, offset, PAGE_SIZE);

        AuditDashboardStats stats = auditDAO.getAuditDashboardStats();

        request.setAttribute("currentUser", user);
        request.setAttribute("stats", stats);
        request.setAttribute("logs", logs);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalCount", totalCount);

        // Retain filter state
        request.setAttribute("search", search != null ? search : "");
        request.setAttribute("selectedRole", role != null ? role : "ALL");
        request.setAttribute("selectedModule", module != null ? module : "ALL");
        request.setAttribute("selectedAction", action != null ? action : "ALL");
        request.setAttribute("selectedStatus", status != null ? status : "ALL");
        request.setAttribute("startDate", startDate != null ? startDate : "");
        request.setAttribute("endDate", endDate != null ? endDate : "");
        request.setAttribute("csrfToken", CsrfUtil.getToken(session));

        request.getRequestDispatcher("/WEB-INF/views/admin/audit_logs.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Audit records are strictly read-only and immutable
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Audit log records cannot be modified or deleted.");
    }
}

