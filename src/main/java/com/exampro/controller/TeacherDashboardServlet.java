package com.exampro.controller;

import com.exampro.dao.ExaminationDAO;
import com.exampro.dao.QuestionDAO;
import com.exampro.dao.TeacherDAO;
import com.exampro.model.Examination;
import com.exampro.model.Question;
import com.exampro.model.Subject;
import com.exampro.model.TeacherDashboardStats;
import com.exampro.model.User;
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
 * TeacherDashboardServlet – Controller for the Teacher Dashboard.
 * ════════════════════════════════════════════════════════════════
 * Mapped to: /teacher/dashboard
 * Protected by AuthenticationFilter (requires TEACHER role).
 */
@WebServlet(name = "TeacherDashboardServlet", urlPatterns = {"/teacher/dashboard"})
public class TeacherDashboardServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(TeacherDashboardServlet.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");

    private final TeacherDAO teacherDAO = new TeacherDAO();
    private final ExaminationDAO examDAO = new ExaminationDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();

    @Override
    public void init() throws ServletException {
        // Ensure Phase 5 tables are initialized safely
        try {
            DatabaseMigrator.migrate();
        } catch (Exception e) {
            LOGGER.warning("DatabaseMigrator warning on TeacherDashboardServlet init: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        // Defense-in-depth role check
        if (user == null || !user.isActive() || !User.ROLE_TEACHER.equalsIgnoreCase(user.getRole())) {
            LOGGER.warning("Unauthorized access attempt to TeacherDashboardServlet from IP: " + request.getRemoteAddr());
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        transferFlashMessages(session, request);

        // Security tokens and display metadata
        request.setAttribute("currentUser", user);
        request.setAttribute("csrfToken", CsrfUtil.getToken(session));
        request.setAttribute("currentDate", LocalDateTime.now().format(DATE_FORMATTER));

        long teacherId = user.getId();

        // 1. Live Dashboard Statistics
        TeacherDashboardStats stats = teacherDAO.getTeacherDashboardStats(teacherId);
        request.setAttribute("dashboardStats", stats);

        // 2. Assigned Subjects
        List<Subject> assignedSubjects = teacherDAO.getAssignedSubjects(teacherId);
        request.setAttribute("assignedSubjects", assignedSubjects);

        // 3. Upcoming Examinations (assigned to teacher's subjects)
        List<Examination> upcomingExams = examDAO.findAllByTeacher(teacherId, null, "SCHEDULED", 0, 5);
        request.setAttribute("upcomingExams", upcomingExams);

        // 4. Recent Questions in Question Bank
        List<Question> recentQuestions = questionDAO.findAllByTeacher(teacherId, null, null, null, null, 0, 5);
        request.setAttribute("recentQuestions", recentQuestions);

        request.getRequestDispatcher("/WEB-INF/views/teacher/dashboard.jsp").forward(request, response);
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
