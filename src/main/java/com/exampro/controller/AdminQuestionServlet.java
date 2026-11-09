package com.exampro.controller;

import com.exampro.dao.QuestionDAO;
import com.exampro.dao.SubjectDAO;
import com.exampro.dao.TeacherDAO;
import com.exampro.model.AuditLog;
import com.exampro.model.Question;
import com.exampro.model.Subject;
import com.exampro.model.Teacher;
import com.exampro.model.User;
import com.exampro.util.AuditService;
import com.exampro.util.CsrfUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AdminQuestionServlet – Centralized Administrator Question Bank Management.
 * ══════════════════════════════════════════════════════════════════════════════
 * Mapped to: /admin/questions
 * Protected by AuthenticationFilter (requires ADMIN role).
 *
 * Capabilities:
 * - Cross-institutional Question Bank oversight across all subjects and faculty.
 * - Multi-criteria filtering by Subject, Authoring Faculty, Difficulty, Topic, and Search keywords.
 * - Detailed question inspection (MCQ options, correct answers, explanations).
 * - Safe administrative deletion for questions not assigned to active examinations.
 * - Centralized audit trail recording for security and compliance.
 */
@WebServlet(name = "AdminQuestionServlet", urlPatterns = {"/admin/questions"})
public class AdminQuestionServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AdminQuestionServlet.class.getName());
    private static final int PAGE_SIZE = 15;

    private final QuestionDAO questionDAO = new QuestionDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final TeacherDAO teacherDAO = new TeacherDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null || !user.isActive() || !User.ROLE_ADMIN.equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        transferFlashMessages(session, request);

        // Filters
        String search = request.getParameter("search");
        if (search != null) search = search.trim();

        String difficulty = request.getParameter("difficulty");
        if (difficulty != null) difficulty = difficulty.trim().toUpperCase();

        String topic = request.getParameter("topic");
        if (topic != null) topic = topic.trim();

        String subjectIdStr = request.getParameter("subjectId");
        Long subjectId = null;
        if (subjectIdStr != null && !subjectIdStr.isBlank() && !"ALL".equalsIgnoreCase(subjectIdStr)) {
            try {
                subjectId = Long.parseLong(subjectIdStr.trim());
            } catch (NumberFormatException ignored) {}
        }

        String teacherIdStr = request.getParameter("teacherId");
        Long teacherId = null;
        if (teacherIdStr != null && !teacherIdStr.isBlank() && !"ALL".equalsIgnoreCase(teacherIdStr)) {
            try {
                teacherId = Long.parseLong(teacherIdStr.trim());
            } catch (NumberFormatException ignored) {}
        }

        // Pagination
        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isBlank()) {
            try {
                page = Math.max(1, Integer.parseInt(pageStr.trim()));
            } catch (NumberFormatException ignored) {}
        }

        int totalCount = questionDAO.countAllQuestions(subjectId, teacherId, difficulty, topic, search);
        int totalPages = (int) Math.ceil((double) totalCount / PAGE_SIZE);
        if (totalPages == 0) totalPages = 1;
        if (page > totalPages) page = totalPages;

        int offset = (page - 1) * PAGE_SIZE;
        List<Question> questions = questionDAO.findAllQuestions(subjectId, teacherId, difficulty, topic, search, offset, PAGE_SIZE);

        List<Subject> subjects = subjectDAO.findAll(null);
        List<Teacher> teachers = teacherDAO.findAll(null, 0, 200);

        request.setAttribute("currentUser", user);
        request.setAttribute("csrfToken", CsrfUtil.getToken(session));
        request.setAttribute("questions", questions);
        request.setAttribute("subjects", subjects);
        request.setAttribute("teachers", teachers);
        request.setAttribute("totalCount", totalCount);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("selectedSubjectId", subjectId);
        request.setAttribute("selectedTeacherId", teacherId);
        request.setAttribute("selectedDifficulty", difficulty);
        request.setAttribute("selectedTopic", topic);
        request.setAttribute("search", search);

        request.getRequestDispatcher("/WEB-INF/views/admin/questions.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null || !user.isActive() || !User.ROLE_ADMIN.equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // CSRF Verification
        String token = request.getParameter("csrfToken");
        if (!CsrfUtil.isValidToken(session, token)) {
            LOGGER.warning("CSRF validation failure on /admin/questions for " + user.getEmail());
            session.setAttribute("flashError", "Security session expired. Please refresh and try again.");
            response.sendRedirect(request.getContextPath() + "/admin/questions");
            return;
        }

        String action = request.getParameter("action");
        if ("delete".equalsIgnoreCase(action)) {
            handleDeleteQuestion(request, response, session, user);
        } else {
            session.setAttribute("flashError", "Unrecognized action.");
            response.sendRedirect(request.getContextPath() + "/admin/questions");
        }
    }

    private void handleDeleteQuestion(HttpServletRequest request, HttpServletResponse response,
                                      HttpSession session, User user) throws IOException {

        String questionIdStr = request.getParameter("questionId");
        long questionId;
        try {
            questionId = Long.parseLong(questionIdStr != null ? questionIdStr.trim() : "-1");
        } catch (NumberFormatException e) {
            session.setAttribute("flashError", "Invalid question ID.");
            response.sendRedirect(request.getContextPath() + "/admin/questions");
            return;
        }

        Question question = questionDAO.findById(questionId);
        if (question == null) {
            session.setAttribute("flashError", "Question not found.");
            response.sendRedirect(request.getContextPath() + "/admin/questions");
            return;
        }

        try {
            boolean deleted = questionDAO.adminDeleteQuestion(questionId);
            if (deleted) {
                AuditService.logSuccess(request, user, AuditLog.MODULE_QUESTION, AuditLog.ACTION_DELETE,
                        "Admin permanently deleted question #" + questionId + " (\"" +
                        (question.getQuestionText().length() > 40 ? question.getQuestionText().substring(0, 37) + "..." : question.getQuestionText()) + "\")");
                session.setAttribute("flashSuccess", "Question #" + questionId + " permanently removed from Question Bank.");
            } else {
                session.setAttribute("flashError", "Cannot delete Question #" + questionId + ": It is currently assigned to one or more active or scheduled examinations.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error deleting question " + questionId + ": " + e.getMessage(), e);
            session.setAttribute("flashError", "Database error occurred while deleting the question.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/questions");
    }

    private void transferFlashMessages(HttpSession session, HttpServletRequest request) {
        if (session == null) return;
        String s = (String) session.getAttribute("flashSuccess");
        if (s != null) {
            request.setAttribute("flashSuccess", s);
            session.removeAttribute("flashSuccess");
        }
        String e = (String) session.getAttribute("flashError");
        if (e != null) {
            request.setAttribute("flashError", e);
            session.removeAttribute("flashError");
        }
    }
}
