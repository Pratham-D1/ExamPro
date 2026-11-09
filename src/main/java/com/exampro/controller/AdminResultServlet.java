package com.exampro.controller;

import com.exampro.dao.ExamResultDAO;
import com.exampro.dao.ExaminationDAO;
import com.exampro.dao.SubjectDAO;
import com.exampro.model.AdminResultDashboardStats;
import com.exampro.model.AuditLog;
import com.exampro.model.ExamResult;
import com.exampro.model.Examination;
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
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AdminResultServlet – Comprehensive Controller for Administrator Results Oversight,
 * Filtered Analytics Dashboard, Result Publication Controls, Auditable Marks Adjustments,
 * and Report Exports (CSV & Print).
 * ══════════════════════════════════════════════════════════════════════════════════════
 * Mapped to:
 *   - /admin/results
 *   - /admin/results/export
 *   - /admin/results/print
 */
@WebServlet(name = "AdminResultServlet", urlPatterns = {
        "/admin/results",
        "/admin/results/export",
        "/admin/results/print"
})
public class AdminResultServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AdminResultServlet.class.getName());
    private static final int PAGE_SIZE = 15;

    private final ExamResultDAO resultDAO = new ExamResultDAO();
    private final ExaminationDAO examDAO = new ExaminationDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null || !user.isActive() || !User.ROLE_ADMIN.equalsIgnoreCase(user.getRole())) {
            LOGGER.warning("Unauthorized access attempt to AdminResultServlet from IP: " + request.getRemoteAddr());
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        transferFlashMessages(session, request);

        String path = request.getServletPath();
        if ("/admin/results/export".equalsIgnoreCase(path)) {
            handleExportCsv(request, response, user);
            return;
        } else if ("/admin/results/print".equalsIgnoreCase(path)) {
            handlePrintReport(request, response, user);
            return;
        }

        handleResultsDashboard(request, response, session, user);
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

        String token = request.getParameter("csrfToken");
        if (!CsrfUtil.isValidToken(session, token)) {
            session.setAttribute("flashError", "Security session expired. Please refresh the page and try again.");
            response.sendRedirect(request.getContextPath() + "/admin/results");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "";

        switch (action.toLowerCase()) {
            case "publishexam" -> handlePublishExam(request, response, session, user.getId(), true);
            case "unpublishexam" -> handlePublishExam(request, response, session, user.getId(), false);
            case "correctmarks" -> handleCorrectMarks(request, response, session, user.getId());
            default -> {
                session.setAttribute("flashError", "Unrecognized administrative action.");
                response.sendRedirect(request.getContextPath() + "/admin/results");
            }
        }
    }

    private void handleResultsDashboard(HttpServletRequest request, HttpServletResponse response,
                                        HttpSession session, User user) throws ServletException, IOException {

        // 1. Parse Filter Parameters
        Long examId = parseLong(request.getParameter("examId"));
        Long subjectId = parseLong(request.getParameter("subjectId"));
        Long studentId = parseLong(request.getParameter("studentId"));
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String statusFilter = request.getParameter("status");
        String publishFilter = request.getParameter("publishFilter");
        String search = request.getParameter("search");
        if (search != null) search = search.trim();

        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isBlank()) {
            try {
                page = Math.max(1, Integer.parseInt(pageStr.trim()));
            } catch (NumberFormatException ignored) {}
        }

        // 2. Fetch Results with Pagination
        int totalCount = resultDAO.countResultsAdvanced(examId, subjectId, studentId, startDate, endDate, statusFilter, publishFilter, search);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / PAGE_SIZE));
        if (page > totalPages) page = totalPages;

        int offset = (page - 1) * PAGE_SIZE;
        List<ExamResult> results = resultDAO.findAllAdvanced(examId, subjectId, studentId, startDate, endDate, statusFilter, publishFilter, search, offset, PAGE_SIZE);

        // 3. Aggregate Dashboard Statistics & Performance
        AdminResultDashboardStats stats = resultDAO.getAdminResultDashboardStats(examId, subjectId, studentId, startDate, endDate, statusFilter, publishFilter);

        // 4. Auxiliary dropdown lists
        List<Examination> allExams = examDAO.findAll(null, null, 0, 100);
        List<Subject> allSubjects = subjectDAO.findAll(null);

        request.setAttribute("currentUser", user);
        request.setAttribute("csrfToken", CsrfUtil.getToken(session));
        request.setAttribute("results", results);
        request.setAttribute("stats", stats);
        request.setAttribute("allExams", allExams);
        request.setAttribute("allSubjects", allSubjects);
        request.setAttribute("selectedExamId", examId);
        request.setAttribute("selectedSubjectId", subjectId);
        request.setAttribute("selectedStudentId", studentId);
        request.setAttribute("startDate", startDate != null ? startDate : "");
        request.setAttribute("endDate", endDate != null ? endDate : "");
        request.setAttribute("statusFilter", statusFilter != null ? statusFilter : "ALL");
        request.setAttribute("publishFilter", publishFilter != null ? publishFilter : "ALL");
        request.setAttribute("searchQuery", search != null ? search : "");
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalCount", totalCount);

        request.getRequestDispatcher("/WEB-INF/views/admin/results.jsp").forward(request, response);
    }

    private void handlePublishExam(HttpServletRequest request, HttpServletResponse response,
                                   HttpSession session, long adminId, boolean publish) throws IOException {
        Long examId = parseLong(request.getParameter("examId"));
        if (examId == null || examId <= 0) {
            session.setAttribute("flashError", "Invalid examination specified for publication toggle.");
            response.sendRedirect(request.getContextPath() + "/admin/results");
            return;
        }

        try {
            boolean toggled = resultDAO.toggleExamResultsPublication(examId, publish, adminId);
            if (toggled) {
                if (publish) {
                    Examination exam = examDAO.findById(examId);
                    String examTitle = (exam != null) ? exam.getTitle() : "Examination";
                    com.exampro.dao.NotificationDAO notifDAO = new com.exampro.dao.NotificationDAO();
                    notifDAO.notifyStudentsInExam(
                            examId,
                            "Results Published: " + examTitle,
                            "Your official evaluation for '" + examTitle + "' has been published. Click to view your scorecard.",
                            com.exampro.model.Notification.CAT_RESULT_PUBLISHED,
                            com.exampro.model.Notification.PRIORITY_IMPORTANT,
                            "/student/result-detail?examId=" + examId
                    );
                }
                String msg = publish ? "Examination results successfully published. Candidates can now view their scorecards."
                                     : "Examination results unpublished. Candidate visibility is now restricted.";
                AuditService.logSuccess(request, adminId, "ADMIN", publish ? AuditLog.ACTION_PUBLISH : AuditLog.ACTION_UNPUBLISH,
                        AuditLog.MODULE_RESULT, (publish ? "Published" : "Unpublished") + " results for exam ID " + examId);
                session.setAttribute("flashSuccess", msg);
            } else {
                session.setAttribute("flashError", "Failed to update publication status.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error toggling publication: " + e.getMessage(), e);
            session.setAttribute("flashError", "Database error occurred while updating publication status.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/results?examId=" + examId);
    }

    private void handleCorrectMarks(HttpServletRequest request, HttpServletResponse response,
                                    HttpSession session, long adminId) throws IOException {
        Long resultId = parseLong(request.getParameter("resultId"));
        String newMarksStr = request.getParameter("newMarks");
        String reason = request.getParameter("reason");

        if (resultId == null || resultId <= 0 || newMarksStr == null || newMarksStr.isBlank()) {
            session.setAttribute("flashError", "Invalid result record or marks input.");
            response.sendRedirect(request.getContextPath() + "/admin/results");
            return;
        }

        double newMarks;
        try {
            newMarks = Double.parseDouble(newMarksStr.trim());
        } catch (NumberFormatException e) {
            session.setAttribute("flashError", "Please enter a valid numeric value for marks.");
            response.sendRedirect(request.getContextPath() + "/admin/results");
            return;
        }

        if (reason == null || reason.trim().length() < 5) {
            session.setAttribute("flashError", "A valid audit reason (minimum 5 characters) is required for marks adjustment.");
            response.sendRedirect(request.getContextPath() + "/admin/results");
            return;
        }

        try {
            boolean updated = resultDAO.updateResultMarksWithAudit(resultId, newMarks, reason, adminId);
            if (updated) {
                AuditService.logSuccess(request, adminId, "ADMIN", AuditLog.ACTION_MARKS_CORRECT,
                        AuditLog.MODULE_RESULT, "Admin adjusted marks for result ID " + resultId + " to " + newMarks + ". Reason: " + reason);
                session.setAttribute("flashSuccess", "Marks successfully corrected and logged into the compliance audit trail.");
            } else {
                session.setAttribute("flashError", "Result record not found.");
            }
        } catch (IllegalArgumentException e) {
            session.setAttribute("flashError", e.getMessage());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error adjusting marks: " + e.getMessage(), e);
            session.setAttribute("flashError", "Database error during marks correction.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/results");
    }

    private void handleExportCsv(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {

        Long examId = parseLong(request.getParameter("examId"));
        Long subjectId = parseLong(request.getParameter("subjectId"));
        Long studentId = parseLong(request.getParameter("studentId"));
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String statusFilter = request.getParameter("status");
        String publishFilter = request.getParameter("publishFilter");
        String search = request.getParameter("search");

        List<ExamResult> results = resultDAO.findAllAdvanced(examId, subjectId, studentId, startDate, endDate, statusFilter, publishFilter, search, 0, 10000);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "ExamPro_Results_" + timestamp + ".csv";

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (PrintWriter writer = response.getWriter()) {
            // Write UTF-8 BOM for Microsoft Excel compatibility
            writer.write('\ufeff');

            // Header line
            writer.println("Result ID,Candidate Name,Email,Registration No,Examination Title,Subject Code,Subject Name,Marks Obtained,Total Marks,Percentage,Status,Published,Submitted At");

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
                writer.print(r.isPublished() ? "YES" : "NO");
                writer.print(",");
                writer.print(escapeCsv(r.getFormattedSubmittedAt()));
                writer.println();
            }
            writer.flush();
        }
    }

    private void handlePrintReport(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {

        Long examId = parseLong(request.getParameter("examId"));
        Long subjectId = parseLong(request.getParameter("subjectId"));
        String statusFilter = request.getParameter("status");
        String publishFilter = request.getParameter("publishFilter");
        String search = request.getParameter("search");

        List<ExamResult> results = resultDAO.findAllAdvanced(examId, subjectId, null, null, null, statusFilter, publishFilter, search, 0, 1000);
        AdminResultDashboardStats stats = resultDAO.getAdminResultDashboardStats(examId, subjectId, null, null, null, statusFilter, publishFilter);

        Examination selectedExam = (examId != null && examId > 0) ? examDAO.findById(examId) : null;

        request.setAttribute("currentUser", user);
        request.setAttribute("results", results);
        request.setAttribute("stats", stats);
        request.setAttribute("selectedExam", selectedExam);
        request.setAttribute("generatedDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss")));

        request.getRequestDispatcher("/WEB-INF/views/admin/results_print.jsp").forward(request, response);
    }

    private String escapeCsv(String value) {
        if (value == null) return "\"\"";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private Long parseLong(String val) {
        if (val != null && !val.isBlank()) {
            try {
                return Long.parseLong(val.trim());
            } catch (NumberFormatException ignored) {}
        }
        return null;
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
