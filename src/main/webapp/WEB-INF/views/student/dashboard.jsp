<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Student Dashboard | ExamPro</title>

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

    <div class="ep-admin-wrapper">

        <!-- Sidebar Navigation -->
        <jsp:include page="sidebar.jsp">
            <jsp:param name="active" value="dashboard"/>
        </jsp:include>

        <!-- Main Content Wrapper -->
        <main class="ep-admin-main">

            <!-- Top Navigation Bar -->
            <header class="ep-admin-topbar">
                <div class="d-flex align-items-center gap-3">
                    <button class="btn btn-outline-secondary btn-sm d-lg-none" id="sidebarToggle" type="button" aria-label="Toggle Navigation">
                        <i class="bi bi-list fs-5"></i>
                    </button>
                    <nav aria-label="breadcrumb">
                        <ol class="breadcrumb m-0 ep-topbar-breadcrumb">
                            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/student/dashboard">Student Portal</a></li>
                            <li class="breadcrumb-item active" aria-current="page">Dashboard</li>
                        </ol>
                    </nav>
                </div>

                <div class="ep-topbar-actions d-flex align-items-center">
                    <jsp:include page="/WEB-INF/views/common/notif_bell.jsp"/>
                    <span class="text-secondary small d-none d-md-inline me-2">
                        <i class="bi bi-person-vcard me-1"></i> Reg No: <strong><c:out value="${studentProfile != null && studentProfile.registrationNo != null ? studentProfile.registrationNo : 'N/A'}"/></strong>
                    </span>
                    <a href="${pageContext.request.contextPath}/student/profile" class="btn btn-outline-secondary btn-sm">
                        <i class="bi bi-person-circle me-1"></i> Profile
                    </a>
                </div>
            </header>

            <!-- Dashboard Body -->
            <div class="p-3 p-md-4">

                <!-- Flash Notifications -->
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
                <c:if test="${not empty flashWarning}">
                    <div class="alert alert-warning alert-dismissible fade show d-flex align-items-center mb-4" role="alert">
                        <i class="bi bi-exclamation-circle-fill fs-5 me-2"></i>
                        <div><c:out value="${flashWarning}"/></div>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:if>

                <!-- Welcome Hero Banner -->
                <section class="ep-welcome-banner mb-4" style="background: linear-gradient(135deg, rgba(46, 160, 67, 0.15) 0%, rgba(22, 27, 34, 0.9) 100%); border-color: rgba(46, 160, 67, 0.3);">
                    <div class="row align-items-center">
                        <div class="col-lg-8">
                            <span class="badge px-3 py-1 rounded-pill mb-2" style="background: rgba(46, 160, 67, 0.2); color: #3fb950; border: 1px solid rgba(46, 160, 67, 0.4);">
                                <i class="bi bi-mortarboard-fill me-1"></i> Candidate Assessment Hub
                            </span>
                            <h1 class="h3 fw-bold text-white mb-1">
                                Welcome, <c:out value="${currentUser.fullName}"/>!
                            </h1>
                            <p class="text-secondary mb-0">
                                Your personal examination workspace. View available tests, take timed assessments with instant autosave, and track your evaluation scorecards.
                            </p>
                        </div>
                        <div class="col-lg-4 text-lg-end mt-3 mt-lg-0">
                            <a href="${pageContext.request.contextPath}/student/exams" class="btn btn-success btn-sm px-3 py-2 me-2" style="background: #2ea043; border-color: #2ea043;">
                                <i class="bi bi-pencil-square me-1"></i> Take Exam
                            </a>
                            <a href="${pageContext.request.contextPath}/student/results" class="btn btn-outline-light btn-sm px-3 py-2">
                                <i class="bi bi-award me-1"></i> View Results
                            </a>
                        </div>
                    </div>
                </section>

                <!-- 4 Live Metric Cards -->
                <section class="row g-3 mb-4">
                    <!-- Available Exams -->
                    <div class="col-6 col-xl-3">
                        <div class="ep-metric-card">
                            <div class="d-flex align-items-center justify-content-between">
                                <div>
                                    <div class="ep-metric-label">Available Exams</div>
                                    <div class="ep-metric-value text-success">
                                        <c:out value="${stats != null ? stats.availableExamsCount : 0}"/>
                                    </div>
                                    <div class="ep-metric-subtext">Active & ready to start</div>
                                </div>
                                <div class="ep-metric-icon ep-metric-icon-student">
                                    <i class="bi bi-journal-check fs-4"></i>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Upcoming Exams -->
                    <div class="col-6 col-xl-3">
                        <div class="ep-metric-card">
                            <div class="d-flex align-items-center justify-content-between">
                                <div>
                                    <div class="ep-metric-label">Upcoming Scheduled</div>
                                    <div class="ep-metric-value text-info">
                                        <c:out value="${stats != null ? stats.upcomingExamsCount : 0}"/>
                                    </div>
                                    <div class="ep-metric-subtext">Starting in future</div>
                                </div>
                                <div class="ep-metric-icon" style="background: rgba(88, 166, 255, 0.15); color: #58a6ff;">
                                    <i class="bi bi-clock-history fs-4"></i>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Completed Attempts -->
                    <div class="col-6 col-xl-3">
                        <div class="ep-metric-card">
                            <div class="d-flex align-items-center justify-content-between">
                                <div>
                                    <div class="ep-metric-label">Completed Tests</div>
                                    <div class="ep-metric-value text-purple" style="color: #a371f7;">
                                        <c:out value="${stats != null ? stats.completedExamsCount : 0}"/>
                                    </div>
                                    <div class="ep-metric-subtext">Successfully submitted</div>
                                </div>
                                <div class="ep-metric-icon" style="background: rgba(163, 113, 247, 0.15); color: #a371f7;">
                                    <i class="bi bi-clipboard2-check-fill fs-4"></i>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Average Score / Pass Rate -->
                    <div class="col-6 col-xl-3">
                        <div class="ep-metric-card">
                            <div class="d-flex align-items-center justify-content-between">
                                <div>
                                    <div class="ep-metric-label">Average Score</div>
                                    <div class="ep-metric-value text-warning">
                                        <fmt:formatNumber value="${stats != null ? stats.averagePercentage : 0.0}" maxFractionDigits="1"/>%
                                    </div>
                                    <div class="ep-metric-subtext">
                                        <span class="text-success fw-bold"><c:out value="${stats != null ? stats.passedCount : 0}"/> Pass</span> &bull; 
                                        <span class="text-danger fw-bold"><c:out value="${stats != null ? stats.failedCount : 0}"/> Fail</span>
                                    </div>
                                </div>
                                <div class="ep-metric-icon" style="background: rgba(210, 153, 34, 0.15); color: #d29922;">
                                    <i class="bi bi-trophy-fill fs-4"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </section>

                <div class="row g-4 mb-4">
                    <!-- Available Examinations Section -->
                    <div class="col-lg-8">
                        <div class="card bg-dark border-secondary h-100">
                            <div class="card-header bg-transparent border-secondary d-flex justify-content-between align-items-center py-3">
                                <h6 class="mb-0 fw-semibold text-white">
                                    <i class="bi bi-play-circle-fill text-success me-2"></i> Available Examinations Now
                                </h6>
                                <a href="${pageContext.request.contextPath}/student/exams" class="btn btn-outline-secondary btn-sm">
                                    View All <i class="bi bi-arrow-right ms-1"></i>
                                </a>
                            </div>
                            <div class="card-body p-0">
                                <c:choose>
                                    <c:when test="${not empty availableExams}">
                                        <div class="table-responsive">
                                            <table class="table table-dark table-hover align-middle mb-0">
                                                <thead>
                                                    <tr class="text-secondary small">
                                                        <th class="ps-3">Examination</th>
                                                        <th>Subject</th>
                                                        <th>Duration</th>
                                                        <th>Marks</th>
                                                        <th class="text-end pe-3">Action</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <c:forEach var="exam" items="${availableExams}">
                                                        <tr>
                                                            <td class="ps-3">
                                                                <div class="fw-semibold text-white"><c:out value="${exam.title}"/></div>
                                                                <div class="small text-muted"><c:out value="${exam.formattedDuration}"/></div>
                                                            </td>
                                                            <td>
                                                                <span class="badge bg-secondary">
                                                                    <c:out value="${exam.subjectCode}"/>
                                                                </span>
                                                                <div class="small text-secondary"><c:out value="${exam.subjectName}"/></div>
                                                            </td>
                                                            <td><c:out value="${exam.durationMinutes}"/> mins</td>
                                                            <td>
                                                                <span class="text-white fw-bold"><c:out value="${exam.totalMarks}"/></span>
                                                                <span class="small text-muted">(Pass: <c:out value="${exam.passingMarks}"/>)</span>
                                                            </td>
                                                            <td class="text-end pe-3">
                                                                <a href="${pageContext.request.contextPath}/student/exam/instructions?examId=${exam.id}"
                                                                   class="btn btn-success btn-sm px-3" style="background: #2ea043; border-color: #2ea043;">
                                                                    <i class="bi bi-play-fill me-1"></i> Start
                                                                </a>
                                                            </td>
                                                        </tr>
                                                    </c:forEach>
                                                </tbody>
                                            </table>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="text-center py-5">
                                            <i class="bi bi-check2-circle fs-1 text-secondary opacity-50"></i>
                                            <p class="text-secondary mt-2 mb-0">No active examinations waiting for you right now.</p>
                                            <small class="text-muted">Check upcoming scheduled dates below or review past scorecards.</small>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>

                    <!-- Upcoming Examinations Section -->
                    <div class="col-lg-4">
                        <div class="card bg-dark border-secondary h-100">
                            <div class="card-header bg-transparent border-secondary py-3">
                                <h6 class="mb-0 fw-semibold text-white">
                                    <i class="bi bi-calendar-event text-info me-2"></i> Upcoming Schedule
                                </h6>
                            </div>
                            <div class="card-body p-3">
                                <c:choose>
                                    <c:when test="${not empty upcomingExams}">
                                        <div class="d-flex flex-column gap-3">
                                            <c:forEach var="upcoming" items="${upcomingExams}">
                                                <div class="p-3 rounded" style="background: var(--ep-bg-secondary); border: 1px solid var(--ep-bg-tertiary);">
                                                    <div class="d-flex justify-content-between align-items-start mb-1">
                                                        <span class="badge bg-secondary"><c:out value="${upcoming.subjectCode}"/></span>
                                                        <span class="badge bg-info-subtle text-info"><c:out value="${upcoming.durationMinutes}"/>m</span>
                                                    </div>
                                                    <div class="fw-semibold text-white text-truncate"><c:out value="${upcoming.title}"/></div>
                                                    <div class="small text-secondary mt-1">
                                                        <i class="bi bi-clock me-1"></i> <c:out value="${upcoming.formattedStartTime}"/>
                                                    </div>
                                                </div>
                                            </c:forEach>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="text-center py-4">
                                            <i class="bi bi-calendar2-check fs-2 text-secondary opacity-50"></i>
                                            <p class="text-secondary small mt-2 mb-0">No upcoming tests currently on calendar.</p>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Recent Results Section -->
                <section class="card bg-dark border-secondary mb-4">
                    <div class="card-header bg-transparent border-secondary d-flex justify-content-between align-items-center py-3">
                        <h6 class="mb-0 fw-semibold text-white">
                            <i class="bi bi-award-fill text-warning me-2"></i> Recent Examination Scorecards
                        </h6>
                        <a href="${pageContext.request.contextPath}/student/results" class="btn btn-outline-secondary btn-sm">
                            Full Results History <i class="bi bi-arrow-right ms-1"></i>
                        </a>
                    </div>
                    <div class="card-body p-0">
                        <c:choose>
                            <c:when test="${not empty recentResults}">
                                <div class="table-responsive">
                                    <table class="table table-dark table-hover align-middle mb-0">
                                        <thead>
                                            <tr class="text-secondary small">
                                                <th class="ps-3">Examination</th>
                                                <th>Subject</th>
                                                <th>Submitted On</th>
                                                <th>Score</th>
                                                <th>Percentage</th>
                                                <th>Status</th>
                                                <th class="text-end pe-3">Review</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="res" items="${recentResults}">
                                                <tr>
                                                    <td class="ps-3 fw-semibold text-white">
                                                        <c:out value="${res.examTitle}"/>
                                                    </td>
                                                    <td>
                                                        <span class="badge bg-secondary"><c:out value="${res.subjectCode}"/></span>
                                                        <span class="small text-secondary ms-1"><c:out value="${res.subjectName}"/></span>
                                                    </td>
                                                    <td class="small text-secondary">
                                                        <c:out value="${res.formattedSubmittedAt}"/>
                                                    </td>
                                                    <td class="fw-bold text-white">
                                                        <fmt:formatNumber value="${res.marksObtained}" maxFractionDigits="1"/> / <c:out value="${res.totalMarks}"/>
                                                    </td>
                                                    <td>
                                                        <div class="d-flex align-items-center gap-2">
                                                            <div class="progress flex-grow-1" style="height: 6px; width: 60px; background: rgba(255,255,255,0.1);">
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
                                                    <td class="text-end pe-3">
                                                        <a href="${pageContext.request.contextPath}/student/result-detail?examId=${res.examinationId}"
                                                           class="btn btn-outline-info btn-sm">
                                                            <i class="bi bi-eye me-1"></i> Scorecard
                                                        </a>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="text-center py-5">
                                    <i class="bi bi-journal-x fs-1 text-secondary opacity-50"></i>
                                    <p class="text-secondary mt-2 mb-0">You have not completed any examinations yet.</p>
                                    <small class="text-muted">Take your first examination above to view your scorecards and analytics.</small>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </section>

            </div>
        </main>
    </div>

    <!-- Bootstrap 5 JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
            crossorigin="anonymous"></script>

    <!-- Sidebar Toggle Script -->
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
        });
    </script>
</body>
</html>
