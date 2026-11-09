package com.exampro.controller;

import com.exampro.dao.ExaminationDAO;
import com.exampro.dao.SubjectDAO;
import com.exampro.model.Examination;
import com.exampro.model.Subject;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AdminExamServlet – Controller for Examination Creation, Scheduling, & Lifecycle Management.
 * Mapped to: /admin/examinations
 */
@WebServlet(name = "AdminExamServlet", urlPatterns = {"/admin/examinations"})
public class AdminExamServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AdminExamServlet.class.getName());
    private static final int PAGE_SIZE = 12;
    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final ExaminationDAO examDAO = new ExaminationDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();

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

        int totalCount = examDAO.countExaminations(search, statusFilter);
        int totalPages = (int) Math.ceil((double) totalCount / PAGE_SIZE);
        if (totalPages < 1) totalPages = 1;
        if (page > totalPages) page = totalPages;

        int offset = (page - 1) * PAGE_SIZE;
        List<Examination> examinations = examDAO.findAll(search, statusFilter, offset, PAGE_SIZE);
        List<Subject> allSubjects = subjectDAO.findAll(null);

        request.setAttribute("currentUser", user);
        request.setAttribute("csrfToken", CsrfUtil.getToken(session));
        request.setAttribute("examinations", examinations);
        request.setAttribute("allSubjects", allSubjects);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalCount", totalCount);
        request.setAttribute("searchQuery", search != null ? search : "");
        request.setAttribute("statusFilter", statusFilter != null ? statusFilter : "ALL");

        request.getRequestDispatcher("/WEB-INF/views/admin/examinations.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null || !user.isActive() || !User.ROLE_ADMIN.equalsIgnoreCase(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: Administrator privileges required.");
            return;
        }

        if (!CsrfUtil.isValidToken(request)) {
            LOGGER.warning("CSRF token validation failed on Exam POST from IP: " + request.getRemoteAddr());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing security token.");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "";

        switch (action.toLowerCase()) {
            case "create" -> handleCreateExam(request, response, session, user);
            case "edit" -> handleEditExam(request, response, session);
            case "publish" -> handlePublishExam(request, response, session);
            case "cancel" -> handleCancelExam(request, response, session);
            default -> {
                session.setAttribute("flashError", "Unrecognized examination action requested.");
                response.sendRedirect(request.getContextPath() + "/admin/examinations");
            }
        }
    }

    private void handleCreateExam(HttpServletRequest request, HttpServletResponse response,
                                  HttpSession session, User user) throws IOException {

        String subjectIdStr = request.getParameter("subjectId");
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String durationStr = request.getParameter("durationMinutes");
        String totalMarksStr = request.getParameter("totalMarks");
        String passingMarksStr = request.getParameter("passingMarks");
        String startTimeStr = request.getParameter("startTime");
        String endTimeStr = request.getParameter("endTime");
        boolean publishImmediately = "true".equalsIgnoreCase(request.getParameter("publishImmediately"));

        if (isBlank(title) || isBlank(subjectIdStr) || isBlank(durationStr) ||
            isBlank(totalMarksStr) || isBlank(passingMarksStr) || isBlank(startTimeStr) || isBlank(endTimeStr)) {
            session.setAttribute("flashError", "All required fields must be completed.");
            response.sendRedirect(request.getContextPath() + "/admin/examinations");
            return;
        }

        long subjectId;
        int duration;
        int totalMarks;
        int passingMarks;
        LocalDateTime startTime;
        LocalDateTime endTime;

        try {
            subjectId = Long.parseLong(subjectIdStr.trim());
            duration = Integer.parseInt(durationStr.trim());
            totalMarks = Integer.parseInt(totalMarksStr.trim());
            passingMarks = Integer.parseInt(passingMarksStr.trim());
            startTime = LocalDateTime.parse(startTimeStr.trim(), INPUT_FORMATTER);
            endTime = LocalDateTime.parse(endTimeStr.trim(), INPUT_FORMATTER);
        } catch (NumberFormatException | DateTimeParseException e) {
            session.setAttribute("flashError", "Invalid numeric or date format provided.");
            response.sendRedirect(request.getContextPath() + "/admin/examinations");
            return;
        }

        // Business rules validation
        if (duration <= 0) {
            session.setAttribute("flashError", "Duration must be greater than zero minutes.");
            response.sendRedirect(request.getContextPath() + "/admin/examinations");
            return;
        }

        if (totalMarks <= 0) {
            session.setAttribute("flashError", "Total marks must be greater than zero.");
            response.sendRedirect(request.getContextPath() + "/admin/examinations");
            return;
        }

        if (passingMarks < 0 || passingMarks > totalMarks) {
            session.setAttribute("flashError", "Passing marks must be between 0 and total marks (" + totalMarks + ").");
            response.sendRedirect(request.getContextPath() + "/admin/examinations");
            return;
        }

        if (!endTime.isAfter(startTime)) {
            session.setAttribute("flashError", "End time must be scheduled after start time.");
            response.sendRedirect(request.getContextPath() + "/admin/examinations");
            return;
        }

        Examination exam = new Examination();
        exam.setSubjectId(subjectId);
        exam.setTitle(title.trim());
        exam.setDescription(description != null ? description.trim() : "");
        exam.setDurationMinutes(duration);
        exam.setTotalMarks(totalMarks);
        exam.setPassingMarks(passingMarks);
        exam.setStartTime(startTime);
        exam.setEndTime(endTime);
        exam.setStatus(publishImmediately ? Examination.STATUS_SCHEDULED : Examination.STATUS_DRAFT);
        exam.setCreatedBy(user.getId());

        try {
            boolean created = examDAO.createExamination(exam);
            if (created) {
                String msg = publishImmediately
                        ? "Examination [" + title + "] scheduled and published successfully."
                        : "Examination [" + title + "] saved as Draft. Ready for question allocation.";
                session.setAttribute("flashSuccess", msg);
            } else {
                session.setAttribute("flashError", "Failed to create examination.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating examination: " + e.getMessage(), e);
            session.setAttribute("flashError", "Database error: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/admin/examinations");
    }

    private void handleEditExam(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {

        String idStr = request.getParameter("id");
        String subjectIdStr = request.getParameter("subjectId");
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String durationStr = request.getParameter("durationMinutes");
        String totalMarksStr = request.getParameter("totalMarks");
        String passingMarksStr = request.getParameter("passingMarks");
        String startTimeStr = request.getParameter("startTime");
        String endTimeStr = request.getParameter("endTime");

        long id;
        long subjectId;
        int duration;
        int totalMarks;
        int passingMarks;
        LocalDateTime startTime;
        LocalDateTime endTime;

        try {
            id = Long.parseLong(idStr.trim());
            subjectId = Long.parseLong(subjectIdStr.trim());
            duration = Integer.parseInt(durationStr.trim());
            totalMarks = Integer.parseInt(totalMarksStr.trim());
            passingMarks = Integer.parseInt(passingMarksStr.trim());
            startTime = LocalDateTime.parse(startTimeStr.trim(), INPUT_FORMATTER);
            endTime = LocalDateTime.parse(endTimeStr.trim(), INPUT_FORMATTER);
        } catch (Exception e) {
            session.setAttribute("flashError", "Invalid numeric or date format.");
            response.sendRedirect(request.getContextPath() + "/admin/examinations");
            return;
        }

        if (duration <= 0 || totalMarks <= 0 || passingMarks < 0 || passingMarks > totalMarks || !endTime.isAfter(startTime)) {
            session.setAttribute("flashError", "Validation failed: Check duration, marks, and date order.");
            response.sendRedirect(request.getContextPath() + "/admin/examinations");
            return;
        }

        Examination exam = examDAO.findById(id);
        if (exam == null) {
            session.setAttribute("flashError", "Examination not found.");
            response.sendRedirect(request.getContextPath() + "/admin/examinations");
            return;
        }

        if (!exam.isDraft()) {
            session.setAttribute("flashError", "Only examinations in Draft status can be edited directly.");
            response.sendRedirect(request.getContextPath() + "/admin/examinations");
            return;
        }

        exam.setSubjectId(subjectId);
        exam.setTitle(title.trim());
        exam.setDescription(description != null ? description.trim() : "");
        exam.setDurationMinutes(duration);
        exam.setTotalMarks(totalMarks);
        exam.setPassingMarks(passingMarks);
        exam.setStartTime(startTime);
        exam.setEndTime(endTime);

        try {
            boolean updated = examDAO.updateDraftExamination(exam);
            if (updated) {
                session.setAttribute("flashSuccess", "Draft examination [" + title + "] updated successfully.");
            } else {
                session.setAttribute("flashError", "Could not update examination.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating draft examination: " + e.getMessage(), e);
            session.setAttribute("flashError", "Database error: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/admin/examinations");
    }

    private void handlePublishExam(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {

        String idStr = request.getParameter("id");
        try {
            long id = Long.parseLong(idStr.trim());
            Examination exam = examDAO.findById(id);
            if (exam == null) {
                session.setAttribute("flashError", "Examination not found.");
                response.sendRedirect(request.getContextPath() + "/admin/examinations");
                return;
            }

            boolean published = examDAO.updateStatus(id, Examination.STATUS_SCHEDULED);
            if (published) {
                session.setAttribute("flashSuccess", "Examination [" + exam.getTitle() + "] published to Scheduled status.");
            } else {
                session.setAttribute("flashError", "Failed to publish examination.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error publishing exam: " + e.getMessage(), e);
            session.setAttribute("flashError", "Error publishing examination.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/examinations");
    }

    private void handleCancelExam(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {

        String idStr = request.getParameter("id");
        try {
            long id = Long.parseLong(idStr.trim());
            boolean cancelled = examDAO.updateStatus(id, Examination.STATUS_CANCELLED);
            if (cancelled) {
                session.setAttribute("flashSuccess", "Examination has been marked as Cancelled.");
            } else {
                session.setAttribute("flashError", "Failed to cancel examination.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error cancelling exam: " + e.getMessage(), e);
            session.setAttribute("flashError", "Error cancelling examination.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/examinations");
    }

    private void transferFlashMessages(HttpSession session, HttpServletRequest request) {
        if (session != null) {
            Object success = session.getAttribute("flashSuccess");
            if (success != null) {
                request.setAttribute("flashSuccess", success);
                session.removeAttribute("flashSuccess");
            }
            Object error = session.getAttribute("flashError");
            if (error != null) {
                request.setAttribute("flashError", error);
                session.removeAttribute("flashError");
            }
        }
    }

    private boolean isBlank(String str) {
        return str == null || str.isBlank();
    }
}
