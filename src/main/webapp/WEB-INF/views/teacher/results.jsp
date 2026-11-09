<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Student Results &amp; Analytics | ExamPro Teacher</title>

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

        <!-- Sidebar Navigation -->
        <jsp:include page="sidebar.jsp">
            <jsp:param name="active" value="results"/>
        </jsp:include>

        <!-- Main Content -->
        <main class="ep-admin-main">

            <!-- Topbar -->
            <header class="ep-admin-topbar">
                <div class="d-flex align-items-center gap-3">
                    <button class="btn btn-outline-secondary btn-sm d-lg-none" id="sidebarToggle" type="button" aria-label="Toggle Navigation">
                        <i class="bi bi-list fs-5"></i>
                    </button>
                    <nav aria-label="breadcrumb">
                        <ol class="breadcrumb m-0 ep-topbar-breadcrumb">
                            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/teacher/dashboard">Teacher Portal</a></li>
                            <li class="breadcrumb-item active" aria-current="page">Student Results &amp; Analytics</li>
                        </ol>
                    </nav>
                </div>
                <div class="ep-topbar-actions d-flex align-items-center gap-2">
                    <a href="${pageContext.request.contextPath}/teacher/results/export?examId=${selectedExamId}" class="btn btn-outline-success btn-sm">
                        <i class="bi bi-file-earmark-spreadsheet me-1"></i> Export CSV
                    </a>
                    <a href="${pageContext.request.contextPath}/teacher/results/print?examId=${selectedExamId}" target="_blank" class="btn btn-outline-secondary btn-sm">
                        <i class="bi bi-printer me-1"></i> Print Grade Sheet
                    </a>
                </div>
            </header>

            <div class="p-3 p-md-4">

                <!-- Alert Messages -->
                <c:if test="${not empty flashSuccess}">
                    <div class="alert alert-success alert-dismissible fade show d-flex align-items-center mb-4" role="alert">
                        <i class="bi bi-check-circle-fill fs-5 me-2"></i>
                        <div><c:out value="${flashSuccess}"/></div>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:if>
                <c:if test="${not empty flashError}">
                    <div class="alert alert-danger alert-dismissible fade show d-flex align-items-center mb-4" role="alert">
                        <i class="bi bi-exclamation-triangle-fill fs-5 me-2"></i>
                        <div><c:out value="${flashError}"/></div>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:if>

                <!-- Page Header -->
                <div class="d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center mb-4 gap-3">
                    <div>
                        <h1 class="h4 fw-bold text-white mb-1">
                            <i class="bi bi-bar-chart-line-fill text-purple me-2" style="color: #a371f7;"></i> Examination Results &amp; Item Analysis
                        </h1>
                        <p class="text-secondary small mb-0">
                            Inspect student test submissions, question accuracy distribution, and subject performance metrics.
                        </p>
                    </div>
                </div>

                <!-- 4 Performance Summary Cards -->
                <section class="row g-3 mb-4">
                    <!-- Total Submissions -->
                    <div class="col-6 col-xl-3">
                        <div class="card bg-dark border-secondary p-3 h-100">
                            <span class="text-secondary small fw-medium">Total Submissions</span>
                            <div class="d-flex align-items-baseline gap-2 mt-2">
                                <h2 class="h2 fw-bold text-white mb-0"><c:out value="${summary.totalAttempts}"/></h2>
                                <span class="text-muted small">candidates</span>
                            </div>
                        </div>
                    </div>

                    <!-- Passed -->
                    <div class="col-6 col-xl-3">
                        <div class="card bg-dark border-secondary p-3 h-100">
                            <span class="text-secondary small fw-medium">Passed Candidates</span>
                            <div class="d-flex align-items-baseline gap-2 mt-2">
                                <h2 class="h2 fw-bold text-success mb-0"><c:out value="${summary.passedCount}"/></h2>
                                <span class="text-muted small">
                                    (<fmt:formatNumber value="${summary.totalAttempts > 0 ? (summary.passedCount * 100.0 / summary.totalAttempts) : 0}" maxFractionDigits="1"/>%)
                                </span>
                            </div>
                        </div>
                    </div>

                    <!-- Failed -->
                    <div class="col-6 col-xl-3">
                        <div class="card bg-dark border-secondary p-3 h-100">
                            <span class="text-secondary small fw-medium">Failed Candidates</span>
                            <div class="d-flex align-items-baseline gap-2 mt-2">
                                <h2 class="h2 fw-bold text-danger mb-0"><c:out value="${summary.failedCount}"/></h2>
                                <span class="text-muted small">
                                    (<fmt:formatNumber value="${summary.totalAttempts > 0 ? (summary.failedCount * 100.0 / summary.totalAttempts) : 0}" maxFractionDigits="1"/>%)
                                </span>
                            </div>
                        </div>
                    </div>

                    <!-- Average Score -->
                    <div class="col-6 col-xl-3">
                        <div class="card bg-dark border-secondary p-3 h-100">
                            <span class="text-secondary small fw-medium">Average Performance</span>
                            <div class="d-flex align-items-baseline gap-2 mt-2">
                                <h2 class="h2 fw-bold text-info mb-0">
                                    <fmt:formatNumber value="${summary.avgPercentage}" maxFractionDigits="1"/>%
                                </h2>
                                <span class="text-muted small">
                                    (<fmt:formatNumber value="${summary.avgMarks}" maxFractionDigits="1"/> pts)
                                </span>
                            </div>
                        </div>
                    </div>
                </section>

                <!-- Filter Toolbar -->
                <div class="card bg-dark border-secondary p-3 mb-4">
                    <form method="get" action="${pageContext.request.contextPath}/teacher/results" class="row g-2 align-items-center">
                        <div class="col-md-5">
                            <div class="input-group input-group-sm">
                                <span class="input-group-text bg-secondary border-secondary text-secondary">
                                    <i class="bi bi-search"></i>
                                </span>
                                <input type="text" name="search" class="form-control bg-dark text-white border-secondary"
                                       placeholder="Search student name, email, or registration no..." value="<c:out value='${search}'/>">
                            </div>
                        </div>

                        <div class="col-md-5">
                            <select name="examId" class="form-select form-select-sm bg-dark text-white border-secondary">
                                <option value="">-- All Authorized Examinations --</option>
                                <c:forEach var="ex" items="${teacherExams}">
                                    <option value="${ex.id}" ${selectedExamId == ex.id ? 'selected' : ''}>
                                        <c:out value="${ex.title}"/> (<c:out value="${ex.subjectCode}"/>)
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="col-md-2 d-flex gap-2 justify-content-md-end">
                            <button type="submit" class="btn btn-outline-info btn-sm w-100">
                                <i class="bi bi-funnel me-1"></i> Filter
                            </button>
                            <c:if test="${not empty selectedExamId || not empty search}">
                                <a href="${pageContext.request.contextPath}/teacher/results" class="btn btn-outline-secondary btn-sm" title="Reset">
                                    <i class="bi bi-arrow-counterclockwise"></i>
                                </a>
                            </c:if>
                        </div>
                    </form>
                </div>

                <!-- ═══════════════════════════════════════
                     CHARTS: Pass/Fail & Question Accuracy
                ═══════════════════════════════════════ -->
                <div class="row g-3 mb-4">
                    <!-- Chart 1: Pass vs Fail -->
                    <div class="col-md-4">
                        <div class="card bg-dark border-secondary h-100 p-3">
                            <h6 class="text-white fw-bold mb-3">
                                <i class="bi bi-pie-chart text-info me-2"></i> Candidate Pass vs Fail
                            </h6>
                            <div class="chart-container-responsive" style="max-height: 200px;">
                                <canvas id="teacherPassFailChart"></canvas>
                            </div>
                        </div>
                    </div>

                    <!-- Chart 2: Question Accuracy Breakdown -->
                    <div class="col-md-8">
                        <div class="card bg-dark border-secondary h-100 p-3">
                            <h6 class="text-white fw-bold mb-3">
                                <i class="bi bi-bar-chart-steps text-purple me-2" style="color: #a371f7;"></i> Question Accuracy Analysis
                            </h6>
                            <c:choose>
                                <c:when test="${not empty questionPerformances}">
                                    <div class="chart-container-responsive" style="max-height: 200px;">
                                        <canvas id="questionAccuracyChart"></canvas>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="text-center py-4 text-muted">
                                        <i class="bi bi-funnel fs-2 d-block mb-1 text-secondary opacity-50"></i>
                                        Select an individual examination from the dropdown above to view question-by-question item accuracy.
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>

                <!-- ═══════════════════════════════════════
                     QUESTION ITEM ANALYSIS TABLE (IF EXAM SELECTED)
                ═══════════════════════════════════════ -->
                <c:if test="${not empty questionPerformances}">
                    <div class="card bg-dark border-secondary mb-4">
                        <div class="card-header bg-transparent border-secondary py-3 d-flex justify-content-between align-items-center">
                            <h6 class="text-white fw-bold mb-0">
                                <i class="bi bi-card-checklist text-success me-2"></i> Item Analysis: <c:out value="${selectedExam.title}"/>
                            </h6>
                            <span class="badge bg-secondary">${fn:length(questionPerformances)} Total Questions</span>
                        </div>
                        <div class="table-responsive">
                            <table class="table table-dark table-hover align-middle mb-0 small">
                                <thead>
                                    <tr class="text-secondary border-secondary">
                                        <th style="width: 4%;">#</th>
                                        <th style="width: 40%;">Question Statement &amp; Topic</th>
                                        <th style="width: 10%;">Difficulty</th>
                                        <th style="width: 8%;">Marks</th>
                                        <th style="width: 10%;">Attempts</th>
                                        <th style="width: 10%;">Correct</th>
                                        <th style="width: 18%;">Accuracy Rate</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="qp" items="${questionPerformances}">
                                        <tr>
                                            <td class="text-muted fw-bold">${qp.questionNumber}</td>
                                            <td>
                                                <div class="text-white fw-semibold text-truncate" style="max-width: 400px;">
                                                    <c:out value="${qp.questionText}"/>
                                                </div>
                                                <c:if test="${not empty qp.topic}">
                                                    <span class="badge bg-secondary font-monospace" style="font-size: 0.65rem;">
                                                        <c:out value="${qp.topic}"/>
                                                    </span>
                                                </c:if>
                                            </td>
                                            <td>
                                                <span class="badge ${qp.difficulty == 'EASY' ? 'mcq-badge-difficulty-easy' : (qp.difficulty == 'HARD' ? 'mcq-badge-difficulty-hard' : 'mcq-badge-difficulty-medium')}">
                                                    <c:out value="${qp.difficulty}"/>
                                                </span>
                                            </td>
                                            <td><c:out value="${qp.questionMarks}"/></td>
                                            <td><c:out value="${qp.totalAttempts}"/></td>
                                            <td class="text-success fw-bold"><c:out value="${qp.correctCount}"/></td>
                                            <td>
                                                <div class="d-flex align-items-center gap-2">
                                                    <div class="progress flex-grow-1" style="height: 6px; width: 60px; background: rgba(255,255,255,0.1);">
                                                        <div class="progress-bar ${qp.accuracyBadgeClass}" role="progressbar"
                                                             style="width: ${qp.accuracyPercentage}%;"></div>
                                                    </div>
                                                    <span class="fw-semibold"><fmt:formatNumber value="${qp.accuracyPercentage}" maxFractionDigits="1"/>%</span>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </c:if>

                <!-- ═══════════════════════════════════════
                     STUDENT SUBMISSIONS TABLE
                ═══════════════════════════════════════ -->
                <div class="card bg-dark border-secondary mb-4">
                    <div class="card-header bg-transparent border-secondary py-3 d-flex justify-content-between align-items-center">
                        <h6 class="text-white fw-bold mb-0">
                            <i class="bi bi-people-fill text-info me-2"></i> Candidate Submission Roster
                        </h6>
                        <span class="badge bg-secondary">${totalCount} Matching Records</span>
                    </div>

                    <div class="table-responsive">
                        <table class="table table-dark table-hover align-middle mb-0">
                            <thead>
                                <tr class="text-secondary small border-secondary">
                                    <th style="width: 5%;">#</th>
                                    <th style="width: 25%;">Student Candidate</th>
                                    <th style="width: 25%;">Examination &amp; Subject</th>
                                    <th style="width: 15%;">Marks Obtained</th>
                                    <th style="width: 12%;">Percentage</th>
                                    <th style="width: 8%;">Result</th>
                                    <th style="width: 10%;">Submitted</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty results}">
                                        <c:forEach var="res" items="${results}" varStatus="status">
                                            <tr>
                                                <td class="text-muted small"><c:out value="${(currentPage - 1) * 15 + status.count}"/></td>
                                                <td>
                                                    <div class="fw-semibold text-white"><c:out value="${res.studentName}"/></div>
                                                    <div class="small text-secondary"><c:out value="${res.studentEmail}"/></div>
                                                    <span class="badge bg-secondary font-monospace" style="font-size: 0.7rem;">
                                                        <c:out value="${res.registrationNo != null ? res.registrationNo : 'N/A'}"/>
                                                    </span>
                                                </td>
                                                <td>
                                                    <div class="text-white fw-medium"><c:out value="${res.examTitle}"/></div>
                                                    <span class="badge bg-primary font-monospace"><c:out value="${res.subjectCode}"/></span>
                                                    <small class="text-muted"><c:out value="${res.subjectName}"/></small>
                                                </td>
                                                <td class="fw-bold text-white">
                                                    <fmt:formatNumber value="${res.marksObtained}" maxFractionDigits="1"/> / <c:out value="${res.totalMarks}"/>
                                                </td>
                                                <td>
                                                    <div class="d-flex align-items-center gap-2">
                                                        <div class="progress flex-grow-1" style="height: 6px; width: 60px; background: rgba(255,255,255,0.1);">
                                                            <div class="progress-bar ${res.status == 'PASS' ? 'bg-success' : 'bg-danger'}"
                                                                 role="progressbar"
                                                                 style="width: ${res.percentage}%;"></div>
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
                                                <td class="text-secondary small">
                                                    <c:out value="${res.formattedSubmittedAt}"/>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="7" class="text-center py-5 text-secondary">
                                                <i class="bi bi-inbox fs-1 d-block mb-2 opacity-50"></i>
                                                No student evaluation records found matching this criteria.
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>

                    <!-- Pagination -->
                    <c:if test="${totalPages > 1}">
                        <div class="card-footer bg-transparent border-secondary py-3">
                            <ul class="pagination pagination-sm justify-content-center m-0">
                                <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                    <a class="page-link bg-dark text-white border-secondary"
                                       href="${pageContext.request.contextPath}/teacher/results?page=${currentPage - 1}&examId=${selectedExamId}&search=${search}">
                                        Previous
                                    </a>
                                </li>
                                <c:forEach var="p" begin="1" end="${totalPages}">
                                    <li class="page-item ${currentPage == p ? 'active' : ''}">
                                        <a class="page-link ${currentPage == p ? 'bg-info border-info text-dark fw-bold' : 'bg-dark text-white border-secondary'}"
                                           href="${pageContext.request.contextPath}/teacher/results?page=${p}&examId=${selectedExamId}&search=${search}">
                                            ${p}
                                        </a>
                                    </li>
                                </c:forEach>
                                <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                    <a class="page-link bg-dark text-white border-secondary"
                                       href="${pageContext.request.contextPath}/teacher/results?page=${currentPage + 1}&examId=${selectedExamId}&search=${search}">
                                        Next
                                    </a>
                                </li>
                            </ul>
                        </div>
                    </c:if>
                </div>

            </div>
        </main>
    </div>

    <!-- Bootstrap 5 JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
            crossorigin="anonymous"></script>

    <!-- Sidebar Toggle & Chart.js Config -->
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            const sidebar = document.getElementById('adminSidebar');
            const toggle = document.getElementById('sidebarToggle');
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

            // 1. Chart: Pass vs Fail Doughnut
            const pCount = ${summary.passedCount};
            const fCount = ${summary.failedCount};
            const ctxPF = document.getElementById('teacherPassFailChart');
            if (ctxPF) {
                new Chart(ctxPF, {
                    type: 'doughnut',
                    data: {
                        labels: ['Passed', 'Failed'],
                        datasets: [{
                            data: [pCount, fCount],
                            backgroundColor: ['#2ea043', '#f85149'],
                            borderColor: '#161b22',
                            borderWidth: 2
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        plugins: {
                            legend: {
                                position: 'bottom',
                                labels: { color: '#8b949e' }
                            }
                        }
                    }
                });
            }

            // 2. Chart: Question Accuracy Bar
            <c:if test="${not empty questionPerformances}">
                const qLabels = [
                    <c:forEach var="qp" items="${questionPerformances}" varStatus="status">
                        'Q${qp.questionNumber}'<c:if test="${!status.last}">,</c:if>
                    </c:forEach>
                ];
                const qData = [
                    <c:forEach var="qp" items="${questionPerformances}" varStatus="status">
                        <fmt:formatNumber value="${qp.accuracyPercentage}" maxFractionDigits="1"/><c:if test="${!status.last}">,</c:if>
                    </c:forEach>
                ];
                const ctxQA = document.getElementById('questionAccuracyChart');
                if (ctxQA && qLabels.length > 0) {
                    new Chart(ctxQA, {
                        type: 'bar',
                        data: {
                            labels: qLabels,
                            datasets: [{
                                label: 'Accuracy %',
                                data: qData,
                                backgroundColor: '#a371f7',
                                borderRadius: 4
                            }]
                        },
                        options: {
                            responsive: true,
                            maintainAspectRatio: false,
                            scales: {
                                y: {
                                    beginAtZero: true,
                                    max: 100,
                                    ticks: { color: '#8b949e' },
                                    grid: { color: 'rgba(255,255,255,0.05)' }
                                },
                                x: {
                                    ticks: { color: '#8b949e' },
                                    grid: { display: false }
                                }
                            },
                            plugins: {
                                legend: { display: false }
                            }
                        }
                    });
                }
            </c:if>
        });
    </script>
</body>
</html>
