package com.exampro.controller;

import com.exampro.dao.ExamAttemptDAO;
import com.exampro.dao.ExamResultDAO;
import com.exampro.model.ExamAttempt;
import com.exampro.model.ExamResult;
import com.exampro.model.StudentAnswer;
import com.exampro.model.StudentPerformanceAnalytics;
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
 * StudentResultServlet – Handles student examination scorecards, performance analytics,
 * and printable result generation.
 * ═════════════════════════════════════════════════════════════════════════════════
 * Mapped to:
 *   - /student/results        (Summary list of past completed examinations)
 *   - /student/result-detail  (Comprehensive question-by-question review & scorecard)
 *   - /student/analytics      (Visual performance analytics, trends, and weak area analysis)
 *   - /student/result/print   (High-definition printable scorecard)
 */
@WebServlet(name = "StudentResultServlet", urlPatterns = {
        "/student/results",
        "/student/result-detail",
        "/student/analytics",
        "/student/result/print"
})
public class StudentResultServlet extends HttpServlet {

    private static final int PAGE_SIZE = 10;

    private final ExamResultDAO examResultDAO = new ExamResultDAO();
    private final ExamAttemptDAO examAttemptDAO = new ExamAttemptDAO();

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

        String path = request.getServletPath();
        switch (path) {
            case "/student/result-detail" -> handleResultDetail(request, response, user, false);
            case "/student/result/print" -> handleResultDetail(request, response, user, true);
            case "/student/analytics" -> handleStudentAnalytics(request, response, user);
            default -> handleResultsList(request, response, user);
        }
    }

    private void handleResultsList(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {

        long studentId = user.getId();
        String search = request.getParameter("search");
        if (search != null) search = search.trim();

        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isBlank()) {
            try {
                page = Math.max(1, Integer.parseInt(pageStr.trim()));
            } catch (NumberFormatException ignored) {}
        }

        int totalCount = examResultDAO.countByStudent(studentId, search);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / PAGE_SIZE));
        if (page > totalPages) page = totalPages;

        int offset = (page - 1) * PAGE_SIZE;
        List<ExamResult> results = examResultDAO.findAllByStudent(studentId, search, offset, PAGE_SIZE);

        request.setAttribute("currentUser", user);
        request.setAttribute("results", results);
        request.setAttribute("search", search != null ? search : "");
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalCount", totalCount);
        request.setAttribute("csrfToken", CsrfUtil.getToken(request.getSession(false)));

        request.getRequestDispatcher("/WEB-INF/views/student/results.jsp").forward(request, response);
    }

    private void handleResultDetail(HttpServletRequest request, HttpServletResponse response,
                                    User user, boolean isPrintView)
            throws ServletException, IOException {

        long studentId = user.getId();
        Long examId = null;
        Long attemptId = null;

        String examIdStr = request.getParameter("examId");
        if (examIdStr != null && !examIdStr.isBlank()) {
            try {
                examId = Long.parseLong(examIdStr.trim());
            } catch (NumberFormatException ignored) {}
        }

        String attemptIdStr = request.getParameter("attemptId");
        if (attemptIdStr != null && !attemptIdStr.isBlank()) {
            try {
                attemptId = Long.parseLong(attemptIdStr.trim());
            } catch (NumberFormatException ignored) {}
        }

        ExamAttempt attempt = null;
        if (attemptId != null && attemptId > 0) {
            attempt = examAttemptDAO.findAttemptById(attemptId, studentId);
            if (attempt != null) {
                examId = attempt.getExaminationId();
            }
        }

        if (examId == null || examId <= 0) {
            request.getSession().setAttribute("flashError", "Invalid examination specified for result review.");
            response.sendRedirect(request.getContextPath() + "/student/results");
            return;
        }

        // Strict Publication Check: Do not expose unpublished results to student
        if (!examResultDAO.isExamResultsPublished(examId)) {
            request.getSession().setAttribute("flashWarning", "Results for this examination are currently pending administrative review and publication.");
            response.sendRedirect(request.getContextPath() + "/student/results");
            return;
        }

        ExamResult result = examResultDAO.findByExamAndStudent(examId, studentId);
        if (result == null) {
            request.getSession().setAttribute("flashWarning", "No evaluated result found for this examination yet.");
            response.sendRedirect(request.getContextPath() + "/student/results");
            return;
        }

        if (attempt == null) {
            attempt = examAttemptDAO.findAttemptByExamAndStudent(examId, studentId);
        }

        List<StudentAnswer> reviewAnswers = null;
        int correctCount = 0;
        int incorrectCount = 0;
        int unattemptedCount = 0;

        if (attempt != null) {
            reviewAnswers = examAttemptDAO.getDetailedReview(attempt.getId(), studentId);
            for (StudentAnswer a : reviewAnswers) {
                if (a.getSelectedOptionKey() == null || a.getSelectedOptionKey().isBlank()) {
                    unattemptedCount++;
                } else if (Boolean.TRUE.equals(a.getCorrect())) {
                    correctCount++;
                } else {
                    incorrectCount++;
                }
            }
        }

        request.setAttribute("currentUser", user);
        request.setAttribute("result", result);
        request.setAttribute("attempt", attempt);
        request.setAttribute("reviewAnswers", reviewAnswers);
        request.setAttribute("correctCount", correctCount);
        request.setAttribute("incorrectCount", incorrectCount);
        request.setAttribute("unattemptedCount", unattemptedCount);
        request.setAttribute("csrfToken", CsrfUtil.getToken(request.getSession(false)));

        if (isPrintView) {
            request.getRequestDispatcher("/WEB-INF/views/student/result_print.jsp").forward(request, response);
        } else {
            request.getRequestDispatcher("/WEB-INF/views/student/result_detail.jsp").forward(request, response);
        }
    }

    private void handleStudentAnalytics(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {

        long studentId = user.getId();
        StudentPerformanceAnalytics analytics = examResultDAO.getStudentPerformanceAnalytics(studentId);

        request.setAttribute("currentUser", user);
        request.setAttribute("analytics", analytics);
        request.setAttribute("csrfToken", CsrfUtil.getToken(request.getSession(false)));

        request.getRequestDispatcher("/WEB-INF/views/student/analytics.jsp").forward(request, response);
    }

    private void transferFlashMessages(HttpSession session, HttpServletRequest request) {
        if (session == null) return;
        String[] flashKeys = {"flashSuccess", "flashError", "flashWarning", "flashInfo"};
        for (String key : flashKeys) {
            Object val = session.getAttribute(key);
            if (val != null) {
                request.setAttribute(key, val);
                session.removeAttribute(key);
            }
        }
    }
}
