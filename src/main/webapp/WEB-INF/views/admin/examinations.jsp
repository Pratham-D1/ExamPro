<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.exampro.model.User" %>
<%@ page import="com.exampro.model.Examination" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Examination Management | ExamPro Admin</title>

    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet"
          integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH"
          crossorigin="anonymous">
    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <!-- Custom Styles -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
</head>
<body class="ep-admin-body">

    <!-- Mobile Sidebar Backdrop -->
    <div class="ep-sidebar-backdrop" id="sidebarBackdrop" aria-hidden="true"></div>

    <div class="ep-admin-wrapper">

        <!-- ═══════════════════════════════════════
             SIDEBAR NAVIGATION
        ═══════════════════════════════════════ -->
        <jsp:include page="sidebar.jsp">
            <jsp:param name="active" value="examinations"/>
        </jsp:include>

        <!-- ═══════════════════════════════════════
             MAIN CONTENT AREA
        ═══════════════════════════════════════ -->
        <div class="ep-admin-main">

            <!-- TOP NAVBAR -->
            <header class="ep-admin-topbar">
                <div class="d-flex align-items-center gap-3">
                    <button class="ep-menu-toggle d-lg-none" id="sidebarToggle" aria-label="Toggle navigation">
                        <i class="bi bi-list"></i>
                    </button>
                    <div>
                        <h1 class="ep-page-title mb-0">Examination Management</h1>
                        <nav aria-label="breadcrumb">
                            <ol class="breadcrumb mb-0">
                                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/admin/dashboard">Admin</a></li>
                                <li class="breadcrumb-item active" aria-current="page">Examinations</li>
                            </ol>
                        </nav>
                    </div>
                </div>

                <div class="d-flex align-items-center gap-3">
                    <jsp:include page="/WEB-INF/views/common/notif_bell.jsp"/>
                    <button type="button" class="btn btn-primary d-flex align-items-center gap-2"
                            data-bs-toggle="modal" data-bs-target="#createExamModal">
                        <i class="bi bi-plus-circle"></i>
                        <span class="d-none d-sm-inline">Schedule Exam</span>
                    </button>
                </div>
            </header>

            <!-- MAIN CONTAINER -->
            <main class="ep-admin-content">

                <!-- Alert Messages -->
                <c:if test="${not empty flashSuccess}">
                    <div class="alert alert-success alert-dismissible fade show d-flex align-items-center gap-2" role="alert">
                        <i class="bi bi-check-circle-fill fs-5"></i>
                        <div><c:out value="${flashSuccess}"/></div>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:if>
                <c:if test="${not empty flashError}">
                    <div class="alert alert-danger alert-dismissible fade show d-flex align-items-center gap-2" role="alert">
                        <i class="bi bi-exclamation-triangle-fill fs-5"></i>
                        <div><c:out value="${flashError}"/></div>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:if>

                <!-- Question Allocation Notice -->
                <div class="alert alert-info border-0 shadow-sm d-flex align-items-start gap-3 mb-4">
                    <i class="bi bi-info-circle-fill fs-4 text-primary flex-shrink-0 mt-1"></i>
                    <div>
                        <h6 class="alert-heading fw-bold mb-1">Examination Lifecycle &amp; Question Configuration</h6>
                        <p class="mb-0 small text-muted">
                            Examinations can be created as <strong>Drafts</strong> or scheduled directly. In accordance with ExamPro policy, 
                            evaluators must ensure questions are prepared prior to candidate participation. Draft examinations can be edited at any time, 
                            whereas published examinations preserve configuration integrity against running attempts.
                        </p>
                    </div>
                </div>

                <!-- Status Filter Pills & Search Bar -->
                <div class="card ep-card mb-4">
                    <div class="card-body p-3">
                        <div class="row g-3 align-items-center">
                            <!-- Status Filters -->
                            <div class="col-12 col-xl-7">
                                <div class="d-flex flex-wrap gap-2">
                                    <a href="${pageContext.request.contextPath}/admin/examinations?status=ALL&search=<c:out value='${searchQuery}'/>"
                                       class="btn btn-sm ${statusFilter == 'ALL' ? 'btn-primary' : 'btn-outline-secondary'}">
                                        All Statuses
                                    </a>
                                    <a href="${pageContext.request.contextPath}/admin/examinations?status=DRAFT&search=<c:out value='${searchQuery}'/>"
                                       class="btn btn-sm ${statusFilter == 'DRAFT' ? 'btn-secondary' : 'btn-outline-secondary'}">
                                        Draft
                                    </a>
                                    <a href="${pageContext.request.contextPath}/admin/examinations?status=SCHEDULED&search=<c:out value='${searchQuery}'/>"
                                       class="btn btn-sm ${statusFilter == 'SCHEDULED' ? 'btn-primary' : 'btn-outline-secondary'}">
                                        Scheduled
                                    </a>
                                    <a href="${pageContext.request.contextPath}/admin/examinations?status=ONGOING&search=<c:out value='${searchQuery}'/>"
                                       class="btn btn-sm ${statusFilter == 'ONGOING' ? 'btn-info' : 'btn-outline-secondary'}">
                                        Ongoing
                                    </a>
                                    <a href="${pageContext.request.contextPath}/admin/examinations?status=COMPLETED&search=<c:out value='${searchQuery}'/>"
                                       class="btn btn-sm ${statusFilter == 'COMPLETED' ? 'btn-success' : 'btn-outline-secondary'}">
                                        Completed
                                    </a>
                                    <a href="${pageContext.request.contextPath}/admin/examinations?status=CANCELLED&search=<c:out value='${searchQuery}'/>"
                                       class="btn btn-sm ${statusFilter == 'CANCELLED' ? 'btn-danger' : 'btn-outline-secondary'}">
                                        Cancelled
                                    </a>
                                </div>
                            </div>

                            <!-- Search Form -->
                            <div class="col-12 col-xl-5">
                                <form action="${pageContext.request.contextPath}/admin/examinations" method="get" class="d-flex gap-2">
                                    <input type="hidden" name="status" value="<c:out value='${statusFilter}'/>">
                                    <div class="input-group">
                                        <span class="input-group-text bg-white"><i class="bi bi-search"></i></span>
                                        <input type="text" name="search" class="form-control"
                                               placeholder="Search by title, subject..."
                                               value="<c:out value='${searchQuery}'/>">
                                    </div>
                                    <button type="submit" class="btn btn-dark">Search</button>
                                    <c:if test="${not empty searchQuery || statusFilter != 'ALL'}">
                                        <a href="${pageContext.request.contextPath}/admin/examinations" class="btn btn-outline-secondary" title="Reset filter">
                                            <i class="bi bi-x-lg"></i>
                                        </a>
                                    </c:if>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- EXAMINATIONS TABLE CARD -->
                <div class="card ep-card shadow-sm">
                    <div class="card-header bg-white py-3 border-0 d-flex justify-content-between align-items-center">
                        <h5 class="mb-0 fw-bold text-dark">
                            Scheduled Examinations
                            <span class="badge bg-light text-dark ms-2 fw-normal">${totalCount} Total</span>
                        </h5>
                    </div>

                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th scope="col" style="width: 50px;">#</th>
                                    <th scope="col">Examination Details</th>
                                    <th scope="col">Subject</th>
                                    <th scope="col">Duration</th>
                                    <th scope="col">Marks (Pass / Total)</th>
                                    <th scope="col">Window Period</th>
                                    <th scope="col">Status</th>
                                    <th scope="col">Attempts</th>
                                    <th scope="col" class="text-end">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty examinations}">
                                        <c:forEach var="exam" items="${examinations}" varStatus="loop">
                                            <tr>
                                                <td class="text-muted fw-semibold">${(currentPage - 1) * 12 + loop.count}</td>
                                                <td>
                                                    <div class="fw-bold text-dark"><c:out value="${exam.title}"/></div>
                                                    <c:if test="${not empty exam.description}">
                                                        <div class="text-muted small text-truncate" style="max-width: 250px;">
                                                            <c:out value="${exam.description}"/>
                                                        </div>
                                                    </c:if>
                                                </td>
                                                <td>
                                                    <span class="badge bg-light text-primary border border-primary-subtle fw-semibold">
                                                        <c:out value="${exam.subjectCode}"/>
                                                    </span>
                                                    <div class="small text-muted"><c:out value="${exam.subjectName}"/></div>
                                                </td>
                                                <td>
                                                    <span class="fw-semibold"><c:out value="${exam.durationMinutes}"/></span> mins
                                                </td>
                                                <td>
                                                    <span class="text-success fw-bold"><c:out value="${exam.passingMarks}"/></span>
                                                    <span class="text-muted">/ <c:out value="${exam.totalMarks}"/></span>
                                                </td>
                                                <td>
                                                    <div class="small">
                                                        <i class="bi bi-calendar-event text-primary me-1"></i>
                                                        <c:out value="${exam.formattedStartTime}"/>
                                                    </div>
                                                    <div class="small text-muted">
                                                        <i class="bi bi-clock-history me-1"></i>
                                                        to <c:out value="${exam.formattedEndTime}"/>
                                                    </div>
                                                </td>
                                                <td>
                                                    <span class="badge ${exam.statusBadgeClass} text-uppercase px-2 py-1">
                                                        <c:out value="${exam.status}"/>
                                                    </span>
                                                </td>
                                                <td>
                                                    <span class="badge bg-secondary-subtle text-secondary-emphasis">
                                                        <i class="bi bi-person-check me-1"></i>${exam.totalAttempts}
                                                    </span>
                                                </td>
                                                <td class="text-end">
                                                    <div class="btn-group btn-group-sm">
                                                        <!-- View Details Button -->
                                                        <button type="button" class="btn btn-outline-info"
                                                                title="View Details"
                                                                onclick="openViewModal(${exam.id}, '<c:out value="${exam.title}" escapeXml="true"/>', '<c:out value="${exam.subjectCode}" escapeXml="true"/> - <c:out value="${exam.subjectName}" escapeXml="true"/>', ${exam.durationMinutes}, ${exam.totalMarks}, ${exam.passingMarks}, '<c:out value="${exam.formattedStartTime}" escapeXml="true"/>', '<c:out value="${exam.formattedEndTime}" escapeXml="true"/>', '<c:out value="${exam.status}" escapeXml="true"/>', '<c:out value="${exam.description}" escapeXml="true"/>', ${exam.totalAttempts})">
                                                            <i class="bi bi-eye"></i>
                                                        </button>

                                                        <!-- Edit Button (Only allowed if DRAFT) -->
                                                        <c:if test="${exam.draft}">
                                                            <button type="button" class="btn btn-outline-primary"
                                                                    title="Edit Draft"
                                                                    onclick="openEditModal(${exam.id}, ${exam.subjectId}, '<c:out value="${exam.title}" escapeXml="true"/>', '<c:out value="${exam.description}" escapeXml="true"/>', ${exam.durationMinutes}, ${exam.totalMarks}, ${exam.passingMarks}, '<c:out value="${exam.inputStartTime}" escapeXml="true"/>', '<c:out value="${exam.inputEndTime}" escapeXml="true"/>')">
                                                                <i class="bi bi-pencil"></i>
                                                            </button>

                                                            <!-- Publish Button -->
                                                            <button type="button" class="btn btn-outline-success"
                                                                    title="Publish Examination"
                                                                    onclick="openPublishModal(${exam.id}, '<c:out value="${exam.title}" escapeXml="true"/>')">
                                                                <i class="bi bi-check-circle"></i>
                                                            </button>
                                                        </c:if>

                                                        <!-- Cancel Button (Only if not already cancelled or completed) -->
                                                        <c:if test="${exam.status != 'CANCELLED' && exam.status != 'COMPLETED'}">
                                                            <button type="button" class="btn btn-outline-danger"
                                                                    title="Cancel Examination"
                                                                    onclick="openCancelModal(${exam.id}, '<c:out value="${exam.title}" escapeXml="true"/>')">
                                                                <i class="bi bi-slash-circle"></i>
                                                            </button>
                                                        </c:if>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="9" class="text-center py-5">
                                                <div class="ep-empty-state">
                                                    <i class="bi bi-file-earmark-x display-4 text-muted"></i>
                                                    <h5 class="mt-3 text-secondary">No Examinations Found</h5>
                                                    <p class="text-muted small">
                                                        <c:choose>
                                                            <c:when test="${not empty searchQuery or statusFilter != 'ALL'}">
                                                                No examinations match the selected filter criteria.
                                                            </c:when>
                                                            <c:otherwise>
                                                                No examinations have been scheduled yet. Click "Schedule Exam" to create one.
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </p>
                                                    <button type="button" class="btn btn-sm btn-primary mt-2"
                                                            data-bs-toggle="modal" data-bs-target="#createExamModal">
                                                        <i class="bi bi-plus-circle me-1"></i> Create First Exam
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>

                    <!-- Pagination -->
                    <c:if test="${totalPages > 1}">
                        <div class="card-footer bg-white border-0 py-3 d-flex justify-content-between align-items-center">
                            <span class="text-muted small">
                                Page <strong>${currentPage}</strong> of <strong>${totalPages}</strong> (${totalCount} records)
                            </span>
                            <nav aria-label="Examinations Table Pagination">
                                <ul class="pagination pagination-sm mb-0">
                                    <li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
                                        <a class="page-link" href="${pageContext.request.contextPath}/admin/examinations?page=${currentPage - 1}&status=<c:out value='${statusFilter}'/>&search=<c:out value='${searchQuery}'/>">
                                            Previous
                                        </a>
                                    </li>
                                    <c:forEach begin="1" end="${totalPages}" var="p">
                                        <li class="page-item ${p == currentPage ? 'active' : ''}">
                                            <a class="page-link" href="${pageContext.request.contextPath}/admin/examinations?page=${p}&status=<c:out value='${statusFilter}'/>&search=<c:out value='${searchQuery}'/>">
                                                ${p}
                                            </a>
                                        </li>
                                    </c:forEach>
                                    <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                                        <a class="page-link" href="${pageContext.request.contextPath}/admin/examinations?page=${currentPage + 1}&status=<c:out value='${statusFilter}'/>&search=<c:out value='${searchQuery}'/>">
                                            Next
                                        </a>
                                    </li>
                                </ul>
                            </nav>
                        </div>
                    </c:if>
                </div>

            </main>
        </div>
    </div>

    <!-- ═══════════════════════════════════════
         CREATE EXAMINATION MODAL
    ═══════════════════════════════════════ -->
    <div class="modal fade" id="createExamModal" tabindex="-1" aria-labelledby="createExamModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <form action="${pageContext.request.contextPath}/admin/examinations" method="post" id="createExamForm">
                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                    <input type="hidden" name="action" value="create">

                    <div class="modal-header">
                        <h5 class="modal-title fw-bold" id="createExamModalLabel">
                            <i class="bi bi-calendar-plus text-primary me-2"></i>Schedule New Examination
                        </h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>

                    <div class="modal-body">
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label for="createSubjectId" class="form-label fw-semibold">Subject <span class="text-danger">*</span></label>
                                <select class="form-select" id="createSubjectId" name="subjectId" required>
                                    <option value="" disabled selected>-- Select Target Subject --</option>
                                    <c:forEach var="sub" items="${allSubjects}">
                                        <option value="${sub.id}">
                                            <c:out value="${sub.code}"/> - <c:out value="${sub.name}"/>
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="col-md-6">
                                <label for="createTitle" class="form-label fw-semibold">Examination Title <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="createTitle" name="title"
                                       placeholder="e.g. Midterm Assessment 2026" required maxlength="150">
                            </div>

                            <div class="col-12">
                                <label for="createDescription" class="form-label fw-semibold">Instructions / Description</label>
                                <textarea class="form-control" id="createDescription" name="description" rows="2"
                                          placeholder="Instructions, syllabus coverage, or notes for candidates..."></textarea>
                            </div>

                            <div class="col-md-4">
                                <label for="createDuration" class="form-label fw-semibold">Duration (Minutes) <span class="text-danger">*</span></label>
                                <input type="number" class="form-control" id="createDuration" name="durationMinutes"
                                       min="1" max="600" value="60" required>
                            </div>

                            <div class="col-md-4">
                                <label for="createTotalMarks" class="form-label fw-semibold">Total Marks <span class="text-danger">*</span></label>
                                <input type="number" class="form-control" id="createTotalMarks" name="totalMarks"
                                       min="1" max="1000" value="100" required>
                            </div>

                            <div class="col-md-4">
                                <label for="createPassingMarks" class="form-label fw-semibold">Passing Marks <span class="text-danger">*</span></label>
                                <input type="number" class="form-control" id="createPassingMarks" name="passingMarks"
                                       min="0" max="1000" value="40" required>
                            </div>

                            <div class="col-md-6">
                                <label for="createStartTime" class="form-label fw-semibold">Window Start Time <span class="text-danger">*</span></label>
                                <input type="datetime-local" class="form-control" id="createStartTime" name="startTime" required>
                            </div>

                            <div class="col-md-6">
                                <label for="createEndTime" class="form-label fw-semibold">Window End Time <span class="text-danger">*</span></label>
                                <input type="datetime-local" class="form-control" id="createEndTime" name="endTime" required>
                            </div>

                            <div class="col-12 mt-3">
                                <div class="form-check form-switch p-3 bg-light rounded border">
                                    <input class="form-check-input ms-0 me-3" type="checkbox" id="publishImmediately" name="publishImmediately" value="true">
                                    <label class="form-check-label fw-semibold" for="publishImmediately">
                                        Publish Examination Immediately (Status: SCHEDULED)
                                    </label>
                                    <div class="text-muted small ps-5">
                                        If left unchecked, this examination will be saved as a <strong>DRAFT</strong> allowing further adjustments.
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-primary">
                            <i class="bi bi-save me-1"></i> Save Examination
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- ═══════════════════════════════════════
         EDIT DRAFT EXAMINATION MODAL
    ═══════════════════════════════════════ -->
    <div class="modal fade" id="editExamModal" tabindex="-1" aria-labelledby="editExamModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <form action="${pageContext.request.contextPath}/admin/examinations" method="post" id="editExamForm">
                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                    <input type="hidden" name="action" value="edit">
                    <input type="hidden" name="id" id="editExamId">

                    <div class="modal-header">
                        <h5 class="modal-title fw-bold" id="editExamModalLabel">
                            <i class="bi bi-pencil-square text-primary me-2"></i>Edit Draft Examination
                        </h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>

                    <div class="modal-body">
                        <div class="alert alert-warning small py-2 mb-3">
                            <i class="bi bi-exclamation-triangle me-1"></i>
                            Only draft examinations can be reconfigured directly.
                        </div>

                        <div class="row g-3">
                            <div class="col-md-6">
                                <label for="editSubjectId" class="form-label fw-semibold">Subject <span class="text-danger">*</span></label>
                                <select class="form-select" id="editSubjectId" name="subjectId" required>
                                    <c:forEach var="sub" items="${allSubjects}">
                                        <option value="${sub.id}">
                                            <c:out value="${sub.code}"/> - <c:out value="${sub.name}"/>
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="col-md-6">
                                <label for="editTitle" class="form-label fw-semibold">Examination Title <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="editTitle" name="title" required maxlength="150">
                            </div>

                            <div class="col-12">
                                <label for="editDescription" class="form-label fw-semibold">Instructions / Description</label>
                                <textarea class="form-control" id="editDescription" name="description" rows="2"></textarea>
                            </div>

                            <div class="col-md-4">
                                <label for="editDuration" class="form-label fw-semibold">Duration (Minutes) <span class="text-danger">*</span></label>
                                <input type="number" class="form-control" id="editDuration" name="durationMinutes" min="1" max="600" required>
                            </div>

                            <div class="col-md-4">
                                <label for="editTotalMarks" class="form-label fw-semibold">Total Marks <span class="text-danger">*</span></label>
                                <input type="number" class="form-control" id="editTotalMarks" name="totalMarks" min="1" max="1000" required>
                            </div>

                            <div class="col-md-4">
                                <label for="editPassingMarks" class="form-label fw-semibold">Passing Marks <span class="text-danger">*</span></label>
                                <input type="number" class="form-control" id="editPassingMarks" name="passingMarks" min="0" max="1000" required>
                            </div>

                            <div class="col-md-6">
                                <label for="editStartTime" class="form-label fw-semibold">Window Start Time <span class="text-danger">*</span></label>
                                <input type="datetime-local" class="form-control" id="editStartTime" name="startTime" required>
                            </div>

                            <div class="col-md-6">
                                <label for="editEndTime" class="form-label fw-semibold">Window End Time <span class="text-danger">*</span></label>
                                <input type="datetime-local" class="form-control" id="editEndTime" name="endTime" required>
                            </div>
                        </div>
                    </div>

                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-primary">
                            <i class="bi bi-save me-1"></i> Update Draft
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- ═══════════════════════════════════════
         VIEW EXAMINATION DETAILS MODAL
    ═══════════════════════════════════════ -->
    <div class="modal fade" id="viewExamModal" tabindex="-1" aria-labelledby="viewExamModalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title fw-bold" id="viewExamModalLabel">
                        <i class="bi bi-info-circle text-primary me-2"></i>Examination Specification
                    </h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <div class="text-center mb-3">
                        <h4 class="fw-bold mb-1" id="viewExamTitle"></h4>
                        <span class="badge bg-primary" id="viewExamSubject"></span>
                        <span class="badge bg-secondary ms-1" id="viewExamStatus"></span>
                    </div>

                    <ul class="list-group list-group-flush border rounded mb-3">
                        <li class="list-group-item d-flex justify-content-between align-items-center">
                            <span class="text-muted">Duration:</span>
                            <span class="fw-bold" id="viewExamDuration"></span>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center">
                            <span class="text-muted">Scoring System:</span>
                            <span class="fw-bold" id="viewExamMarks"></span>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center">
                            <span class="text-muted">Start Time:</span>
                            <span class="fw-bold" id="viewExamStartTime"></span>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center">
                            <span class="text-muted">End Time:</span>
                            <span class="fw-bold" id="viewExamEndTime"></span>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center">
                            <span class="text-muted">Total Attempts:</span>
                            <span class="badge bg-info text-dark" id="viewExamAttempts"></span>
                        </li>
                    </ul>

                    <div>
                        <h6 class="fw-bold small text-muted text-uppercase mb-1">Instructions / Description</h6>
                        <div class="p-3 bg-light rounded text-secondary small" id="viewExamDescription"></div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                </div>
            </div>
        </div>
    </div>

    <!-- ═══════════════════════════════════════
         PUBLISH CONFIRMATION MODAL
    ═══════════════════════════════════════ -->
    <div class="modal fade" id="publishModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <form action="${pageContext.request.contextPath}/admin/examinations" method="post">
                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                    <input type="hidden" name="action" value="publish">
                    <input type="hidden" name="id" id="publishExamId">

                    <div class="modal-header">
                        <h5 class="modal-title fw-bold text-success">
                            <i class="bi bi-cloud-arrow-up-fill me-2"></i>Publish Examination
                        </h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <p class="mb-2">Are you sure you want to publish examination <strong id="publishExamTitle"></strong>?</p>
                        <div class="alert alert-info small mb-0">
                            <i class="bi bi-shield-check me-1"></i>
                            Once published, this examination status becomes <strong>SCHEDULED</strong> and will become active for candidates during the window period.
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-success">Publish Now</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- ═══════════════════════════════════════
         CANCEL CONFIRMATION MODAL
    ═══════════════════════════════════════ -->
    <div class="modal fade" id="cancelModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <form action="${pageContext.request.contextPath}/admin/examinations" method="post">
                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                    <input type="hidden" name="action" value="cancel">
                    <input type="hidden" name="id" id="cancelExamId">

                    <div class="modal-header">
                        <h5 class="modal-title fw-bold text-danger">
                            <i class="bi bi-slash-circle-fill me-2"></i>Cancel Examination
                        </h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <p class="mb-2">Are you sure you want to cancel examination <strong id="cancelExamTitle"></strong>?</p>
                        <div class="alert alert-warning small mb-0">
                            <i class="bi bi-exclamation-triangle me-1"></i>
                            This will close the examination window and mark the status as <strong>CANCELLED</strong>. Candidate attempts already completed remain archived.
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Keep Exam</button>
                        <button type="submit" class="btn btn-danger">Confirm Cancellation</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Bootstrap 5 JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
            crossorigin="anonymous"></script>

    <!-- Page Specific Script -->
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            // Mobile Sidebar Toggle
            const toggle = document.getElementById('sidebarToggle');
            const sidebar = document.getElementById('adminSidebar');
            const backdrop = document.getElementById('sidebarBackdrop');

            if (toggle && sidebar && backdrop) {
                toggle.addEventListener('click', function () {
                    sidebar.classList.toggle('show');
                    backdrop.classList.toggle('show');
                });
                backdrop.addEventListener('click', function () {
                    sidebar.classList.remove('show');
                    backdrop.classList.remove('show');
                });
            }

            // Set default datetime values for create modal (now and now + 7 days)
            const now = new Date();
            const pad = (n) => String(n).padStart(2, '0');
            const formatForInput = (d) => {
                return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + 'T' + pad(d.getHours()) + ':' + pad(d.getMinutes());
            };
            
            const startInput = document.getElementById('createStartTime');
            const endInput = document.getElementById('createEndTime');
            if (startInput && !startInput.value) {
                startInput.value = formatForInput(now);
            }
            if (endInput && !endInput.value) {
                const later = new Date(now.getTime() + (2 * 60 * 60 * 1000)); // +2 hours
                endInput.value = formatForInput(later);
            }
        });

        function openViewModal(id, title, subject, duration, totalMarks, passingMarks, start, end, status, desc, attempts) {
            document.getElementById('viewExamTitle').textContent = title;
            document.getElementById('viewExamSubject').textContent = subject;
            document.getElementById('viewExamStatus').textContent = status;
            document.getElementById('viewExamDuration').textContent = duration + ' Minutes';
            document.getElementById('viewExamMarks').textContent = passingMarks + ' / ' + totalMarks + ' (Passing / Total)';
            document.getElementById('viewExamStartTime').textContent = start;
            document.getElementById('viewExamEndTime').textContent = end;
            document.getElementById('viewExamAttempts').textContent = attempts + ' Attempts';
            document.getElementById('viewExamDescription').textContent = desc || 'No specific instructions provided.';

            const modal = new bootstrap.Modal(document.getElementById('viewExamModal'));
            modal.show();
        }

        function openEditModal(id, subjectId, title, desc, duration, totalMarks, passingMarks, start, end) {
            document.getElementById('editExamId').value = id;
            document.getElementById('editSubjectId').value = subjectId;
            document.getElementById('editTitle').value = title;
            document.getElementById('editDescription').value = desc || '';
            document.getElementById('editDuration').value = duration;
            document.getElementById('editTotalMarks').value = totalMarks;
            document.getElementById('editPassingMarks').value = passingMarks;
            document.getElementById('editStartTime').value = start;
            document.getElementById('editEndTime').value = end;

            const modal = new bootstrap.Modal(document.getElementById('editExamModal'));
            modal.show();
        }

        function openPublishModal(id, title) {
            document.getElementById('publishExamId').value = id;
            document.getElementById('publishExamTitle').textContent = title;
            const modal = new bootstrap.Modal(document.getElementById('publishModal'));
            modal.show();
        }

        function openCancelModal(id, title) {
            document.getElementById('cancelExamId').value = id;
            document.getElementById('cancelExamTitle').textContent = title;
            const modal = new bootstrap.Modal(document.getElementById('cancelModal'));
            modal.show();
        }
    </script>
</body>
</html>
