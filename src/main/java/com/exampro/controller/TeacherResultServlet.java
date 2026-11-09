package com.exampro.controller;

import com.exampro.dao.ExamResultDAO;
import com.exampro.dao.ExaminationDAO;
import com.exampro.model.ExamResult;
import com.exampro.model.Examination;
import com.exampro.model.QuestionPerformance;
import com.exampro.model.User;
import com.exampro.util.CsrfUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

/**
 * TeacherResultServlet – Controller for Faculty Examination Analytics,
 * Question-wise Item Analysis, Performance Metrics, and Authorized Export (CSV & Print).
 * ══════════════════════════════════════════════════════════════════════════════════════
 * Mapped to:
 *   - /teacher/results
 *   - /teacher/results/export
 *   - /teacher/results/print
 */
@WebServlet(name = "TeacherResultServlet", urlPatterns = {
        "/teacher/results",
        "/teacher/results/export",
        "/teacher/results/print"
})
public class TeacherResultServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(TeacherResultServlet.class.getName());
    private static final int PAGE_SIZE = 15;

    private final ExamResultDAO resultDAO = new ExamResultDAO();
    private final ExaminationDAO examDAO = new ExaminationDAO();

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

        String path = request.getServletPath();
        if ("/teacher/results/export".equalsIgnoreCase(path)) {
            handleExportCsv(request, response, user);
            return;
        } else if ("/teacher/results/print".equalsIgnoreCase(path)) {
            handlePrintReport(request, response, user);
            return;
        }

        handleTeacherResultsDashboard(request, response, session, user);
    }

    private void handleTeacherResultsDashboard(HttpServletRequest request, HttpServletResponse response,
                                               HttpSession session, User user) throws ServletException, IOException {

        long teacherId = user.getId();

        String search = request.getParameter("search");
        if (search != null) search = search.trim();

        Long examId = null;
        String examIdStr = request.getParameter("examId");
        if (examIdStr != null && !examIdStr.isBlank()) {
            try {
                examId = Long.parseLong(examIdStr.trim());
            } catch (NumberFormatException ignored) {}
        }

        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isBlank()) {
            try {
                page = Math.max(1, Integer.parseInt(pageStr.trim()));
            } catch (NumberFormatException ignored) {}
        }

        // Enforce strict teacher-subject/exam authorization
        if (examId != null && examId > 0) {
            if (!examDAO.isTeacherAuthorizedForExam(teacherId, examId)) {
                LOGGER.warning("Unauthorized result inspection attempt: Teacher " + teacherId + " on exam " + examId);
                session.setAttribute("flashError", "You are not authorized to view results for that examination.");
                examId = null; // Reset to all authorized exams
            }
        }

        int totalCount = resultDAO.countByTeacher(teacherId, examId, search);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / PAGE_SIZE));
        if (page > totalPages) page = totalPages;

        int offset = (page - 1) * PAGE_SIZE;
        List<ExamResult> results = resultDAO.findAllByTeacher(teacherId, examId, search, offset, PAGE_SIZE);

        // Performance summary metrics
        ExamResultDAO.ResultSummary summary = resultDAO.getPerformanceSummaryByTeacher(teacherId, examId);

        // Question-wise item analysis if a specific exam is selected
        List<QuestionPerformance> questionPerformances = null;
        Examination selectedExam = null;
        if (examId != null && examId > 0) {
            questionPerformances = resultDAO.getQuestionPerformanceByExam(examId);
            selectedExam = examDAO.findById(examId);
        }

        // List of teacher's examinations for the dropdown filter
        List<Examination> teacherExams = examDAO.findAllByTeacher(teacherId, null, null, 0, 100);

        request.setAttribute("currentUser", user);
        request.setAttribute("csrfToken", CsrfUtil.getToken(session));
        request.setAttribute("results", results);
        request.setAttribute("teacherExams", teacherExams);
        request.setAttribute("selectedExam", selectedExam);
        request.setAttribute("summary", summary);
        request.setAttribute("questionPerformances", questionPerformances);
        request.setAttribute("search", search != null ? search : "");
        request.setAttribute("selectedExamId", examId);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalCount", totalCount);

        request.getRequestDispatcher("/WEB-INF/views/teacher/results.jsp").forward(request, response);
    }

    private void handleExportCsv(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {

        long teacherId = user.getId();
        Long examId = null;
        String examIdStr = request.getParameter("examId");
        if (examIdStr != null && !examIdStr.isBlank()) {
            try {
                examId = Long.parseLong(examIdStr.trim());
            } catch (NumberFormatException ignored) {}
        }

        if (examId != null && examId > 0) {
            if (!examDAO.isTeacherAuthorizedForExam(teacherId, examId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized exam export.");
                return;
            }
        }

        List<ExamResult> results = resultDAO.findAllByTeacher(teacherId, examId, null, 0, 10000);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "Faculty_Results_" + timestamp + ".csv";

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (PrintWriter writer = response.getWriter()) {
            writer.write('\ufeff'); // UTF-8 BOM
            writer.println("Result ID,Candidate Name,Email,Registration No,Examination Title,Subject Code,Subject Name,Marks Obtained,Total Marks,Percentage,Status,Submission Date");

            for (ExamResult r : results) {
                writer.print(r.getId());
                writer.print(",");
                writer.print(escapeCsv(r.getStudentName()));
                writer.print(",");
                writer.print(escapeCsv(r.getStudentEmail()));
                writer.print(",");
                writer.print(escapeCsv(r.getRegistrationNo()));
                writer.print(",");
                writer.print(escapeCsv(r.getExamTitle()));
                writer.print(",");
                writer.print(escapeCsv(r.getSubjectCode()));
                writer.print(",");
                writer.print(escapeCsv(r.getSubjectName()));
                writer.print(",");
                writer.print(r.getMarksObtained());
                writer.print(",");
                writer.print(r.getTotalMarks());
                writer.print(",");
                writer.print(r.getPercentage());
                writer.print(",");
                writer.print(escapeCsv(r.getStatus()));
                writer.print(",");
                writer.print(escapeCsv(r.getFormattedSubmittedAt()));
                writer.println();
            }
            writer.flush();
        }
    }

    private void handlePrintReport(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {

        long teacherId = user.getId();
        Long examId = null;
        String examIdStr = request.getParameter("examId");
        if (examIdStr != null && !examIdStr.isBlank()) {
            try {
                examId = Long.parseLong(examIdStr.trim());
            } catch (NumberFormatException ignored) {}
        }

        if (examId != null && examId > 0) {
            if (!examDAO.isTeacherAuthorizedForExam(teacherId, examId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized print request.");
                return;
            }
        }

        List<ExamResult> results = resultDAO.findAllByTeacher(teacherId, examId, null, 0, 1000);
        ExamResultDAO.ResultSummary summary = resultDAO.getPerformanceSummaryByTeacher(teacherId, examId);
        List<QuestionPerformance> questionPerformances = (examId != null && examId > 0)
                ? resultDAO.getQuestionPerformanceByExam(examId) : null;
        Examination selectedExam = (examId != null && examId > 0) ? examDAO.findById(examId) : null;

        request.setAttribute("currentUser", user);
        request.setAttribute("results", results);
        request.setAttribute("summary", summary);
        request.setAttribute("selectedExam", selectedExam);
        request.setAttribute("questionPerformances", questionPerformances);
        request.setAttribute("generatedDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss")));

        request.getRequestDispatcher("/WEB-INF/views/teacher/results_print.jsp").forward(request, response);
    }

    private String escapeCsv(String value) {
        if (value == null) return "\"\"";
        return "\"" + value.replace("\"", "\"\"") + "\"";
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
