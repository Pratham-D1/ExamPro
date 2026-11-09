package com.exampro.controller;

import com.exampro.dao.ExamQuestionDAO;
import com.exampro.dao.ExaminationDAO;
import com.exampro.dao.QuestionDAO;
import com.exampro.model.ExamQuestion;
import com.exampro.model.Examination;
import com.exampro.model.Question;
import com.exampro.model.User;
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
 * TeacherExamServlet – Controller for Examination Oversight & Question Assignment.
 * ════════════════════════════════════════════════════════════════════════════════
 * Mapped to: /teacher/examinations
 * Protected by AuthenticationFilter (requires TEACHER role).
 */
@WebServlet(name = "TeacherExamServlet", urlPatterns = {"/teacher/examinations"})
public class TeacherExamServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(TeacherExamServlet.class.getName());
    private static final int PAGE_SIZE = 12;

    private final ExaminationDAO examDAO = new ExaminationDAO();
    private final ExamQuestionDAO examQuestionDAO = new ExamQuestionDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();
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
        String action = request.getParameter("action");
        if (action == null) action = "list";

        if ("manage".equalsIgnoreCase(action)) {
            renderExamManagePage(request, response, session, user, teacherId);
        } else {
            renderExamListPage(request, response, session, user, teacherId);
        }
    }

    private void renderExamListPage(HttpServletRequest request, HttpServletResponse response,
                                    HttpSession session, User user, long teacherId)
            throws ServletException, IOException {

        String search = request.getParameter("search");
        if (search != null) search = search.trim();

        String statusFilter = request.getParameter("status");
        if (statusFilter != null) statusFilter = statusFilter.trim();

        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isBlank()) {
            try {
                page = Math.max(1, Integer.parseInt(pageStr.trim()));
            } catch (NumberFormatException ignored) {}
        }

        int totalExams = examDAO.countByTeacher(teacherId, search, statusFilter);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalExams / PAGE_SIZE));
        if (page > totalPages) page = totalPages;

        int offset = (page - 1) * PAGE_SIZE;
        List<Examination> examinations = examDAO.findAllByTeacher(teacherId, search, statusFilter, offset, PAGE_SIZE);

        request.setAttribute("currentUser", user);
        request.setAttribute("csrfToken", CsrfUtil.getToken(session));
        request.setAttribute("examinations", examinations);
        request.setAttribute("search", search != null ? search : "");
        request.setAttribute("selectedStatus", statusFilter != null ? statusFilter : "ALL");
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalCount", totalExams);

        request.getRequestDispatcher("/WEB-INF/views/teacher/examinations.jsp").forward(request, response);
    }

    private void renderExamManagePage(HttpServletRequest request, HttpServletResponse response,
                                      HttpSession session, User user, long teacherId)
            throws ServletException, IOException {

        String examIdStr = request.getParameter("id");
        long examId;
        try {
            examId = Long.parseLong(examIdStr != null ? examIdStr.trim() : "-1");
        } catch (NumberFormatException e) {
            session.setAttribute("flashError", "Invalid examination ID.");
            response.sendRedirect(request.getContextPath() + "/teacher/examinations");
            return;
        }

        // Validate teacher authorization for this examination
        if (!examDAO.isTeacherAuthorizedForExam(teacherId, examId)) {
            LOGGER.warning("Unauthorized exam access attempt: Teacher " + teacherId + " tried to manage exam " + examId);
            session.setAttribute("flashError", "Access denied: You are not authorized to manage questions for this examination.");
            response.sendRedirect(request.getContextPath() + "/teacher/examinations");
            return;
        }

        Examination exam = examDAO.findById(examId);
        if (exam == null) {
            session.setAttribute("flashError", "Examination not found.");
            response.sendRedirect(request.getContextPath() + "/teacher/examinations");
            return;
        }

        // 1. Fetch currently assigned questions for this exam
        List<ExamQuestion> assignedQuestions = examQuestionDAO.getQuestionsForExam(examId);
        double totalAssignedMarks = examQuestionDAO.sumMarksInExam(examId);
        int assignedCount = assignedQuestions.size();

        exam.setAssignedQuestionsCount(assignedCount);
        exam.setAssignedMarksTotal(totalAssignedMarks);

        // 2. Fetch available questions from question bank for this exam's subject
        String diffFilter = request.getParameter("difficulty");
        String qSearch = request.getParameter("qSearch");
        List<Question> availableQuestions = questionDAO.findAvailableQuestionsForExam(
                examId, exam.getSubjectId(), teacherId, diffFilter, qSearch
        );

        request.setAttribute("currentUser", user);
        request.setAttribute("csrfToken", CsrfUtil.getToken(session));
        request.setAttribute("exam", exam);
        request.setAttribute("assignedQuestions", assignedQuestions);
        request.setAttribute("availableQuestions", availableQuestions);
        request.setAttribute("selectedDifficulty", diffFilter != null ? diffFilter : "ALL");
        request.setAttribute("qSearch", qSearch != null ? qSearch : "");

        request.getRequestDispatcher("/WEB-INF/views/teacher/exam_manage.jsp").forward(request, response);
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

        String token = request.getParameter("csrfToken");
        if (!CsrfUtil.isValidToken(session, token)) {
            session.setAttribute("flashError", "Security session expired. Please refresh and try again.");
            response.sendRedirect(request.getContextPath() + "/teacher/examinations");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "";

        long teacherId = user.getId();

        switch (action.toLowerCase()) {
            case "addquestion" -> handleAddQuestion(request, response, session, teacherId);
            case "removequestion" -> handleRemoveQuestion(request, response, session, teacherId);
            default -> {
                session.setAttribute("flashError", "Unrecognized examination action.");
                response.sendRedirect(request.getContextPath() + "/teacher/examinations");
            }
        }
    }

    private void handleAddQuestion(HttpServletRequest request, HttpServletResponse response,
                                   HttpSession session, long teacherId) throws IOException {

        String examIdStr = request.getParameter("examId");
        String questionIdStr = request.getParameter("questionId");

        long examId;
        long questionId;
        try {
            examId = Long.parseLong(examIdStr != null ? examIdStr.trim() : "-1");
            questionId = Long.parseLong(questionIdStr != null ? questionIdStr.trim() : "-1");
        } catch (NumberFormatException e) {
            session.setAttribute("flashError", "Invalid exam or question ID.");
            response.sendRedirect(request.getContextPath() + "/teacher/examinations");
            return;
        }

        // Authorization checks
        if (!examDAO.isTeacherAuthorizedForExam(teacherId, examId)) {
            LOGGER.warning("Unauthorized question add: Teacher " + teacherId + " on exam " + examId);
            session.setAttribute("flashError", "Access denied: You are not authorized to modify this examination.");
            response.sendRedirect(request.getContextPath() + "/teacher/examinations");
            return;
        }

        Examination exam = examDAO.findById(examId);
        if (exam == null) {
            session.setAttribute("flashError", "Examination not found.");
            response.sendRedirect(request.getContextPath() + "/teacher/examinations");
            return;
        }

        // Prevent modification of ongoing or completed examinations
        if (exam.isOngoing() || exam.isCompleted() || exam.isCancelled()) {
            session.setAttribute("flashError", "Cannot modify questions for an examination that is " + exam.getStatus() + ".");
            response.sendRedirect(request.getContextPath() + "/teacher/examinations?action=manage&id=" + examId);
            return;
        }

        Question question = questionDAO.findById(questionId);
        if (question == null) {
            session.setAttribute("flashError", "Selected question does not exist.");
            response.sendRedirect(request.getContextPath() + "/teacher/examinations?action=manage&id=" + examId);
            return;
        }

        // Subject matching validation
        if (question.getSubjectId() != exam.getSubjectId()) {
            session.setAttribute("flashError", "Question subject (" + question.getSubjectCode() + ") does not match examination subject (" + exam.getSubjectCode() + ").");
            response.sendRedirect(request.getContextPath() + "/teacher/examinations?action=manage&id=" + examId);
            return;
        }

        try {
            boolean added = examQuestionDAO.addQuestionToExam(examId, questionId, question.getMarks());
            if (added) {
                session.setAttribute("flashSuccess", "Question successfully added to examination.");
            } else {
                session.setAttribute("flashError", "Question is already assigned to this examination.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error adding question to exam: " + e.getMessage(), e);
            session.setAttribute("flashError", "Database error occurred while adding question to exam.");
        }

        response.sendRedirect(request.getContextPath() + "/teacher/examinations?action=manage&id=" + examId);
    }

    private void handleRemoveQuestion(HttpServletRequest request, HttpServletResponse response,
                                      HttpSession session, long teacherId) throws IOException {

        String examIdStr = request.getParameter("examId");
        String questionIdStr = request.getParameter("questionId");

        long examId;
        long questionId;
        try {
            examId = Long.parseLong(examIdStr != null ? examIdStr.trim() : "-1");
            questionId = Long.parseLong(questionIdStr != null ? questionIdStr.trim() : "-1");
        } catch (NumberFormatException e) {
            session.setAttribute("flashError", "Invalid exam or question ID.");
            response.sendRedirect(request.getContextPath() + "/teacher/examinations");
            return;
        }

        if (!examDAO.isTeacherAuthorizedForExam(teacherId, examId)) {
            session.setAttribute("flashError", "Access denied: You are not authorized to modify this examination.");
            response.sendRedirect(request.getContextPath() + "/teacher/examinations");
            return;
        }

        Examination exam = examDAO.findById(examId);
        if (exam == null) {
            session.setAttribute("flashError", "Examination not found.");
            response.sendRedirect(request.getContextPath() + "/teacher/examinations");
            return;
        }

        if (exam.isOngoing() || exam.isCompleted() || exam.isCancelled()) {
            session.setAttribute("flashError", "Cannot remove questions from an examination that is " + exam.getStatus() + ".");
            response.sendRedirect(request.getContextPath() + "/teacher/examinations?action=manage&id=" + examId);
            return;
        }

        try {
            boolean removed = examQuestionDAO.removeQuestionFromExam(examId, questionId);
            if (removed) {
                session.setAttribute("flashSuccess", "Question removed from examination.");
            } else {
                session.setAttribute("flashError", "Question was not assigned to this examination.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error removing question from exam: " + e.getMessage(), e);
            session.setAttribute("flashError", "Database error occurred while removing question.");
        }

        response.sendRedirect(request.getContextPath() + "/teacher/examinations?action=manage&id=" + examId);
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

