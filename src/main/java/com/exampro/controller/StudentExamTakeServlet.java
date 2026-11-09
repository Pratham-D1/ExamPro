package com.exampro.controller;

import com.exampro.dao.ExamAttemptDAO;
import com.exampro.dao.ExaminationDAO;
import com.exampro.model.*;
import com.exampro.util.AuditService;
import com.exampro.util.CsrfUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * StudentExamTakeServlet – Engine for Online Examination Delivery, Question Navigation,
 * Asynchronous Answer Auto-Saving, Server-Side Timing, and Automated Submission.
 * ══════════════════════════════════════════════════════════════════════════════════════
 * Mapped to:
 *   - /student/exam/instructions (GET)
 *   - /student/exam/start        (POST)
 *   - /student/exam/take         (GET)
 *   - /student/exam/save-answer  (POST, JSON API)
 *   - /student/exam/submit       (POST)
 */
@WebServlet(name = "StudentExamTakeServlet", urlPatterns = {
        "/student/exam/instructions",
        "/student/exam/start",
        "/student/exam/take",
        "/student/exam/save-answer",
        "/student/exam/submit"
})
public class StudentExamTakeServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(StudentExamTakeServlet.class.getName());

    private final ExaminationDAO examDAO = new ExaminationDAO();
    private final ExamAttemptDAO attemptDAO = new ExamAttemptDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null || !user.isActive() || !User.ROLE_STUDENT.equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String path = request.getServletPath();

        if ("/student/exam/instructions".equalsIgnoreCase(path)) {
            handleInstructions(request, response, session, user);
        } else if ("/student/exam/take".equalsIgnoreCase(path)) {
            handleTakeExam(request, response, session, user);
        } else {
            response.sendRedirect(request.getContextPath() + "/student/dashboard");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null || !user.isActive() || !User.ROLE_STUDENT.equalsIgnoreCase(user.getRole())) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session expired or unauthorized");
            return;
        }

        String path = request.getServletPath();

        if ("/student/exam/save-answer".equalsIgnoreCase(path)) {
            // Asynchronous Answer Auto-Saving (JSON API)
            handleSaveAnswer(request, response, session, user);
        } else if ("/student/exam/start".equalsIgnoreCase(path)) {
            handleStartExam(request, response, session, user);
        } else if ("/student/exam/submit".equalsIgnoreCase(path)) {
            handleSubmitExam(request, response, session, user);
        } else {
            response.sendRedirect(request.getContextPath() + "/student/dashboard");
        }
    }

    private void handleInstructions(HttpServletRequest request, HttpServletResponse response,
                                    HttpSession session, User user) throws ServletException, IOException {

        String examIdStr = request.getParameter("examId");
        long examId;
        try {
            examId = Long.parseLong(examIdStr != null ? examIdStr.trim() : "-1");
        } catch (NumberFormatException e) {
            session.setAttribute("flashError", "Invalid examination request.");
            response.sendRedirect(request.getContextPath() + "/student/exams");
            return;
        }

        long studentId = user.getId();

        // 1. Check if student already has an active attempt
        ExamAttempt existing = attemptDAO.findAttemptByExamAndStudent(examId, studentId);
        if (existing != null) {
            if (existing.isInProgress()) {
                if (existing.isExpired()) {
                    try {
                        attemptDAO.submitAndEvaluateAttempt(existing.getId(), studentId);
                        session.setAttribute("flashWarning", "Your previous examination session expired and was automatically submitted.");
                        response.sendRedirect(request.getContextPath() + "/student/results");
                        return;
                    } catch (SQLException ignored) {}
                }
                // Resume existing in-progress attempt
                response.sendRedirect(request.getContextPath() + "/student/exam/take?attemptId=" + existing.getId());
                return;
            } else {
                session.setAttribute("flashWarning", "You have already completed this examination.");
                response.sendRedirect(request.getContextPath() + "/student/results");
                return;
            }
        }

        // 2. Validate eligibility to start
        if (!examDAO.isStudentEligibleForExam(studentId, examId)) {
            session.setAttribute("flashError", "You are not eligible to take this examination, or it is currently outside its scheduled time window.");
            response.sendRedirect(request.getContextPath() + "/student/exams");
            return;
        }

        Examination exam = examDAO.findById(examId);
        if (exam == null) {
            session.setAttribute("flashError", "Examination not found.");
            response.sendRedirect(request.getContextPath() + "/student/exams");
            return;
        }

        request.setAttribute("currentUser", user);
        request.setAttribute("csrfToken", CsrfUtil.getToken(session));
        request.setAttribute("exam", exam);

        request.getRequestDispatcher("/WEB-INF/views/student/exam_instructions.jsp").forward(request, response);
    }

    private void handleStartExam(HttpServletRequest request, HttpServletResponse response,
                                 HttpSession session, User user) throws IOException {

        String token = request.getParameter("csrfToken");
        if (!CsrfUtil.isValidToken(session, token)) {
            session.setAttribute("flashError", "Security token expired. Please reload the instructions page.");
            response.sendRedirect(request.getContextPath() + "/student/exams");
            return;
        }

        String examIdStr = request.getParameter("examId");
        long examId;
        try {
            examId = Long.parseLong(examIdStr != null ? examIdStr.trim() : "-1");
        } catch (NumberFormatException e) {
            session.setAttribute("flashError", "Invalid examination identifier.");
            response.sendRedirect(request.getContextPath() + "/student/exams");
            return;
        }

        long studentId = user.getId();

        Examination exam = examDAO.findById(examId);
        if (exam == null) {
            session.setAttribute("flashError", "Examination not found.");
            response.sendRedirect(request.getContextPath() + "/student/exams");
            return;
        }

        try {
            ExamAttempt attempt = attemptDAO.createOrGetAttempt(examId, studentId, exam.getDurationMinutes());
            if (attempt == null) {
                session.setAttribute("flashError", "Unable to initialize examination session. Please contact the administrator.");
                response.sendRedirect(request.getContextPath() + "/student/exams");
                return;
            }

            if (!attempt.isInProgress()) {
                session.setAttribute("flashWarning", "This examination attempt has already concluded.");
                response.sendRedirect(request.getContextPath() + "/student/results");
                return;
            }

            // Successfully started/resumed attempt -> redirect to exam room
            AuditService.logSuccess(request, user, AuditLog.MODULE_EXAM, AuditLog.ACTION_EXAM_START,
                    "Started examination attempt for: " + exam.getTitle() + " (Attempt ID: " + attempt.getId() + ")");
            response.sendRedirect(request.getContextPath() + "/student/exam/take?attemptId=" + attempt.getId());

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error starting exam: " + e.getMessage(), e);
            session.setAttribute("flashError", "Database error starting examination: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/student/exams");
        }
    }

    private void handleTakeExam(HttpServletRequest request, HttpServletResponse response,
                                HttpSession session, User user) throws ServletException, IOException {

        String attemptIdStr = request.getParameter("attemptId");
        long attemptId;
        try {
            attemptId = Long.parseLong(attemptIdStr != null ? attemptIdStr.trim() : "-1");
        } catch (NumberFormatException e) {
            session.setAttribute("flashError", "Invalid examination session.");
            response.sendRedirect(request.getContextPath() + "/student/exams");
            return;
        }

        long studentId = user.getId();
        ExamAttempt attempt = attemptDAO.findAttemptById(attemptId);

        if (attempt == null || attempt.getStudentId() != studentId) {
            LOGGER.warning("Unauthorized take access attempt: Student " + studentId + " on attempt " + attemptId);
            session.setAttribute("flashError", "Access denied: Unauthorized examination session.");
            response.sendRedirect(request.getContextPath() + "/student/exams");
            return;
        }

        // Check if already completed
        if (!attempt.isInProgress()) {
            session.setAttribute("flashWarning", "This examination session has already been finalized.");
            response.sendRedirect(request.getContextPath() + "/student/results");
            return;
        }

        // Server-side timer validation
        if (attempt.isExpired()) {
            LOGGER.info("Attempt " + attemptId + " expired. Auto-evaluating and redirecting to results.");
            try {
                attemptDAO.submitAndEvaluateAttempt(attemptId, studentId);
            } catch (SQLException ignored) {}
            session.setAttribute("flashWarning", "Examination timer expired. Your answers were automatically submitted.");
            response.sendRedirect(request.getContextPath() + "/student/results");
            return;
        }

        // Load sanitized questions (answers excluded for security)
        List<ExamTakeQuestion> questions = attemptDAO.getQuestionsForTake(attemptId, attempt.getExaminationId());

        request.setAttribute("currentUser", user);
        request.setAttribute("csrfToken", CsrfUtil.getToken(session));
        request.setAttribute("attempt", attempt);
        request.setAttribute("questions", questions);
        request.setAttribute("remainingSeconds", attempt.getRemainingSeconds());

        request.getRequestDispatcher("/WEB-INF/views/student/exam_take.jsp").forward(request, response);
    }

    private void handleSaveAnswer(HttpServletRequest request, HttpServletResponse response,
                                  HttpSession session, User user) throws IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String token = request.getParameter("csrfToken");
        if (!CsrfUtil.isValidToken(session, token)) {
            out.write("{\"success\":false,\"message\":\"Security session invalid\"}");
            return;
        }

        String attemptIdStr = request.getParameter("attemptId");
        String questionIdStr = request.getParameter("questionId");
        String selectedOption = request.getParameter("selectedOption");
        String reviewStr = request.getParameter("markedForReview");

        long attemptId;
        long questionId;
        try {
            attemptId = Long.parseLong(attemptIdStr != null ? attemptIdStr.trim() : "-1");
            questionId = Long.parseLong(questionIdStr != null ? questionIdStr.trim() : "-1");
        } catch (NumberFormatException e) {
            out.write("{\"success\":false,\"message\":\"Invalid parameters\"}");
            return;
        }

        Boolean markedForReview = null;
        if (reviewStr != null && !reviewStr.isBlank()) {
            markedForReview = Boolean.parseBoolean(reviewStr.trim());
        }

        try {
            boolean saved = attemptDAO.saveAnswer(attemptId, user.getId(), questionId, selectedOption, markedForReview);
            if (saved) {
                ExamAttempt attempt = attemptDAO.findAttemptById(attemptId);
                long rem = (attempt != null) ? attempt.getRemainingSeconds() : 0;
                out.write("{\"success\":true,\"remainingSeconds\":" + rem + "}");
            } else {
                ExamAttempt attempt = attemptDAO.findAttemptById(attemptId);
                if (attempt != null && (attempt.isExpired() || !attempt.isInProgress())) {
                    out.write("{\"success\":false,\"timeout\":true,\"redirectUrl\":\"" + request.getContextPath() + "/student/results\"}");
                } else {
                    out.write("{\"success\":false,\"message\":\"Save rejected\"}");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving answer: " + e.getMessage(), e);
            out.write("{\"success\":false,\"message\":\"Database error\"}");
        }
    }

    private void handleSubmitExam(HttpServletRequest request, HttpServletResponse response,
                                  HttpSession session, User user) throws IOException {

        String token = request.getParameter("csrfToken");
        if (!CsrfUtil.isValidToken(session, token)) {
            session.setAttribute("flashError", "Security token expired. Please try submitting again.");
            response.sendRedirect(request.getContextPath() + "/student/exams");
            return;
        }

        String attemptIdStr = request.getParameter("attemptId");
        long attemptId;
        try {
            attemptId = Long.parseLong(attemptIdStr != null ? attemptIdStr.trim() : "-1");
        } catch (NumberFormatException e) {
            session.setAttribute("flashError", "Invalid attempt identifier.");
            response.sendRedirect(request.getContextPath() + "/student/exams");
            return;
        }

        long studentId = user.getId();

        try {
            ExamResult result = attemptDAO.submitAndEvaluateAttempt(attemptId, studentId);
            if (result != null) {
                AuditService.logSuccess(request, user, AuditLog.MODULE_EXAM, AuditLog.ACTION_EXAM_SUBMIT,
                        "Submitted examination (Attempt ID: " + attemptId + ", Score: " + result.getMarksObtained() + "/" + result.getTotalMarks() + ")");
                session.setAttribute("flashSuccess", "Examination submitted successfully! Your test has been graded.");
                try {
                    com.exampro.dao.NotificationDAO notifDAO = new com.exampro.dao.NotificationDAO();
                    notifDAO.create(new com.exampro.model.Notification(
                            studentId,
                            "Exam Completed: " + (result.getExamTitle() != null ? result.getExamTitle() : "Examination"),
                            "You submitted '" + (result.getExamTitle() != null ? result.getExamTitle() : "Examination") + "'. Evaluated score: " + result.getMarksObtained() + "/" + result.getTotalMarks() + " (" + Math.round(result.getPercentage()) + "%).",
                            com.exampro.model.Notification.CAT_RESULT_PUBLISHED,
                            com.exampro.model.Notification.PRIORITY_NORMAL,
                            "/student/results"
                    ));
                } catch (Exception ignored) {}
            } else {
                session.setAttribute("flashWarning", "Examination session has already been finalized.");
            }
            response.sendRedirect(request.getContextPath() + "/student/results");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error submitting exam: " + e.getMessage(), e);
            session.setAttribute("flashError", "Error finalizing submission: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/student/exams");
        }
    }
}

