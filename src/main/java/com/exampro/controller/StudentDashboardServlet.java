package com.exampro.controller;

import com.exampro.dao.ExamResultDAO;
import com.exampro.dao.ExaminationDAO;
import com.exampro.dao.StudentDAO;
import com.exampro.model.*;
import com.exampro.util.CsrfUtil;
import com.exampro.util.DatabaseMigrator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

/**
 * StudentDashboardServlet – Controller for the Student Candidate Portal.
 * ═════════════════════════════════════════════════════════════════════
 * Mapped to: /student/dashboard
 * Protected by AuthenticationFilter (requires STUDENT role).
 */
@WebServlet(name = "StudentDashboardServlet", urlPatterns = {"/student/dashboard"})
public class StudentDashboardServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(StudentDashboardServlet.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");

    private final StudentDAO studentDAO = new StudentDAO();
    private final ExaminationDAO examDAO = new ExaminationDAO();
    private final ExamResultDAO resultDAO = new ExamResultDAO();

    @Override
    public void init() throws ServletException {
        try {
            DatabaseMigrator.migrate();
        } catch (Exception e) {
            LOGGER.warning("DatabaseMigrator warning on StudentDashboardServlet init: " + e.getMessage());
        }
    }

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

        request.setAttribute("currentUser", user);
        request.setAttribute("csrfToken", CsrfUtil.getToken(session));
        request.setAttribute("currentDate", LocalDateTime.now().format(DATE_FORMATTER));

        // 1. Profile information
        Student studentProfile = studentDAO.findById(studentId);
        request.setAttribute("studentProfile", studentProfile);

        // 2. Real-time Dashboard Statistics
        StudentDashboardStats stats = studentDAO.getStudentDashboardStats(studentId);
        request.setAttribute("dashboardStats", stats);

        // 3. Available Examinations (active now, not yet taken)
        List<Examination> availableExams = examDAO.findAvailableForStudent(studentId, null, 0, 4);
        request.setAttribute("availableExams", availableExams);

        // 4. Upcoming Examinations
        List<Examination> upcomingExams = examDAO.findUpcomingForStudent(studentId, 4);
        request.setAttribute("upcomingExams", upcomingExams);

        // 5. Recent Completed Results
        List<ExamResult> recentResults = resultDAO.findAllByStudent(studentId, null, 0, 5);
        request.setAttribute("recentResults", recentResults);

        request.getRequestDispatcher("/WEB-INF/views/student/dashboard.jsp").forward(request, response);
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
