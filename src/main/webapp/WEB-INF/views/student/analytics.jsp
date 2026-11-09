<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Performance Analytics &amp; Diagnostics | ExamPro</title>

    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <!-- Chart.js 4.4 -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
</head>
<body class="ep-admin-body">

    <div class="ep-admin-wrapper">

        <!-- Sidebar Navigation -->
        <jsp:include page="sidebar.jsp">
            <jsp:param name="active" value="analytics"/>
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
                            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/student/dashboard">Student Portal</a></li>
                            <li class="breadcrumb-item active" aria-current="page">Performance Analytics</li>
                        </ol>
                    </nav>
                </div>
                <div class="ep-topbar-actions">
                    <a href="${pageContext.request.contextPath}/student/results" class="btn btn-outline-secondary btn-sm">
                        <i class="bi bi-award me-1"></i> View Scorecards
                    </a>
                </div>
            </header>

            <div class="p-3 p-md-4">

                <!-- Page Banner -->
                <section class="ep-welcome-banner mb-4" style="background: linear-gradient(135deg, rgba(46, 160, 67, 0.15) 0%, rgba(22, 27, 34, 0.9) 100%); border-color: rgba(46, 160, 67, 0.3);">
                    <div class="row align-items-center">
                        <div class="col-lg-8">
                            <span class="badge px-3 py-1 rounded-pill mb-2" style="background: rgba(46, 160, 67, 0.2); color: #3fb950; border: 1px solid rgba(46, 160, 67, 0.4);">
                                <i class="bi bi-graph-up-arrow me-1"></i> Diagnostic Intelligence
                            </span>
                            <h1 class="h3 fw-bold text-white mb-1">
                                Performance Insights &amp; Learning Analytics
                            </h1>
                            <p class="text-secondary mb-0">
                                Detailed analysis of your academic trajectory, subject strengths, and specific topic areas requiring targeted revision.
                            </p>
                        </div>
                    </div>
                </section>

                <!-- 4 Performance Summary Cards -->
                <section class="row g-3 mb-4">
                    <div class="col-6 col-xl-3">
                        <div class="card bg-dark border-secondary p-3 h-100">
                            <span class="text-secondary small fw-medium">Completed Tests</span>
                            <div class="d-flex align-items-baseline gap-2 mt-2">
                                <h2 class="h2 fw-bold text-white mb-0"><c:out value="${analytics.totalExamsAttempted}"/></h2>
                                <span class="text-muted small">evaluations</span>
                            </div>
                        </div>
                    </div>

                    <div class="col-6 col-xl-3">
                        <div class="card bg-dark border-secondary p-3 h-100">
                            <span class="text-secondary small fw-medium">Overall Average Score</span>
                            <div class="d-flex align-items-baseline gap-2 mt-2">
                                <h2 class="h2 fw-bold text-success mb-0">
                                    <fmt:formatNumber value="${analytics.overallAvgPercentage}" maxFractionDigits="1"/>%
                                </h2>
                                <span class="text-muted small">across subjects</span>
                            </div>
                        </div>
                    </div>

                    <div class="col-6 col-xl-3">
                        <div class="card bg-dark border-secondary p-3 h-100">
                            <span class="text-secondary small fw-medium">Pass vs Fail Record</span>
                            <div class="d-flex align-items-baseline gap-2 mt-2">
                                <h2 class="h2 fw-bold text-white mb-0">
                                    <span class="text-success"><c:out value="${analytics.totalPassed}"/>P</span>
                                    <span class="text-muted small">/</span>
                                    <span class="text-danger"><c:out value="${analytics.totalFailed}"/>F</span>
                                </h2>
                            </div>
                        </div>
                    </div>

                    <div class="col-6 col-xl-3">
                        <div class="card bg-dark border-secondary p-3 h-100">
                            <span class="text-secondary small fw-medium">Personal Best Performance</span>
                            <div class="d-flex align-items-baseline gap-2 mt-2">
                                <h2 class="h2 fw-bold text-warning mb-0">
                                    <fmt:formatNumber value="${analytics.bestScorePercentage}" maxFractionDigits="1"/>%
                                </h2>
                                <span class="text-muted small text-truncate" style="max-width: 120px;"><c:out value="${analytics.bestExamTitle}"/></span>
                            </div>
                        </div>
                    </div>
                </section>

                <!-- ═══════════════════════════════════════
                     CHARTS: Subject Proficiency & Trajectory
                ═══════════════════════════════════════ -->
                <div class="row g-3 mb-4">
                    <!-- Chart 1: Subject Proficiency -->
                    <div class="col-md-6">
                        <div class="card bg-dark border-secondary p-3 h-100">
                            <h6 class="text-white fw-bold mb-3">
                                <i class="bi bi-bar-chart-line text-success me-2"></i> Subject Proficiency (Average %)
                            </h6>
                            <c:choose>
                                <c:when test="${not empty analytics.subjectScores}">
                                    <div class="chart-container-responsive" style="max-height: 220px;">
                                        <canvas id="studentSubjectChart"></canvas>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="text-center py-5 text-secondary">
                                        No published evaluation records yet to chart subject proficiency.
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <!-- Chart 2: Score Trajectory -->
                    <div class="col-md-6">
                        <div class="card bg-dark border-secondary p-3 h-100">
                            <h6 class="text-white fw-bold mb-3">
                                <i class="bi bi-graph-up text-info me-2"></i> Performance Progression Over Time
                            </h6>
                            <c:choose>
                                <c:when test="${not empty analytics.scoreTrajectory}">
                                    <div class="chart-container-responsive" style="max-height: 220px;">
                                        <canvas id="studentTrajectoryChart"></canvas>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="text-center py-5 text-secondary">
                                        Complete multiple examinations to unlock score trajectory analytics.
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>

                <!-- ═══════════════════════════════════════
                     DIAGNOSTIC INSIGHTS: Weak Areas & Strengths
                ═══════════════════════════════════════ -->
                <div class="card bg-dark border-secondary p-4 mb-4">
                    <h5 class="text-white fw-bold mb-3">
                        <i class="bi bi-lightning-charge-fill text-warning me-2"></i> Topic Diagnostics &amp; Learning Recommendations
                    </h5>

                    <div class="row g-4">
                        <!-- Weak Areas -->
                        <div class="col-md-6">
                            <div class="p-3 rounded h-100" style="background: rgba(248, 81, 73, 0.08); border: 1px solid rgba(248, 81, 73, 0.3);">
                                <h6 class="fw-bold text-danger mb-2 d-flex align-items-center gap-2">
                                    <i class="bi bi-exclamation-triangle-fill"></i> Areas Requiring Improvement (&lt; 50% accuracy)
                                </h6>
                                <p class="text-secondary small mb-3">
                                    Based on your question attempts, revision in these topics is recommended:
                                </p>
                                <div>
                                    <c:set var="hasWeak" value="false"/>
                                    <c:forEach var="tp" items="${analytics.topicPerformances}">
                                        <c:if test="${tp.weakArea}">
                                            <c:set var="hasWeak" value="true"/>
                                            <span class="ep-weak-area-pill">
                                                <i class="bi bi-arrow-down-circle"></i>
                                                <strong><c:out value="${tp.topic}"/></strong> (<c:out value="${tp.subjectCode}"/>): <fmt:formatNumber value="${tp.accuracyPercentage}" maxFractionDigits="0"/>%
                                            </span>
                                        </c:if>
                                    </c:forEach>
                                    <c:if test="${!hasWeak}">
                                        <div class="text-success small">
                                            <i class="bi bi-check-circle me-1"></i> Great work! No critical weak areas detected across attempted questions.
                                        </div>
                                    </c:if>
                                </div>
                            </div>
                        </div>

                        <!-- Strengths -->
                        <div class="col-md-6">
                            <div class="p-3 rounded h-100" style="background: rgba(46, 160, 67, 0.08); border: 1px solid rgba(46, 160, 67, 0.3);">
                                <h6 class="fw-bold text-success mb-2 d-flex align-items-center gap-2">
                                    <i class="bi bi-trophy-fill"></i> Demonstrated Mastery (&ge; 70% accuracy)
                                </h6>
                                <p class="text-secondary small mb-3">
                                    You consistently score high accuracy in the following curriculum topics:
                                </p>
                                <div>
                                    <c:set var="hasStrength" value="false"/>
                                    <c:forEach var="tp" items="${analytics.topicPerformances}">
                                        <c:if test="${tp.accuracyPercentage >= 70.0}">
                                            <c:set var="hasStrength" value="true"/>
                                            <span class="ep-strength-pill">
                                                <i class="bi bi-star-fill"></i>
                                                <strong><c:out value="${tp.topic}"/></strong> (<c:out value="${tp.subjectCode}"/>): <fmt:formatNumber value="${tp.accuracyPercentage}" maxFractionDigits="0"/>%
                                            </span>
                                        </c:if>
                                    </c:forEach>
                                    <c:if test="${!hasStrength}">
                                        <div class="text-secondary small">
                                            Keep practicing to build topic masteries above 70%.
                                        </div>
                                    </c:if>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- ═══════════════════════════════════════
                     SUBJECT-WISE PROFICIENCY TABLE
                ═══════════════════════════════════════ -->
                <div class="card bg-dark border-secondary">
                    <div class="card-header bg-transparent border-secondary py-3">
                        <h6 class="text-white fw-bold mb-0">
                            <i class="bi bi-journals text-primary me-2"></i> Subject-Wise Curriculum Performance
                        </h6>
                    </div>
                    <div class="table-responsive">
                        <table class="table table-dark table-hover align-middle mb-0">
                            <thead>
                                <tr class="text-secondary small border-secondary">
                                    <th>Subject Code</th>
                                    <th>Subject Name</th>
                                    <th>Tests Taken</th>
                                    <th>Average Score</th>
                                    <th>Highest Score</th>
                                    <th>Proficiency Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty analytics.subjectScores}">
                                        <c:forEach var="ss" items="${analytics.subjectScores}">
                                            <tr>
                                                <td class="fw-bold text-white"><c:out value="${ss.subjectCode}"/></td>
                                                <td><c:out value="${ss.subjectName}"/></td>
                                                <td><c:out value="${ss.examsTaken}"/></td>
                                                <td>
                                                    <span class="fw-bold text-success"><fmt:formatNumber value="${ss.avgPercentage}" maxFractionDigits="1"/>%</span>
                                                </td>
                                                <td>
                                                    <span class="text-info"><fmt:formatNumber value="${ss.highestPercentage}" maxFractionDigits="1"/>%</span>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${ss.avgPercentage >= 75.0}">
                                                            <span class="badge bg-success-subtle text-success border border-success-subtle">Advanced</span>
                                                        </c:when>
                                                        <c:when test="${ss.avgPercentage >= 50.0}">
                                                            <span class="badge bg-primary-subtle text-primary border border-primary-subtle">Competent</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge bg-warning-subtle text-warning border border-warning-subtle">Needs Focus</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="6" class="text-center py-4 text-secondary">
                                                No curriculum performance data available yet.
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>

            </div>
        </main>
    </div>

    <!-- Bootstrap 5 JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

    <!-- Chart Configuration Script -->
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            // Sidebar toggle
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

            // 1. Subject Proficiency Chart
            <c:if test="${not empty analytics.subjectScores}">
                const subLabels = [
                    <c:forEach var="ss" items="${analytics.subjectScores}" varStatus="status">
                        '<c:out value="${ss.subjectCode}"/>'<c:if test="${!status.last}">,</c:if>
                    </c:forEach>
                ];
                const subData = [
                    <c:forEach var="ss" items="${analytics.subjectScores}" varStatus="status">
                        <fmt:formatNumber value="${ss.avgPercentage}" maxFractionDigits="1"/><c:if test="${!status.last}">,</c:if>
                    </c:forEach>
                ];
                const ctxSub = document.getElementById('studentSubjectChart');
                if (ctxSub) {
                    new Chart(ctxSub, {
                        type: 'bar',
                        data: {
                            labels: subLabels,
                            datasets: [{
                                label: 'Average Score %',
                                data: subData,
                                backgroundColor: '#2ea043',
                                borderRadius: 6
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
                            plugins: { legend: { display: false } }
                        }
                    });
                }
            </c:if>

            // 2. Score Trajectory Chart
            <c:if test="${not empty analytics.scoreTrajectory}">
                const trajLabels = [
                    <c:forEach var="pt" items="${analytics.scoreTrajectory}" varStatus="status">
                        '<c:out value="${pt.formattedDate}"/>'<c:if test="${!status.last}">,</c:if>
                    </c:forEach>
                ];
                const trajData = [
                    <c:forEach var="pt" items="${analytics.scoreTrajectory}" varStatus="status">
                        <fmt:formatNumber value="${pt.percentage}" maxFractionDigits="1"/><c:if test="${!status.last}">,</c:if>
                    </c:forEach>
                ];
                const ctxTraj = document.getElementById('studentTrajectoryChart');
                if (ctxTraj) {
                    new Chart(ctxTraj, {
                        type: 'line',
                        data: {
                            labels: trajLabels,
                            datasets: [{
                                label: 'Score %',
                                data: trajData,
                                borderColor: '#58a6ff',
                                backgroundColor: 'rgba(88, 166, 255, 0.1)',
                                fill: true,
                                tension: 0.3,
                                pointBackgroundColor: '#58a6ff',
                                pointRadius: 5
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
                                    grid: { color: 'rgba(255,255,255,0.05)' }
                                }
                            },
                            plugins: { legend: { display: false } }
                        }
                    });
                }
            </c:if>
        });
    </script>
</body>
</html>

