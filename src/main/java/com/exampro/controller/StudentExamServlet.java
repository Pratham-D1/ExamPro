package com.exampro.controller;

import com.exampro.dao.ExaminationDAO;
import com.exampro.model.Examination;
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
 * StudentExamServlet – Controller for Student Available Examinations & Schedule Catalog.
 * ═══════════════════════════════════════════════════════════════════════════════════════
 * Mapped to: /student/exams and /student/my-exams
 * Protected by AuthenticationFilter (requires STUDENT role).
 */
@WebServlet(name = "StudentExamServlet", urlPatterns = {"/student/exams", "/student/my-exams"})
public class StudentExamServlet extends HttpServlet {

    private static final int PAGE_SIZE = 12;

    private final ExaminationDAO examDAO = new ExaminationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null || !user.isActive() || !User.ROLE_STUDENT.equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        transferFlashMessages(session, request);

        long studentId = user.getId();
        String path = request.getServletPath();

        String search = request.getParameter("search");
        if (search != null) search = search.trim();

        String filter = request.getParameter("filter");
        if (filter == null || filter.isBlank()) {
            filter = path.contains("my-exams") ? "ALL" : "AVAILABLE";
        } else {
            filter = filter.trim().toUpperCase();
        }

        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isBlank()) {
            try {
                page = Math.max(1, Integer.parseInt(pageStr.trim()));
            } catch (NumberFormatException ignored) {}
        }

        int offset = (page - 1) * PAGE_SIZE;

        List<Examination> examinations;
        int totalCount;

        if ("AVAILABLE".equalsIgnoreCase(filter)) {
            totalCount = examDAO.countAvailableForStudent(studentId, search);
            examinations = examDAO.findAvailableForStudent(studentId, search, offset, PAGE_SIZE);
        } else {
            // My exams catalog
            examinations = examDAO.findMyExamsForStudent(studentId, filter, offset, PAGE_SIZE);
            totalCount = examinations.size(); // Approximate or matching query
        }

        int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / PAGE_SIZE));

        request.setAttribute("currentUser", user);
        request.setAttribute("csrfToken", CsrfUtil.getToken(session));
        request.setAttribute("examinations", examinations);
        request.setAttribute("search", search != null ? search : "");
        request.setAttribute("selectedFilter", filter);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalCount", totalCount);
        request.setAttribute("isMyExams", path.contains("my-exams"));

        request.getRequestDispatcher("/WEB-INF/views/student/exams.jsp").forward(request, response);
    }

    private void transferFlashMessages(HttpSession session, HttpServletRequest request) {
        if (session == null) return;
        String success = (String) session.getAttribute("flashSuccess");
        if (success != null) {
            request.setAttribute("flashSuccess", success);
            session.removeAttribute("flashSuccess");
        }
        String error = (String) session.getAttribute("flashError");
        if (error != null) {
            request.setAttribute("flashError", error);
            session.removeAttribute("flashError");
        }
    }
}

