<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.exampro.model.User" %>
<%@ page import="com.exampro.model.ExamResult" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Results Management &amp; Analytics | ExamPro Admin</title>

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
    <!-- Chart.js 4.4 -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
    <!-- Custom Styles -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
</head>
<body class="ep-admin-body">

    <!-- Mobile Sidebar Backdrop -->
    <div class="ep-sidebar-backdrop" id="sidebarBackdrop" aria-hidden="true"></div>

    <div class="ep-admin-wrapper">

        <!-- ═══════════════════════════════════════
             ADMIN SIDEBAR NAVIGATION
        ═══════════════════════════════════════ -->
        <jsp:include page="sidebar.jsp">
            <jsp:param name="active" value="results"/>
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
                        <h1 class="ep-page-title mb-0">Results &amp; Performance Analytics</h1>
                        <nav aria-label="breadcrumb">
                            <ol class="breadcrumb mb-0">
                                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/admin/dashboard">Admin</a></li>
                                <li class="breadcrumb-item active" aria-current="page">Results &amp; Analytics</li>
                            </ol>
                        </nav>
                    </div>
                </div>

                <div class="d-flex align-items-center gap-2">
                    <jsp:include page="/WEB-INF/views/common/notif_bell.jsp"/>
                    <a href="${pageContext.request.contextPath}/admin/results/export?examId=${selectedExamId}&subjectId=${selectedSubjectId}&status=${statusFilter}&publishFilter=${publishFilter}&startDate=${startDate}&endDate=${endDate}&search=${searchQuery}"
                       class="btn btn-outline-success btn-sm">
                        <i class="bi bi-file-earmark-spreadsheet me-1"></i> Export CSV
                    </a>
                    <a href="${pageContext.request.contextPath}/admin/results/print?examId=${selectedExamId}&subjectId=${selectedSubjectId}&status=${statusFilter}&publishFilter=${publishFilter}&search=${searchQuery}"
                       target="_blank" class="btn btn-outline-secondary btn-sm">
                        <i class="bi bi-printer me-1"></i> Print / PDF Report
                    </a>
                </div>
            </header>

            <!-- MAIN CONTAINER -->
            <main class="ep-admin-content p-3 p-md-4">

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

                <!-- ═══════════════════════════════════════
                     TOP KPI METRICS CARDS
                ═══════════════════════════════════════ -->
                <div class="row g-3 mb-4">
                    <div class="col-6 col-xl-3">
                        <div class="card ep-card shadow-sm h-100">
                            <div class="card-body p-3">
                                <div class="d-flex justify-content-between align-items-center">
                                    <div>
                                        <div class="text-muted small fw-semibold">Evaluated Submissions</div>
                                        <div class="h3 fw-bold text-dark mb-0 mt-1"><c:out value="${stats.totalEvaluatedAttempts}"/></div>
                                        <div class="small text-secondary mt-1">From <c:out value="${stats.totalStudentAttempts}"/> total attempts</div>
                                    </div>
                                    <div class="ep-metric-icon bg-primary-subtle text-primary" style="width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center;">
                                        <i class="bi bi-clipboard2-check fs-4"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-6 col-xl-3">
                        <div class="card ep-card shadow-sm h-100">
                            <div class="card-body p-3">
                                <div class="d-flex justify-content-between align-items-center">
                                    <div>
                                        <div class="text-muted small fw-semibold">Overall Pass Rate</div>
                                        <div class="h3 fw-bold text-success mb-0 mt-1">
                                            <fmt:formatNumber value="${stats.overallPassPercentage}" maxFractionDigits="1"/>%
                                        </div>
                                        <div class="small text-secondary mt-1">
                                            <span class="text-success fw-bold"><c:out value="${stats.overallPassCount}"/> Passed</span> &bull; 
                                            <span class="text-danger fw-bold"><c:out value="${stats.overallFailCount}"/> Failed</span>
                                        </div>
                                    </div>
                                    <div class="ep-metric-icon bg-success-subtle text-success" style="width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center;">
                                        <i class="bi bi-check-circle fs-4"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-6 col-xl-3">
                        <div class="card ep-card shadow-sm h-100">
                            <div class="card-body p-3">
                                <div class="d-flex justify-content-between align-items-center">
                                    <div>
                                        <div class="text-muted small fw-semibold">Average Score</div>
                                        <div class="h3 fw-bold text-warning mb-0 mt-1">
                                            <fmt:formatNumber value="${stats.averageScore}" maxFractionDigits="1"/>%
                                        </div>
                                        <div class="small text-secondary mt-1">
                                            High: <strong class="text-dark"><fmt:formatNumber value="${stats.highestScore}" maxFractionDigits="1"/>%</strong> | 
                                            Low: <strong class="text-dark"><fmt:formatNumber value="${stats.lowestScore}" maxFractionDigits="1"/>%</strong>
                                        </div>
                                    </div>
                                    <div class="ep-metric-icon bg-warning-subtle text-warning" style="width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center;">
                                        <i class="bi bi-graph-up-arrow fs-4"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-6 col-xl-3">
                        <div class="card ep-card shadow-sm h-100">
                            <div class="card-body p-3">
                                <div class="d-flex justify-content-between align-items-center">
                                    <div>
                                        <div class="text-muted small fw-semibold">Publication Control</div>
                                        <div class="h3 fw-bold text-purple mb-0 mt-1" style="color: #6f42c1;">
                                            <c:out value="${stats.publishedExamsCount}"/>
                                        </div>
                                        <div class="small text-secondary mt-1">
                                            <span class="text-success"><c:out value="${stats.publishedExamsCount}"/> Published</span> &bull; 
                                            <span class="text-muted"><c:out value="${stats.unpublishedExamsCount}"/> Draft/Pending</span>
                                        </div>
                                    </div>
                                    <div class="ep-metric-icon" style="background: rgba(111, 66, 193, 0.15); color: #6f42c1; width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center;">
                                        <i class="bi bi-broadcast-pin fs-4"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- ═══════════════════════════════════════
                     INTERACTIVE CHARTS & VISUALIZATIONS
                ═══════════════════════════════════════ -->
                <div class="row g-3 mb-4">
                    <!-- Chart 1: Pass vs Fail Distribution -->
                    <div class="col-md-4">
                        <div class="card ep-card shadow-sm h-100">
                            <div class="card-header bg-white py-3 border-0">
                                <h6 class="mb-0 fw-bold text-dark">
                                    <i class="bi bi-pie-chart text-primary me-2"></i> Pass vs Fail Distribution
                                </h6>
                            </div>
                            <div class="card-body p-3 d-flex flex-column justify-content-center">
                                <div class="chart-container-responsive" style="max-height: 220px;">
                                    <canvas id="passFailChart"></canvas>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Chart 2: Score Range Distribution -->
                    <div class="col-md-4">
                        <div class="card ep-card shadow-sm h-100">
                            <div class="card-header bg-white py-3 border-0">
                                <h6 class="mb-0 fw-bold text-dark">
                                    <i class="bi bi-bar-chart-steps text-info me-2"></i> Score Distribution Histogram
                                </h6>
                            </div>
                            <div class="card-body p-3">
                                <div class="chart-container-responsive" style="max-height: 220px;">
                                    <canvas id="scoreDistChart"></canvas>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Chart 3: Subject Performance Comparison -->
                    <div class="col-md-4">
                        <div class="card ep-card shadow-sm h-100">
                            <div class="card-header bg-white py-3 border-0">
                                <h6 class="mb-0 fw-bold text-dark">
                                    <i class="bi bi-journal-bookmark text-success me-2"></i> Subject Performance (Avg %)
                                </h6>
                            </div>
                            <div class="card-body p-3">
                                <div class="chart-container-responsive" style="max-height: 220px;">
                                    <canvas id="subjectPerfChart"></canvas>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- ═══════════════════════════════════════
                     FILTER & SEARCH TOOLBAR
                ═══════════════════════════════════════ -->
                <div class="card ep-card mb-4 shadow-sm">
                    <div class="card-body p-3">
                        <form action="${pageContext.request.contextPath}/admin/results" method="get" class="row g-2 align-items-end">
                            <!-- Filter by Examination -->
                            <div class="col-12 col-md-3">
                                <label for="filterExam" class="form-label small text-muted fw-semibold mb-1">Examination</label>
                                <select class="form-select form-select-sm" id="filterExam" name="examId">
                                    <option value="">-- All Examinations --</option>
                                    <c:forEach var="ex" items="${allExams}">
                                        <option value="${ex.id}" ${selectedExamId == ex.id ? 'selected' : ''}>
                                            <c:out value="${ex.title}"/> (<c:out value="${ex.subjectCode}"/>)
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <!-- Filter by Subject -->
                            <div class="col-12 col-md-2">
                                <label for="filterSubject" class="form-label small text-muted fw-semibold mb-1">Subject</label>
                                <select class="form-select form-select-sm" id="filterSubject" name="subjectId">
                                    <option value="">-- All Subjects --</option>
                                    <c:forEach var="sb" items="${allSubjects}">
                                        <option value="${sb.id}" ${selectedSubjectId == sb.id ? 'selected' : ''}>
                                            <c:out value="${sb.code}"/> - <c:out value="${sb.name}"/>
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <!-- Filter by Outcome -->
                            <div class="col-6 col-md-2">
                                <label for="filterStatus" class="form-label small text-muted fw-semibold mb-1">Outcome</label>
                                <select class="form-select form-select-sm" id="filterStatus" name="status">
                                    <option value="ALL" ${statusFilter == 'ALL' ? 'selected' : ''}>All Outcomes</option>
                                    <option value="PASS" ${statusFilter == 'PASS' ? 'selected' : ''}>Pass Only</option>
                                    <option value="FAIL" ${statusFilter == 'FAIL' ? 'selected' : ''}>Fail Only</option>
                                </select>
                            </div>

                            <!-- Filter by Publication -->
                            <div class="col-6 col-md-2">
                                <label for="publishFilter" class="form-label small text-muted fw-semibold mb-1">Visibility</label>
                                <select class="form-select form-select-sm" id="publishFilter" name="publishFilter">
                                    <option value="ALL" ${publishFilter == 'ALL' ? 'selected' : ''}>All Statuses</option>
                                    <option value="PUBLISHED" ${publishFilter == 'PUBLISHED' ? 'selected' : ''}>Published Only</option>
                                    <option value="UNPUBLISHED" ${publishFilter == 'UNPUBLISHED' ? 'selected' : ''}>Unpublished / Hidden</option>
                                </select>
                            </div>

                            <!-- Search Input -->
                            <div class="col-12 col-md-2">
                                <label for="filterSearch" class="form-label small text-muted fw-semibold mb-1">Search Query</label>
                                <input type="text" class="form-control form-control-sm" id="filterSearch" name="search"
                                       placeholder="Name, email, reg no..."
                                       value="<c:out value='${searchQuery}'/>">
                            </div>

                            <!-- Buttons -->
                            <div class="col-12 col-md-1 d-flex gap-1">
                                <button type="submit" class="btn btn-primary btn-sm w-100" title="Apply Filters">
                                    <i class="bi bi-funnel"></i>
                                </button>
                                <a href="${pageContext.request.contextPath}/admin/results" class="btn btn-outline-secondary btn-sm" title="Reset Filters">
                                    <i class="bi bi-arrow-counterclockwise"></i>
                                </a>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- ═══════════════════════════════════════
                     EXAM PUBLICATION ACTION TOOLBAR
                ═══════════════════════════════════════ -->
                <c:if test="${not empty selectedExamId && selectedExamId > 0}">
                    <div class="alert alert-dark border border-secondary d-flex justify-content-between align-items-center mb-4">
                        <div class="d-flex align-items-center gap-2">
                            <i class="bi bi-shield-lock-fill text-warning fs-4"></i>
                            <div>
                                <strong class="text-white">Examination Publication Control</strong>
                                <div class="small text-secondary">Control whether candidates can view their scorecards and detailed answers for this exam.</div>
                            </div>
                        </div>

                        <div class="d-flex gap-2">
                            <form action="${pageContext.request.contextPath}/admin/results" method="post" class="m-0">
                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                <input type="hidden" name="action" value="publishExam">
                                <input type="hidden" name="examId" value="${selectedExamId}">
                                <button type="submit" class="btn btn-success btn-sm fw-bold">
                                    <i class="bi bi-broadcast me-1"></i> Publish All Results
                                </button>
                            </form>

                            <form action="${pageContext.request.contextPath}/admin/results" method="post" class="m-0">
                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                <input type="hidden" name="action" value="unpublishExam">
                                <input type="hidden" name="examId" value="${selectedExamId}">
                                <button type="submit" class="btn btn-outline-warning btn-sm fw-bold">
                                    <i class="bi bi-eye-slash me-1"></i> Unpublish (Hide from Students)
                                </button>
                            </form>
                        </div>
                    </div>
                </c:if>

                <!-- ═══════════════════════════════════════
                     RESULTS TABLE
                ═══════════════════════════════════════ -->
                <div class="card ep-card shadow-sm">
                    <div class="card-header bg-white py-3 border-0 d-flex justify-content-between align-items-center">
                        <h5 class="mb-0 fw-bold text-dark">
                            Evaluated Submissions
                            <span class="badge bg-light text-dark ms-2 fw-normal">${totalCount} Matching</span>
                        </h5>
                    </div>

                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th scope="col" style="width: 50px;">#</th>
                                    <th scope="col">Candidate</th>
                                    <th scope="col">Examination</th>
                                    <th scope="col">Subject</th>
                                    <th scope="col">Score (Obtained / Total)</th>
                                    <th scope="col">Percentage</th>
                                    <th scope="col">Status</th>
                                    <th scope="col">Visibility</th>
                                    <th scope="col">Submitted At</th>
                                    <th scope="col" class="text-end">Correction</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty results}">
                                        <c:forEach var="res" items="${results}" varStatus="loop">
                                            <tr>
                                                <td class="text-muted fw-semibold">${(currentPage - 1) * 15 + loop.count}</td>
                                                <td>
                                                    <div class="fw-bold text-dark"><c:out value="${res.studentName}"/></div>
                                                    <div class="small text-muted">
                                                        <c:out value="${res.studentEmail}"/>
                                                        <c:if test="${not empty res.registrationNo}">
                                                            &bull; <code><c:out value="${res.registrationNo}"/></code>
                                                        </c:if>
                                                    </div>
                                                </td>
                                                <td>
                                                    <div class="fw-semibold text-dark"><c:out value="${res.examTitle}"/></div>
                                                </td>
                                                <td>
                                                    <span class="badge bg-secondary-subtle text-secondary border">
                                                        <c:out value="${res.subjectCode}"/>
                                                    </span>
                                                </td>
                                                <td>
                                                    <span class="fw-bold text-dark"><fmt:formatNumber value="${res.marksObtained}" maxFractionDigits="1"/></span>
                                                    <span class="text-muted small">/ <c:out value="${res.totalMarks}"/></span>
                                                </td>
                                                <td>
                                                    <div class="d-flex align-items-center gap-2">
                                                        <div class="progress flex-grow-1" style="height: 6px; width: 60px; background: rgba(0,0,0,0.08);">
                                                            <div class="progress-bar ${res.status == 'PASS' ? 'bg-success' : 'bg-danger'}"
                                                                 role="progressbar"
                                                                 style="width: ${res.percentage}%;"
                                                                 aria-valuenow="${res.percentage}"
                                                                 aria-valuemin="0"
                                                                 aria-valuemax="100"></div>
                                                        </div>
                                                        <span class="small fw-semibold"><fmt:formatNumber value="${res.percentage}" maxFractionDigits="1"/>%</span>
                                                    </div>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${res.status == 'PASS'}">
                                                            <span class="badge bg-success-subtle text-success border border-success-subtle px-2 py-1">
                                                                <i class="bi bi-check-circle me-1"></i> PASS
                                                            </span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge bg-danger-subtle text-danger border border-danger-subtle px-2 py-1">
                                                                <i class="bi bi-x-circle me-1"></i> FAIL
                                                            </span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${res.published}">
                                                            <span class="badge bg-success-subtle text-success border border-success-subtle">
                                                                <i class="bi bi-eye me-1"></i> Published
                                                            </span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge bg-warning-subtle text-warning border border-warning-subtle">
                                                                <i class="bi bi-eye-slash me-1"></i> Hidden
                                                            </span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="small text-muted">
                                                    <c:out value="${res.formattedSubmittedAt}"/>
                                                </td>
                                                <td class="text-end">
                                                    <button type="button" class="btn btn-outline-warning btn-sm"
                                                            data-bs-toggle="modal" data-bs-target="#correctMarksModal"
                                                            data-result-id="${res.id}"
                                                            data-student-name="<c:out value='${res.studentName}'/>"
                                                            data-exam-title="<c:out value='${res.examTitle}'/>"
                                                            data-current-marks="${res.marksObtained}"
                                                            data-total-marks="${res.totalMarks}"
                                                            title="Auditable Marks Correction">
                                                        <i class="bi bi-pencil-square me-1"></i> Adjust
                                                    </button>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="10" class="text-center py-5 text-muted">
                                                <i class="bi bi-inbox fs-1 d-block mb-2 text-secondary opacity-50"></i>
                                                <p class="mb-0">No examination results match the selected criteria.</p>
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>

                    <!-- Pagination -->
                    <c:if test="${totalPages > 1}">
                        <div class="card-footer bg-white border-0 py-3">
                            <ul class="pagination pagination-sm justify-content-center m-0">
                                <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                    <a class="page-link" href="${pageContext.request.contextPath}/admin/results?page=${currentPage - 1}&examId=${selectedExamId}&subjectId=${selectedSubjectId}&status=${statusFilter}&publishFilter=${publishFilter}&search=${searchQuery}">Previous</a>
                                </li>
                                <c:forEach var="p" begin="1" end="${totalPages}">
                                    <li class="page-item ${currentPage == p ? 'active' : ''}">
                                        <a class="page-link" href="${pageContext.request.contextPath}/admin/results?page=${p}&examId=${selectedExamId}&subjectId=${selectedSubjectId}&status=${statusFilter}&publishFilter=${publishFilter}&search=${searchQuery}">${p}</a>
                                    </li>
                                </c:forEach>
                                <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                    <a class="page-link" href="${pageContext.request.contextPath}/admin/results?page=${currentPage + 1}&examId=${selectedExamId}&subjectId=${selectedSubjectId}&status=${statusFilter}&publishFilter=${publishFilter}&search=${searchQuery}">Next</a>
                                </li>
                            </ul>
                        </div>
                    </c:if>
                </div>

            </main>
        </div>
    </div>

    <!-- ═══════════════════════════════════════
         AUDITABLE MARKS CORRECTION MODAL
    ═══════════════════════════════════════ -->
    <div class="modal fade" id="correctMarksModal" tabindex="-1" aria-labelledby="correctMarksModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <form action="${pageContext.request.contextPath}/admin/results" method="post">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <input type="hidden" name="action" value="correctMarks">
                    <input type="hidden" name="resultId" id="modalResultId">

                    <div class="modal-header border-bottom">
                        <h5 class="modal-title fw-bold" id="correctMarksModalLabel">
                            <i class="bi bi-shield-exclamation text-warning me-2"></i> Authorized Marks Correction
                        </h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>

                    <div class="modal-body">
                        <div class="alert alert-secondary py-2 px-3 small mb-3">
                            <div>Candidate: <strong id="modalStudentName"></strong></div>
                            <div>Examination: <strong id="modalExamTitle"></strong></div>
                            <div>Maximum Possible Marks: <strong id="modalTotalMarks"></strong></div>
                        </div>

                        <div class="mb-3">
                            <label for="modalNewMarks" class="form-label small fw-semibold">New Corrected Marks</label>
                            <input type="number" step="0.5" min="0" class="form-control" id="modalNewMarks" name="newMarks" required>
                            <div class="form-text small">Must be between 0 and maximum exam marks. Percentage and Pass/Fail status will recalculate automatically.</div>
                        </div>

                        <div class="mb-3">
                            <label for="modalReason" class="form-label small fw-semibold">Administrative Reason / Justification</label>
                            <textarea class="form-control" id="modalReason" name="reason" rows="3" required minlength="5"
                                      placeholder="State the official justification (e.g. 'Bonus awarded for ambiguous Question 4', 'Re-evaluation approved by academic board')..."></textarea>
                            <div class="form-text small text-danger">Required for institutional audit compliance.</div>
                        </div>
                    </div>

                    <div class="modal-footer border-top">
                        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-warning fw-bold">
                            <i class="bi bi-check2-circle me-1"></i> Apply &amp; Log Adjustment
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Bootstrap 5 JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
            crossorigin="anonymous"></script>

    <!-- Client-side Scripts & Chart.js Config -->
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            // Sidebar toggle for mobile
            const toggle = document.getElementById('sidebarToggle');
            const sidebar = document.getElementById('adminSidebar');
            const backdrop = document.getElementById('sidebarBackdrop');
            if (toggle && sidebar && backdrop) {
                toggle.addEventListener('click', () => {
                    sidebar.classList.toggle('show');
                    backdrop.classList.toggle('show');
                });
                backdrop.addEventListener('click', () => {
                    sidebar.classList.remove('show');
                    backdrop.classList.remove('show');
                });
            }

            // Correction modal population
            const correctModal = document.getElementById('correctMarksModal');
            if (correctModal) {
                correctModal.addEventListener('show.bs.modal', function (event) {
                    const button = event.relatedTarget;
                    document.getElementById('modalResultId').value = button.getAttribute('data-result-id');
                    document.getElementById('modalStudentName').textContent = button.getAttribute('data-student-name');
                    document.getElementById('modalExamTitle').textContent = button.getAttribute('data-exam-title');
                    document.getElementById('modalTotalMarks').textContent = button.getAttribute('data-total-marks');
                    const curMarks = button.getAttribute('data-current-marks');
                    const maxMarks = button.getAttribute('data-total-marks');
                    const input = document.getElementById('modalNewMarks');
                    input.value = curMarks;
                    input.max = maxMarks;
                });
            }

            // 1. Chart: Pass vs Fail Distribution
            const passCount = ${stats.overallPassCount};
            const failCount = ${stats.overallFailCount};
            const ctxPF = document.getElementById('passFailChart');
            if (ctxPF) {
                new Chart(ctxPF, {
                    type: 'doughnut',
                    data: {
                        labels: ['Passed', 'Failed'],
                        datasets: [{
                            data: [passCount, failCount],
                            backgroundColor: ['#2ea043', '#f85149'],
                            borderWidth: 2,
                            borderColor: '#ffffff'
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        plugins: {
                            legend: { position: 'bottom' }
                        }
                    }
                });
            }

            // 2. Chart: Score Distribution Brackets
            const c80 = ${stats.count80to100};
            const c60 = ${stats.count60to79};
            const c40 = ${stats.count40to59};
            const cBelow = ${stats.countBelow40};
            const ctxDist = document.getElementById('scoreDistChart');
            if (ctxDist) {
                new Chart(ctxDist, {
                    type: 'bar',
                    data: {
                        labels: ['80-100%', '60-79%', '40-59%', '<40%'],
                        datasets: [{
                            label: 'Candidates',
                            data: [c80, c60, c40, cBelow],
                            backgroundColor: ['#2ea043', '#58a6ff', '#d29922', '#f85149'],
                            borderRadius: 6
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        scales: {
                            y: { beginAtZero: true, ticks: { precision: 0 } }
                        },
                        plugins: { legend: { display: false } }
                    }
                });
            }

            // 3. Chart: Subject Performance Comparison
            const subLabels = [
                <c:forEach var="sp" items="${stats.subjectPerformances}" varStatus="status">
                    '<c:out value="${sp.subjectCode}"/>'<c:if test="${!status.last}">,</c:if>
                </c:forEach>
            ];
            const subData = [
                <c:forEach var="sp" items="${stats.subjectPerformances}" varStatus="status">
                    <fmt:formatNumber value="${sp.avgPercentage}" maxFractionDigits="1"/><c:if test="${!status.last}">,</c:if>
                </c:forEach>
            ];
            const ctxSub = document.getElementById('subjectPerfChart');
            if (ctxSub && subLabels.length > 0) {
                new Chart(ctxSub, {
                    type: 'bar',
                    data: {
                        labels: subLabels,
                        datasets: [{
                            label: 'Avg Score %',
                            data: subData,
                            backgroundColor: '#6f42c1',
                            borderRadius: 6
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        indexAxis: 'y',
                        scales: {
                            x: { beginAtZero: true, max: 100 }
                        },
                        plugins: { legend: { display: false } }
                    }
                });
            }
        });
    </script>
</body>
</html>
