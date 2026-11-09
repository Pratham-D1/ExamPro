<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Teacher Dashboard | ExamPro</title>

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
                            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/teacher/dashboard">Teacher Portal</a></li>
                            <li class="breadcrumb-item active" aria-current="page">Dashboard</li>
                        </ol>
                    </nav>
                </div>

                <div class="ep-topbar-actions d-flex align-items-center">
                    <jsp:include page="/WEB-INF/views/common/notif_bell.jsp"/>
                    <span class="text-secondary small d-none d-md-inline me-2">
                        <i class="bi bi-calendar3 me-1"></i> <c:out value="${currentDate}"/>
                    </span>
                    <a href="${pageContext.request.contextPath}/teacher/profile" class="btn btn-outline-secondary btn-sm">
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

                <!-- Welcome Hero Banner -->
                <section class="ep-welcome-banner ep-welcome-banner-teacher mb-4">
                    <div class="row align-items-center">
                        <div class="col-lg-8">
                            <span class="badge bg-purple px-3 py-1 rounded-pill mb-2" style="background: rgba(163, 113, 247, 0.2); color: #a371f7; border: 1px solid rgba(163, 113, 247, 0.4);">
                                <i class="bi bi-person-workspace me-1"></i> Faculty Workspace
                            </span>
                            <h1 class="h3 fw-bold text-white mb-1">
                                Welcome back, <c:out value="${currentUser.fullName}"/>!
                            </h1>
                            <p class="text-secondary mb-0">
                                Manage your authored question bank, construct examination tests, and monitor student assessment performance in real time.
                            </p>
                        </div>
                        <div class="col-lg-4 text-lg-end mt-3 mt-lg-0">
                            <a href="${pageContext.request.contextPath}/teacher/questions" class="btn btn-primary btn-sm px-3 py-2 me-2" style="background: #a371f7; border-color: #a371f7;">
                                <i class="bi bi-plus-circle me-1"></i> Add Question
                            </a>
                            <a href="${pageContext.request.contextPath}/teacher/examinations" class="btn btn-outline-light btn-sm px-3 py-2">
                                <i class="bi bi-file-earmark-text me-1"></i> View Exams
                            </a>
                        </div>
                    </div>
                </section>

                <!-- 4 Live Metric Cards -->
                <section class="row g-3 mb-4">
                    <!-- Total Assigned Exams -->
                    <div class="col-6 col-xl-3">
                        <div class="card bg-dark border-secondary p-3 h-100">
                            <div class="d-flex align-items-center justify-content-between mb-2">
                                <span class="text-secondary small fw-medium">Assigned Exams</span>
                                <div class="ep-metric-icon-purple p-2 rounded-3">
                                    <i class="bi bi-file-earmark-text fs-5"></i>
                                </div>
                            </div>
                            <h2 class="h2 fw-bold text-white mb-0">
                                <c:out value="${dashboardStats.totalAssignedExams}"/>
                            </h2>
                            <span class="text-muted small mt-1">Across assigned subjects</span>
                        </div>
                    </div>

                    <!-- Questions in Bank -->
                    <div class="col-6 col-xl-3">
                        <div class="card bg-dark border-secondary p-3 h-100">
                            <div class="d-flex align-items-center justify-content-between mb-2">
                                <span class="text-secondary small fw-medium">My Questions</span>
                                <div class="p-2 rounded-3" style="background: rgba(79, 142, 247, 0.15); color: #4f8ef7;">
                                    <i class="bi bi-patch-question fs-5"></i>
                                </div>
                            </div>
                            <h2 class="h2 fw-bold text-white mb-0">
                                <c:out value="${dashboardStats.totalQuestions}"/>
                            </h2>
                            <span class="text-muted small mt-1">Authored by you</span>
                        </div>
                    </div>

                    <!-- Upcoming Exams -->
                    <div class="col-6 col-xl-3">
                        <div class="card bg-dark border-secondary p-3 h-100">
                            <div class="d-flex align-items-center justify-content-between mb-2">
                                <span class="text-secondary small fw-medium">Upcoming Exams</span>
                                <div class="p-2 rounded-3" style="background: rgba(210, 153, 34, 0.15); color: #d29922;">
                                    <i class="bi bi-calendar-event fs-5"></i>
                                </div>
                            </div>
                            <h2 class="h2 fw-bold text-white mb-0">
                                <c:out value="${dashboardStats.upcomingExams}"/>
                            </h2>
                            <span class="text-muted small mt-1">Scheduled next</span>
                        </div>
                    </div>

                    <!-- Student Attempts -->
                    <div class="col-6 col-xl-3">
                        <div class="card bg-dark border-secondary p-3 h-100">
                            <div class="d-flex align-items-center justify-content-between mb-2">
                                <span class="text-secondary small fw-medium">Student Attempts</span>
                                <div class="p-2 rounded-3" style="background: rgba(63, 185, 80, 0.15); color: #3fb950;">
                                    <i class="bi bi-mortarboard fs-5"></i>
                                </div>
                            </div>
                            <h2 class="h2 fw-bold text-white mb-0">
                                <c:out value="${dashboardStats.totalStudentAttempts}"/>
                            </h2>
                            <span class="text-muted small mt-1">Evaluations submitted</span>
                        </div>
                    </div>
                </section>

                <!-- Middle Row: Assigned Subjects & Question Difficulty Breakdown -->
                <section class="row g-4 mb-4">

                    <!-- Assigned Subjects -->
                    <div class="col-lg-7">
                        <div class="card bg-dark border-secondary h-100">
                            <div class="card-header bg-transparent border-secondary d-flex justify-content-between align-items-center py-3">
                                <h3 class="h6 fw-bold text-white mb-0">
                                    <i class="bi bi-journal-bookmark-fill text-accent me-2"></i> Assigned Teaching Subjects
                                </h3>
                                <span class="badge bg-secondary">
                                    <c:out value="${fn:length(assignedSubjects)}"/> Subjects
                                </span>
                            </div>
                            <div class="card-body p-3">
                                <c:choose>
                                    <c:when test="${not empty assignedSubjects}">
                                        <div class="row g-2">
                                            <c:forEach var="sub" items="${assignedSubjects}">
                                                <div class="col-md-6">
                                                    <div class="p-3 rounded-3 border border-secondary" style="background: var(--ep-bg-primary);">
                                                        <div class="d-flex justify-content-between align-items-start mb-1">
                                                            <span class="badge bg-primary text-uppercase font-monospace">
                                                                <c:out value="${sub.code}"/>
                                                            </span>
                                                            <span class="text-muted small"><c:out value="${sub.credits}"/> Credits</span>
                                                        </div>
                                                        <h4 class="h6 fw-semibold text-white mb-2 text-truncate" title="<c:out value='${sub.name}'/>">
                                                            <c:out value="${sub.name}"/>
                                                        </h4>
                                                        <a href="${pageContext.request.contextPath}/teacher/questions?subjectId=${sub.id}" class="small text-accent text-decoration-none">
                                                            <i class="bi bi-filter me-1"></i> Filter Questions &rarr;
                                                        </a>
                                                    </div>
                                                </div>
                                            </c:forEach>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="text-center py-4 text-muted">
                                            <i class="bi bi-journal-x fs-1 opacity-50 d-block mb-2"></i>
                                            <p class="mb-0">No subjects currently assigned to your account.</p>
                                            <small class="text-secondary">Please contact an administrator to allocate subjects.</small>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>

                    <!-- Question Bank Breakdown & Quick Stats -->
                    <div class="col-lg-5">
                        <div class="card bg-dark border-secondary h-100">
                            <div class="card-header bg-transparent border-secondary py-3">
                                <h3 class="h6 fw-bold text-white mb-0">
                                    <i class="bi bi-pie-chart-fill text-warning me-2"></i> Question Difficulty Breakdown
                                </h3>
                            </div>
                            <div class="card-body p-3 d-flex flex-column justify-content-around">
                                <!-- Easy -->
                                <div class="mb-3">
                                    <div class="d-flex justify-content-between align-items-center mb-1">
                                        <span class="badge mcq-badge-difficulty-easy px-2 py-1">EASY</span>
                                        <span class="text-white fw-bold"><c:out value="${dashboardStats.easyQuestions}"/> questions</span>
                                    </div>
                                    <div class="progress bg-secondary" style="height: 6px;">
                                        <div class="progress-bar bg-success" role="progressbar"
                                             style="width: ${dashboardStats.totalQuestions > 0 ? (dashboardStats.easyQuestions * 100 / dashboardStats.totalQuestions) : 0}%;"></div>
                                    </div>
                                </div>

                                <!-- Medium -->
                                <div class="mb-3">
                                    <div class="d-flex justify-content-between align-items-center mb-1">
                                        <span class="badge mcq-badge-difficulty-medium px-2 py-1">MEDIUM</span>
                                        <span class="text-white fw-bold"><c:out value="${dashboardStats.mediumQuestions}"/> questions</span>
                                    </div>
                                    <div class="progress bg-secondary" style="height: 6px;">
                                        <div class="progress-bar bg-warning" role="progressbar"
                                             style="width: ${dashboardStats.totalQuestions > 0 ? (dashboardStats.mediumQuestions * 100 / dashboardStats.totalQuestions) : 0}%;"></div>
                                    </div>
                                </div>

                                <!-- Hard -->
                                <div class="mb-2">
                                    <div class="d-flex justify-content-between align-items-center mb-1">
                                        <span class="badge mcq-badge-difficulty-hard px-2 py-1">HARD</span>
                                        <span class="text-white fw-bold"><c:out value="${dashboardStats.hardQuestions}"/> questions</span>
                                    </div>
                                    <div class="progress bg-secondary" style="height: 6px;">
                                        <div class="progress-bar bg-danger" role="progressbar"
                                             style="width: ${dashboardStats.totalQuestions > 0 ? (dashboardStats.hardQuestions * 100 / dashboardStats.totalQuestions) : 0}%;"></div>
                                    </div>
                                </div>

                                <div class="mt-3 pt-3 border-top border-secondary text-center">
                                    <a href="${pageContext.request.contextPath}/teacher/questions" class="btn btn-outline-light btn-sm w-100">
                                        <i class="bi bi-collection me-1"></i> Open Question Bank
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </section>

                <!-- Bottom Row: Upcoming Examinations Table -->
                <section class="card bg-dark border-secondary mb-4">
                    <div class="card-header bg-transparent border-secondary d-flex justify-content-between align-items-center py-3">
                        <h3 class="h6 fw-bold text-white mb-0">
                            <i class="bi bi-clock-history text-info me-2"></i> Upcoming Scheduled Examinations
                        </h3>
                        <a href="${pageContext.request.contextPath}/teacher/examinations" class="btn btn-outline-secondary btn-sm">
                            View All Examinations &rarr;
                        </a>
                    </div>
                    <div class="table-responsive">
                        <table class="table table-dark table-hover align-middle mb-0">
                            <thead>
                                <tr class="text-secondary small border-secondary">
                                    <th>Exam Title</th>
                                    <th>Subject</th>
                                    <th>Start Time</th>
                                    <th>Duration</th>
                                    <th>Marks</th>
                                    <th>Readiness</th>
                                    <th>Status</th>
                                    <th class="text-end">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty upcomingExams}">
                                        <c:forEach var="exam" items="${upcomingExams}">
                                            <tr>
                                                <td class="fw-semibold text-white">
                                                    <c:out value="${exam.title}"/>
                                                </td>
                                                <td>
                                                    <span class="badge bg-secondary font-monospace"><c:out value="${exam.subjectCode}"/></span>
                                                    <small class="text-muted ms-1 d-none d-md-inline"><c:out value="${exam.subjectName}"/></small>
                                                </td>
                                                <td class="small text-secondary">
                                                    <c:out value="${exam.formattedStartTime}"/>
                                                </td>
                                                <td class="small text-secondary">
                                                    <c:out value="${exam.durationMinutes}"/> mins
                                                </td>
                                                <td class="small">
                                                    <span class="text-white fw-medium"><c:out value="${exam.totalMarks}"/></span>
                                                    <span class="text-muted">(Pass: <c:out value="${exam.passingMarks}"/>)</span>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${exam.ready}">
                                                            <span class="badge bg-success"><i class="bi bi-check2 me-1"></i>Ready</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge bg-warning text-dark">
                                                                <i class="bi bi-exclamation-circle me-1"></i><c:out value="${exam.assignedQuestionsCount}"/> Qs
                                                            </span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <span class="badge ${exam.statusBadgeClass}"><c:out value="${exam.status}"/></span>
                                                </td>
                                                <td class="text-end">
                                                    <a href="${pageContext.request.contextPath}/teacher/examinations?action=manage&id=${exam.id}"
                                                       class="btn btn-outline-info btn-sm">
                                                        <i class="bi bi-gear-fill me-1"></i> Manage Questions
                                                    </a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="8" class="text-center py-4 text-muted">
                                                No upcoming examinations scheduled for your subjects.
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </section>

            </div>
        </main>
    </div>

    <!-- Bootstrap 5 JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-YvpcrYf0tY3lHB60NNkmXc4s9bIOgUxi8T/jzmLe3zxj8NxO8yVMnN5Z2X5ZZzEB"
            crossorigin="anonymous"></script>

    <!-- Sidebar Toggle Script -->
    <script>
        const sidebarToggle = document.getElementById('sidebarToggle');
        const adminSidebar = document.getElementById('adminSidebar');
        const sidebarBackdrop = document.getElementById('sidebarBackdrop');

        if (sidebarToggle && adminSidebar && sidebarBackdrop) {
            sidebarToggle.addEventListener('click', () => {
                adminSidebar.classList.toggle('show');
                sidebarBackdrop.classList.toggle('show');
            });
            sidebarBackdrop.addEventListener('click', () => {
                adminSidebar.classList.remove('show');
                sidebarBackdrop.classList.remove('show');
            });
        }
    </script>
</body>
</html>
