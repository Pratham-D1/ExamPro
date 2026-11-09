package com.exampro.controller;

import com.exampro.dao.QuestionDAO;
import com.exampro.dao.TeacherDAO;
import com.exampro.model.AuditLog;
import com.exampro.model.Question;
import com.exampro.model.QuestionOption;
import com.exampro.model.Subject;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TeacherQuestionServlet – Controller for Question Bank & MCQ Management.
 * ═══════════════════════════════════════════════════════════════════════
 * Mapped to: /teacher/questions
 * Protected by AuthenticationFilter (requires TEACHER role).
 */
@WebServlet(name = "TeacherQuestionServlet", urlPatterns = {"/teacher/questions"})
public class TeacherQuestionServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(TeacherQuestionServlet.class.getName());
    private static final int PAGE_SIZE = 12;

    private final QuestionDAO questionDAO = new QuestionDAO();
    private final TeacherDAO teacherDAO = new TeacherDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null || !user.isActive() || !User.ROLE_TEACHER.equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        transferFlashMessages(session, request);

        long teacherId = user.getId();

        // 1. Query filters
        String search = request.getParameter("search");
        if (search != null) search = search.trim();

        String difficulty = request.getParameter("difficulty");
        if (difficulty != null) difficulty = difficulty.trim().toUpperCase();

        String topic = request.getParameter("topic");
        if (topic != null) topic = topic.trim();

        Long subjectId = null;
        String subjectIdStr = request.getParameter("subjectId");
        if (subjectIdStr != null && !subjectIdStr.isBlank()) {
            try {
                subjectId = Long.parseLong(subjectIdStr.trim());
            } catch (NumberFormatException ignored) {}
        }

        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isBlank()) {
            try {
                page = Math.max(1, Integer.parseInt(pageStr.trim()));
            } catch (NumberFormatException ignored) {}
        }

        // 2. Fetch Questions
        int totalQuestions = questionDAO.countByTeacher(teacherId, subjectId, difficulty, topic, search);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalQuestions / PAGE_SIZE));
        if (page > totalPages) page = totalPages;

        int offset = (page - 1) * PAGE_SIZE;
        List<Question> questions = questionDAO.findAllByTeacher(teacherId, subjectId, difficulty, topic, search, offset, PAGE_SIZE);

        // 3. Fetch Assigned Subjects (for filters and creation modal)
        List<Subject> assignedSubjects = teacherDAO.getAssignedSubjects(teacherId);

        request.setAttribute("currentUser", user);
        request.setAttribute("csrfToken", CsrfUtil.getToken(session));
        request.setAttribute("questions", questions);
        request.setAttribute("assignedSubjects", assignedSubjects);
        request.setAttribute("search", search != null ? search : "");
        request.setAttribute("selectedSubjectId", subjectId);
        request.setAttribute("selectedDifficulty", difficulty);
        request.setAttribute("selectedTopic", topic != null ? topic : "");
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalCount", totalQuestions);

        request.getRequestDispatcher("/WEB-INF/views/teacher/questions.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null || !user.isActive() || !User.ROLE_TEACHER.equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // CSRF validation
        String token = request.getParameter("csrfToken");
        if (!CsrfUtil.isValidToken(session, token)) {
            LOGGER.warning("CSRF validation failed for user " + user.getEmail() + " on /teacher/questions");
            session.setAttribute("flashError", "Security session expired. Please refresh and try again.");
            response.sendRedirect(request.getContextPath() + "/teacher/questions");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "";

        switch (action.toLowerCase()) {
            case "create" -> handleCreateQuestion(request, response, session, user);
            case "update" -> handleUpdateQuestion(request, response, session, user);
            case "delete" -> handleDeleteQuestion(request, response, session, user);
            default -> {
                session.setAttribute("flashError", "Unrecognized question action.");
                response.sendRedirect(request.getContextPath() + "/teacher/questions");
            }
        }
    }

    private void handleCreateQuestion(HttpServletRequest request, HttpServletResponse response,
                                      HttpSession session, User user) throws IOException {

        long teacherId = user.getId();
        String subjectIdStr = request.getParameter("subjectId");
        String questionText = request.getParameter("questionText");
        String topic = request.getParameter("topic");
        String difficulty = request.getParameter("difficulty");
        String marksStr = request.getParameter("marks");
        String optionA = request.getParameter("optionA");
        String optionB = request.getParameter("optionB");
        String optionC = request.getParameter("optionC");
        String optionD = request.getParameter("optionD");
        String correctOption = request.getParameter("correctOption");
        String explanation = request.getParameter("explanation");

        // 1. Validation
        long subjectId;
        try {
            subjectId = Long.parseLong(subjectIdStr != null ? subjectIdStr.trim() : "-1");
        } catch (NumberFormatException e) {
            session.setAttribute("flashError", "Please select a valid subject.");
            response.sendRedirect(request.getContextPath() + "/teacher/questions");
            return;
        }

        // 2. Subject authorization check
        if (!teacherDAO.isTeacherAssignedToSubject(teacherId, subjectId)) {
            LOGGER.warning("Subject authorization violation: Teacher " + teacherId + " attempted to create question for unassigned subject " + subjectId);
            session.setAttribute("flashError", "Access denied: You can only create questions for subjects assigned to you by administrators.");
            response.sendRedirect(request.getContextPath() + "/teacher/questions");
            return;
        }

        if (questionText == null || questionText.isBlank()) {
            session.setAttribute("flashError", "Question text cannot be empty.");
            response.sendRedirect(request.getContextPath() + "/teacher/questions");
            return;
        }

        if (optionA == null || optionA.isBlank() ||
            optionB == null || optionB.isBlank() ||
            optionC == null || optionC.isBlank() ||
            optionD == null || optionD.isBlank()) {
            session.setAttribute("flashError", "All four MCQ options (A, B, C, D) are required.");
            response.sendRedirect(request.getContextPath() + "/teacher/questions");
            return;
        }

        if (correctOption == null || !Arrays.asList("A", "B", "C", "D").contains(correctOption.trim().toUpperCase())) {
            session.setAttribute("flashError", "Please designate one valid correct option (A, B, C, or D).");
            response.sendRedirect(request.getContextPath() + "/teacher/questions");
            return;
        }
        correctOption = correctOption.trim().toUpperCase();

        double marks = 1.0;
        try {
            if (marksStr != null && !marksStr.isBlank()) {
                marks = Double.parseDouble(marksStr.trim());
            }
            if (marks <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            session.setAttribute("flashError", "Marks must be a positive number.");
            response.sendRedirect(request.getContextPath() + "/teacher/questions");
            return;
        }

        if (difficulty == null || !Arrays.asList("EASY", "MEDIUM", "HARD").contains(difficulty.trim().toUpperCase())) {
            difficulty = "MEDIUM";
        } else {
            difficulty = difficulty.trim().toUpperCase();
        }

        // 2. Build Entities
        Question q = new Question();
        q.setSubjectId(subjectId);
        q.setTeacherId(teacherId);
        q.setQuestionText(questionText.trim());
        q.setTopic(topic != null && !topic.isBlank() ? topic.trim() : "General");
        q.setDifficulty(difficulty);
        q.setMarks(marks);
        q.setQuestionType(Question.TYPE_MCQ);
        q.setExplanation(explanation != null && !explanation.isBlank() ? explanation.trim() : null);

        List<QuestionOption> options = new ArrayList<>();
        options.add(new QuestionOption("A", optionA.trim(), "A".equals(correctOption)));
        options.add(new QuestionOption("B", optionB.trim(), "B".equals(correctOption)));
        options.add(new QuestionOption("C", optionC.trim(), "C".equals(correctOption)));
        options.add(new QuestionOption("D", optionD.trim(), "D".equals(correctOption)));

        try {
            boolean created = questionDAO.createQuestion(q, options);
            if (created) {
                AuditService.logSuccess(request, user, AuditLog.MODULE_QUESTION, AuditLog.ACTION_CREATE,
                        "Created question #" + q.getId() + " (" + q.getTopic() + ", " + q.getDifficulty() + ")");
                session.setAttribute("flashSuccess", "New question successfully added to your Question Bank.");
            } else {
                session.setAttribute("flashError", "Failed to persist question. Please review your input.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error adding question: " + e.getMessage(), e);
            session.setAttribute("flashError", "Database error occurred while saving the question: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/teacher/questions");
    }

    private void handleUpdateQuestion(HttpServletRequest request, HttpServletResponse response,
                                      HttpSession session, User user) throws IOException {

        long teacherId = user.getId();
        String questionIdStr = request.getParameter("questionId");
        String subjectIdStr = request.getParameter("subjectId");
        String questionText = request.getParameter("questionText");
        String topic = request.getParameter("topic");
        String difficulty = request.getParameter("difficulty");
        String marksStr = request.getParameter("marks");
        String optionA = request.getParameter("optionA");
        String optionB = request.getParameter("optionB");
        String optionC = request.getParameter("optionC");
        String optionD = request.getParameter("optionD");
        String correctOption = request.getParameter("correctOption");
        String explanation = request.getParameter("explanation");

        long questionId;
        long subjectId;
        try {
            questionId = Long.parseLong(questionIdStr != null ? questionIdStr.trim() : "-1");
            subjectId = Long.parseLong(subjectIdStr != null ? subjectIdStr.trim() : "-1");
        } catch (NumberFormatException e) {
            session.setAttribute("flashError", "Invalid question or subject ID.");
            response.sendRedirect(request.getContextPath() + "/teacher/questions");
            return;
        }

        // Ownership and authorization verification
        Question existing = questionDAO.findById(questionId);
        if (existing == null || existing.getTeacherId() != teacherId) {
            LOGGER.warning("Ownership violation: Teacher " + teacherId + " attempted to edit question " + questionId);
            session.setAttribute("flashError", "Access denied: You can only edit questions authored by you.");
            response.sendRedirect(request.getContextPath() + "/teacher/questions");
            return;
        }

        if (!teacherDAO.isTeacherAssignedToSubject(teacherId, subjectId)) {
            session.setAttribute("flashError", "Access denied: Selected subject is not assigned to you.");
            response.sendRedirect(request.getContextPath() + "/teacher/questions");
            return;
        }

        if (questionText == null || questionText.isBlank()) {
            session.setAttribute("flashError", "Question statement cannot be empty.");
            response.sendRedirect(request.getContextPath() + "/teacher/questions");
            return;
        }

        if (optionA == null || optionA.isBlank() ||
            optionB == null || optionB.isBlank() ||
            optionC == null || optionC.isBlank() ||
            optionD == null || optionD.isBlank()) {
            session.setAttribute("flashError", "All four options (A, B, C, D) are required.");
            response.sendRedirect(request.getContextPath() + "/teacher/questions");
            return;
        }

        if (correctOption == null || !Arrays.asList("A", "B", "C", "D").contains(correctOption.trim().toUpperCase())) {
            session.setAttribute("flashError", "Please designate one valid correct option (A, B, C, or D).");
            response.sendRedirect(request.getContextPath() + "/teacher/questions");
            return;
        }
        correctOption = correctOption.trim().toUpperCase();

        double marks = 1.0;
        try {
            if (marksStr != null && !marksStr.isBlank()) {
                marks = Double.parseDouble(marksStr.trim());
            }
            if (marks <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            session.setAttribute("flashError", "Marks must be a positive number.");
            response.sendRedirect(request.getContextPath() + "/teacher/questions");
            return;
        }

        if (difficulty == null || !Arrays.asList("EASY", "MEDIUM", "HARD").contains(difficulty.trim().toUpperCase())) {
            difficulty = "MEDIUM";
        } else {
            difficulty = difficulty.trim().toUpperCase();
        }

        existing.setSubjectId(subjectId);
        existing.setQuestionText(questionText.trim());
        existing.setTopic(topic != null && !topic.isBlank() ? topic.trim() : "General");
        existing.setDifficulty(difficulty);
        existing.setMarks(marks);
        existing.setExplanation(explanation != null && !explanation.isBlank() ? explanation.trim() : null);

        List<QuestionOption> options = new ArrayList<>();
        options.add(new QuestionOption("A", optionA.trim(), "A".equals(correctOption)));
        options.add(new QuestionOption("B", optionB.trim(), "B".equals(correctOption)));
        options.add(new QuestionOption("C", optionC.trim(), "C".equals(correctOption)));
        options.add(new QuestionOption("D", optionD.trim(), "D".equals(correctOption)));

        try {
            boolean updated = questionDAO.updateQuestion(existing, options);
            if (updated) {
                AuditService.logSuccess(request, user, AuditLog.MODULE_QUESTION, AuditLog.ACTION_UPDATE,
                        "Updated question #" + questionId + " (" + existing.getTopic() + ", " + existing.getDifficulty() + ")");
                session.setAttribute("flashSuccess", "Question successfully updated.");
            } else {
                session.setAttribute("flashError", "Failed to update question.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error updating question " + questionId + ": " + e.getMessage(), e);
            session.setAttribute("flashError", "Database error occurred while updating the question.");
        }

        response.sendRedirect(request.getContextPath() + "/teacher/questions");
    }

    private void handleDeleteQuestion(HttpServletRequest request, HttpServletResponse response,
                                      HttpSession session, User user) throws IOException {

        long teacherId = user.getId();
        String questionIdStr = request.getParameter("questionId");
        long questionId;
        try {
            questionId = Long.parseLong(questionIdStr != null ? questionIdStr.trim() : "-1");
        } catch (NumberFormatException e) {
            session.setAttribute("flashError", "Invalid question ID.");
            response.sendRedirect(request.getContextPath() + "/teacher/questions");
            return;
        }

        Question existing = questionDAO.findById(questionId);
        if (existing == null || existing.getTeacherId() != teacherId) {
            session.setAttribute("flashError", "Access denied: You can only delete questions authored by you.");
            response.sendRedirect(request.getContextPath() + "/teacher/questions");
            return;
        }

        try {
            boolean deleted = questionDAO.deleteQuestion(questionId, teacherId);
            if (deleted) {
                AuditService.logSuccess(request, user, AuditLog.MODULE_QUESTION, AuditLog.ACTION_DELETE,
                        "Deleted question #" + questionId);
                session.setAttribute("flashSuccess", "Question permanently removed from your question bank.");
            } else {
                session.setAttribute("flashError", "Cannot delete question: It is currently assigned to one or more active or scheduled examinations.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error deleting question " + questionId + ": " + e.getMessage(), e);
            session.setAttribute("flashError", "Cannot delete question: It is referenced by examinations.");
        }

        response.sendRedirect(request.getContextPath() + "/teacher/questions");
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

