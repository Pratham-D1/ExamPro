<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.exampro.model.User" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="ExamPro Administrator Dashboard - Real-time metrics and portal oversight.">
    <title>Administrator Dashboard | ExamPro</title>

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
            <jsp:param name="active" value="dashboard"/>
        </jsp:include>

        <!-- ═══════════════════════════════════════
             MAIN DASHBOARD CONTENT
        ═══════════════════════════════════════ -->
        <div class="ep-admin-main">

            <!-- Top Navigation Bar -->
            <header class="ep-admin-topbar">
                <div class="d-flex align-items-center gap-3">
                    <!-- Mobile Hamburger Toggle -->
                    <button class="btn btn-outline-secondary btn-sm d-lg-none" id="sidebarToggle" type="button" aria-label="Toggle sidebar navigation">
                        <i class="bi bi-list fs-5"></i>
                    </button>

                    <!-- Breadcrumbs -->
                    <nav aria-label="breadcrumb">
                        <ol class="ep-topbar-breadcrumb list-unstyled m-0">
                            <li><a href="${pageContext.request.contextPath}/"><i class="bi bi-house-door me-1"></i>Home</a></li>
                            <li class="text-muted">/</li>
                            <li class="active" aria-current="page">Admin Dashboard</li>
                        </ol>
                    </nav>
                </div>

                <!-- Topbar Actions & Profile -->
                <div class="ep-topbar-actions d-flex align-items-center">
                    <jsp:include page="/WEB-INF/views/common/notif_bell.jsp"/>
                    <a href="${pageContext.request.contextPath}/health" class="btn btn-outline-secondary btn-sm d-none d-md-inline-flex align-items-center gap-1" title="Application Health">
                        <i class="bi bi-heart-pulse-fill text-danger"></i>
                        <span>Health</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/db-test" class="btn btn-outline-secondary btn-sm d-none d-md-inline-flex align-items-center gap-1" title="Database Connection Diagnostics">
                        <i class="bi bi-database-check text-info"></i>
                        <span>DB Test</span>
                    </a>

                    <!-- Admin Profile Badge -->
                    <div class="d-flex align-items-center gap-2 ps-2 border-start border-secondary">
                        <span class="badge badge-role-admin d-none d-sm-inline-block">
                            <i class="bi bi-shield-lock-fill me-1"></i>ADMIN
                        </span>
                        <span class="text-white small fw-medium d-none d-sm-inline-block">
                            <c:out value="${not empty sessionScope.userName ? sessionScope.userName : currentUser.fullName}"/>
                        </span>

                        <!-- Direct Topbar Logout Button -->
                        <form action="${pageContext.request.contextPath}/logout" method="post" class="m-0">
                            <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                            <button type="submit" class="btn btn-outline-danger btn-sm" id="topbarLogoutBtn" title="Sign out of ExamPro">
                                <i class="bi bi-box-arrow-right"></i>
                                <span class="d-none d-md-inline ms-1">Sign Out</span>
                            </button>
                        </form>
                    </div>
                </div>
            </header>

            <!-- Dashboard Content Container -->
            <main class="container-fluid p-4 p-lg-5">

                <!-- Database Error Notice (If Connection or Query Fails) -->
                <c:if test="${not empty dbError}">
                    <div class="alert alert-danger alert-dismissible fade show d-flex align-items-center mb-4" role="alert">
                        <i class="bi bi-exclamation-triangle-fill fs-4 me-3"></i>
                        <div>
                            <strong>Database Communication Notice:</strong>
                            <div><c:out value="${dbError}"/></div>
                        </div>
                        <button type="button" class="btn-close ms-auto" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:if>

                <!-- Welcome Header Banner -->
                <section class="ep-welcome-banner" aria-labelledby="dashboardWelcomeHeading">
                    <div class="row align-items-center position-relative" style="z-index: 1;">
                        <div class="col-xl-8 col-lg-7">
                            <div class="d-flex align-items-center gap-2 mb-2">
                                <span class="badge role-badge-admin px-3 py-1 rounded-pill" style="background: rgba(240, 136, 62, 0.2); color: #f0883e; border: 1px solid rgba(240, 136, 62, 0.4);">
                                    <i class="bi bi-shield-check me-1"></i>System Administration
                                </span>
                                <span class="text-secondary small">
                                    <i class="bi bi-calendar3 me-1"></i><c:out value="${currentDate}"/>
                                </span>
                            </div>
                            <h1 class="h2 fw-bold text-white mb-2" id="dashboardWelcomeHeading">
                                Welcome back, <c:out value="${not empty sessionScope.userName ? sessionScope.userName : currentUser.fullName}"/>!
                            </h1>
                            <p class="text-secondary mb-0" style="max-width: 650px;">
                                Central administration dashboard for monitoring students, faculty accounts, active examinations, and database integrity.
                            </p>
                        </div>
                        <div class="col-xl-4 col-lg-5 text-lg-end mt-4 mt-lg-0">
                            <div class="d-inline-flex flex-column align-items-lg-end text-start">
                                <span class="badge bg-dark border border-secondary text-light px-3 py-2 mb-2 font-monospace small">
                                    <i class="bi bi-person-circle me-1 text-warning"></i>
                                    <code><c:out value="${not empty sessionScope.userEmail ? sessionScope.userEmail : currentUser.email}"/></code>
                                </span>
                                <div class="text-muted small">
                                    Session: <span class="text-success"><i class="bi bi-circle-fill" style="font-size: 0.55rem;"></i> Active &amp; CSRF Protected</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </section>

                <!-- ═══════════════════════════════════════
                     STATISTICS CARDS (6 Key Metrics)
                ═══════════════════════════════════════ -->
                <section class="mb-5" aria-label="Key Performance Indicators">
                    <div class="row g-4">

                        <!-- Card 1: Total Students (Live from MySQL users) -->
                        <div class="col-sm-6 col-xl-4 col-xxl-2">
                            <div class="ep-stat-card card-students">
                                <div>
                                    <div class="ep-stat-header">
                                        <div>
                                            <div class="ep-stat-title">Total Students</div>
                                            <div class="ep-stat-value text-success" id="statTotalStudents">
                                                <c:out value="${dashboardStats.totalStudents}"/>
                                            </div>
                                        </div>
                                        <div class="ep-stat-icon-wrapper icon-students">
                                            <i class="bi bi-people-fill"></i>
                                        </div>
                                    </div>
                                </div>
                                <div class="ep-stat-footer">
                                    <span class="text-secondary">
                                        <i class="bi bi-check-circle text-success me-1"></i>
                                        <strong><c:out value="${dashboardStats.activeStudents}"/></strong> Active
                                    </span>
                                    <a href="${pageContext.request.contextPath}/admin/students" class="text-decoration-none small text-success">
                                        Manage &rarr;
                                    </a>
                                </div>
                            </div>
                        </div>

                        <!-- Card 2: Total Teachers (Live from MySQL users) -->
                        <div class="col-sm-6 col-xl-4 col-xxl-2">
                            <div class="ep-stat-card card-teachers">
                                <div>
                                    <div class="ep-stat-header">
                                        <div>
                                            <div class="ep-stat-title">Total Teachers</div>
                                            <div class="ep-stat-value" style="color: #a371f7;" id="statTotalTeachers">
                                                <c:out value="${dashboardStats.totalTeachers}"/>
                                            </div>
                                        </div>
                                        <div class="ep-stat-icon-wrapper icon-teachers">
                                            <i class="bi bi-person-video3"></i>
                                        </div>
                                    </div>
                                </div>
                                <div class="ep-stat-footer">
                                    <span class="text-secondary">
                                        <i class="bi bi-check-circle me-1" style="color: #a371f7;"></i>
                                        <strong><c:out value="${dashboardStats.activeTeachers}"/></strong> Active
                                    </span>
                                    <a href="${pageContext.request.contextPath}/admin/teachers" class="text-decoration-none small" style="color: #a371f7;">
                                        Manage &rarr;
                                    </a>
                                </div>
                            </div>
                        </div>

                        <!-- Card 3: Total Subjects (Live from MySQL subjects) -->
                        <div class="col-sm-6 col-xl-4 col-xxl-2">
                            <div class="ep-stat-card card-subjects">
                                <div>
                                    <div class="ep-stat-header">
                                        <div>
                                            <div class="ep-stat-title">Total Subjects</div>
                                            <div class="ep-stat-value text-info" id="statTotalSubjects">
                                                <c:out value="${dashboardStats.totalSubjects != null ? dashboardStats.totalSubjects : 0}"/>
                                            </div>
                                        </div>
                                        <div class="ep-stat-icon-wrapper icon-subjects">
                                            <i class="bi bi-journal-bookmark-fill"></i>
                                        </div>
                                    </div>
                                </div>
                                <div class="ep-stat-footer">
                                    <span class="text-secondary">Curriculum courses</span>
                                    <a href="${pageContext.request.contextPath}/admin/subjects" class="text-decoration-none small text-info">
                                        Manage &rarr;
                                    </a>
                                </div>
                            </div>
                        </div>

                        <!-- Card 4: Total Examinations (Live from MySQL examinations) -->
                        <div class="col-sm-6 col-xl-4 col-xxl-2">
                            <div class="ep-stat-card card-exams">
                                <div>
                                    <div class="ep-stat-header">
                                        <div>
                                            <div class="ep-stat-title">Total Exams</div>
                                            <div class="ep-stat-value text-warning" id="statTotalExams">
                                                <c:out value="${dashboardStats.totalExams != null ? dashboardStats.totalExams : 0}"/>
                                            </div>
                                        </div>
                                        <div class="ep-stat-icon-wrapper icon-exams">
                                            <i class="bi bi-file-earmark-text-fill"></i>
                                        </div>
                                    </div>
                                </div>
                                <div class="ep-stat-footer">
                                    <span class="text-secondary">All configured exams</span>
                                    <a href="${pageContext.request.contextPath}/admin/examinations" class="text-decoration-none small text-warning">
                                        Manage &rarr;
                                    </a>
                                </div>
                            </div>
                        </div>

                        <!-- Card 5: Upcoming Examinations -->
                        <div class="col-sm-6 col-xl-4 col-xxl-2">
                            <div class="ep-stat-card" style="border-top: 3px solid #58a6ff;">
                                <div>
                                    <div class="ep-stat-header">
                                        <div>
                                            <div class="ep-stat-title">Upcoming Exams</div>
                                            <div class="ep-stat-value text-primary" id="statUpcomingExams">
                                                <c:out value="${dashboardStats.upcomingExams != null ? dashboardStats.upcomingExams : 0}"/>
                                            </div>
                                        </div>
                                        <div class="ep-stat-icon-wrapper" style="background: rgba(88, 166, 255, 0.12); color: #58a6ff; border: 1px solid rgba(88, 166, 255, 0.25);">
                                            <i class="bi bi-calendar-event-fill"></i>
                                        </div>
                                    </div>
                                </div>
                                <div class="ep-stat-footer">
                                    <span class="text-secondary">Scheduled tests</span>
                                    <a href="${pageContext.request.contextPath}/admin/examinations?status=SCHEDULED" class="text-decoration-none small text-primary">
                                        View &rarr;
                                    </a>
                                </div>
                            </div>
                        </div>

                        <!-- Card 6: Completed Examinations -->
                        <div class="col-sm-6 col-xl-4 col-xxl-2">
                            <div class="ep-stat-card" style="border-top: 3px solid #238636;">
                                <div>
                                    <div class="ep-stat-header">
                                        <div>
                                            <div class="ep-stat-title">Completed Exams</div>
                                            <div class="ep-stat-value text-success" id="statCompletedExams">
                                                <c:out value="${dashboardStats.completedExams != null ? dashboardStats.completedExams : 0}"/>
                                            </div>
                                        </div>
                                        <div class="ep-stat-icon-wrapper" style="background: rgba(46, 160, 67, 0.12); color: #3fb950; border: 1px solid rgba(46, 160, 67, 0.25);">
                                            <i class="bi bi-check2-circle"></i>
                                        </div>
                                    </div>
                                </div>
                                <div class="ep-stat-footer">
                                    <span class="text-secondary">Completed events</span>
                                    <a href="${pageContext.request.contextPath}/admin/examinations?status=COMPLETED" class="text-decoration-none small text-success">
                                        View &rarr;
                                    </a>
                                </div>
                            </div>
                        </div>

                    </div>
                </section>

                <!-- ═══════════════════════════════════════
                     QUICK ACTIONS SECTION
                ═══════════════════════════════════════ -->
                <section class="mb-5" aria-labelledby="quickActionsHeading">
                    <div class="d-flex align-items-center justify-content-between mb-3">
                        <h2 class="h5 fw-bold text-white m-0" id="quickActionsHeading">
                            <i class="bi bi-lightning-charge-fill text-warning me-2"></i>Quick Actions
                        </h2>
                        <span class="text-muted small">Administrative Shortcuts</span>
                    </div>

                    <div class="row g-3">
                        <div class="col-6 col-md-3 col-xl-2">
                            <a href="${pageContext.request.contextPath}/admin/students" class="ep-action-btn">
                                <i class="bi bi-person-plus-fill text-success"></i>
                                <span class="small fw-semibold">Students</span>
                            </a>
                        </div>
                        <div class="col-6 col-md-3 col-xl-2">
                            <a href="${pageContext.request.contextPath}/admin/teachers" class="ep-action-btn">
                                <i class="bi bi-person-workspace" style="color: #a371f7;"></i>
                                <span class="small fw-semibold">Teachers</span>
                            </a>
                        </div>
                        <div class="col-6 col-md-3 col-xl-2">
                            <a href="${pageContext.request.contextPath}/admin/subjects" class="ep-action-btn">
                                <i class="bi bi-journal-plus text-info"></i>
                                <span class="small fw-semibold">Subjects</span>
                            </a>
                        </div>
                        <div class="col-6 col-md-3 col-xl-2">
                            <a href="${pageContext.request.contextPath}/admin/examinations" class="ep-action-btn">
                                <i class="bi bi-calendar-plus text-warning"></i>
                                <span class="small fw-semibold">Examinations</span>
                            </a>
                        </div>
                        <div class="col-6 col-md-3 col-xl-2">
                            <a href="${pageContext.request.contextPath}/admin/questions" class="ep-action-btn">
                                <i class="bi bi-patch-question-fill text-accent"></i>
                                <span class="small fw-semibold">Question Bank</span>
                            </a>
                        </div>
                        <div class="col-6 col-md-3 col-xl-2">
                            <a href="${pageContext.request.contextPath}/admin/results" class="ep-action-btn">
                                <i class="bi bi-bar-chart-line-fill text-primary"></i>
                                <span class="small fw-semibold">Results &amp; Analytics</span>
                            </a>
                        </div>
                        <div class="col-6 col-md-3 col-xl-2">
                            <a href="${pageContext.request.contextPath}/admin/announcements" class="ep-action-btn">
                                <i class="bi bi-megaphone-fill text-warning"></i>
                                <span class="small fw-semibold">Announcements</span>
                            </a>
                        </div>
                        <div class="col-6 col-md-3 col-xl-2">
                            <a href="${pageContext.request.contextPath}/admin/users" class="ep-action-btn">
                                <i class="bi bi-person-badge-fill text-info"></i>
                                <span class="small fw-semibold">User Directory</span>
                            </a>
                        </div>
                        <div class="col-6 col-md-3 col-xl-2">
                            <a href="${pageContext.request.contextPath}/admin/audit-logs" class="ep-action-btn">
                                <i class="bi bi-shield-check text-success"></i>
                                <span class="small fw-semibold">Security &amp; Audit</span>
                            </a>
                        </div>
                        <div class="col-6 col-md-3 col-xl-2">
                            <a href="${pageContext.request.contextPath}/admin/system" class="ep-action-btn">
                                <i class="bi bi-cpu-fill text-danger"></i>
                                <span class="small fw-semibold">System Overview</span>
                            </a>
                        </div>
                        <div class="col-6 col-md-3 col-xl-2">
                            <a href="${pageContext.request.contextPath}/health" class="ep-action-btn">
                                <i class="bi bi-heart-pulse text-danger"></i>
                                <span class="small fw-semibold">System Health</span>
                            </a>
                        </div>
                        <div class="col-6 col-md-3 col-xl-2">
                            <a href="${pageContext.request.contextPath}/db-test" class="ep-action-btn">
                                <i class="bi bi-database-check text-info"></i>
                                <span class="small fw-semibold">DB Diagnostics</span>
                            </a>
                        </div>
                    </div>
                </section>

                <!-- ═══════════════════════════════════════
                     RECENT ACTIVITY & SYSTEM OVERVIEW
                ═══════════════════════════════════════ -->
                <div class="row g-4">

                    <!-- Recent Activity: Registered Users Stream -->
                    <div class="col-xl-8">
                        <section class="ep-card h-100" aria-labelledby="recentActivityHeading">
                            <div class="ep-card-header">
                                <h3 class="ep-card-title" id="recentActivityHeading">
                                    <i class="bi bi-clock-history text-warning"></i>
                                    Recent Accounts &amp; Registrations
                                </h3>
                                <span class="badge bg-dark border border-secondary text-secondary small">
                                    Live from <code>users</code>
                                </span>
                            </div>

                            <div class="table-responsive">
                                <c:choose>
                                    <c:when test="${not empty dashboardStats.recentUsers}">
                                        <table class="ep-table-custom" aria-label="Recent user accounts">
                                            <thead>
                                                <tr>
                                                    <th scope="col">User</th>
                                                    <th scope="col">Role</th>
                                                    <th scope="col">Email</th>
                                                    <th scope="col">Status</th>
                                                    <th scope="col">Registered</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="u" items="${dashboardStats.recentUsers}">
                                                    <tr>
                                                        <td>
                                                            <div class="d-flex align-items-center gap-2">
                                                                <div class="ep-admin-avatar" style="width: 32px; height: 32px; font-size: 0.8rem;">
                                                                    <c:out value="${fn:substring(u.fullName, 0, 1)}"/>
                                                                </div>
                                                                <span class="fw-semibold text-white">
                                                                    <c:out value="${u.fullName}"/>
                                                                </span>
                                                            </div>
                                                        </td>
                                                        <td>
                                                            <c:choose>
                                                                <c:when test="${u.role eq 'ADMIN'}">
                                                                    <span class="badge badge-role-admin">
                                                                        <i class="bi bi-shield-lock-fill me-1"></i>ADMIN
                                                                    </span>
                                                                </c:when>
                                                                <c:when test="${u.role eq 'TEACHER'}">
                                                                    <span class="badge badge-role-teacher">
                                                                        <i class="bi bi-person-video3 me-1"></i>TEACHER
                                                                    </span>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <span class="badge badge-role-student">
                                                                        <i class="bi bi-mortarboard me-1"></i>STUDENT
                                                                    </span>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                        <td class="font-monospace text-secondary small">
                                                            <c:out value="${u.email}"/>
                                                        </td>
                                                        <td>
                                                            <c:choose>
                                                                <c:when test="${u.active}">
                                                                    <span class="badge bg-success bg-opacity-10 text-success border border-success border-opacity-25 px-2 py-1">
                                                                        <i class="bi bi-check-circle-fill me-1"></i>Active
                                                                    </span>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <span class="badge bg-danger bg-opacity-10 text-danger border border-danger border-opacity-25 px-2 py-1">
                                                                        <i class="bi bi-slash-circle me-1"></i>Inactive
                                                                    </span>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                        <td class="text-secondary small">
                                                            <c:out value="${u.formattedCreatedAt}"/>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </c:when>
                                    <c:otherwise>
                                        <!-- Accessible Empty State -->
                                        <div class="text-center py-5 text-muted">
                                            <i class="bi bi-people display-4 d-block mb-3 opacity-50"></i>
                                            <h4 class="h6 text-white mb-1">No Accounts Recorded</h4>
                                            <p class="small text-secondary mb-0">No registered users found in the database table.</p>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </section>
                    </div>

                    <!-- Security & System Environment Status -->
                    <div class="col-xl-4">
                        <section class="ep-card h-100" aria-labelledby="systemStatusHeading">
                            <div class="ep-card-header">
                                <h3 class="ep-card-title" id="systemStatusHeading">
                                    <i class="bi bi-shield-check text-success"></i>
                                    Security &amp; Environment
                                </h3>
                            </div>
                            <div class="p-4">
                                <ul class="list-unstyled mb-0 d-flex flex-column gap-3">
                                    <li class="d-flex align-items-center justify-content-between p-2 rounded bg-black bg-opacity-25 border border-secondary border-opacity-25">
                                        <span class="text-secondary small">
                                            <i class="bi bi-person-badge text-warning me-2"></i>Current Role
                                        </span>
                                        <span class="badge badge-role-admin">
                                            <c:out value="${sessionScope.userRole}"/>
                                        </span>
                                    </li>
                                    <li class="d-flex align-items-center justify-content-between p-2 rounded bg-black bg-opacity-25 border border-secondary border-opacity-25">
                                        <span class="text-secondary small">
                                            <i class="bi bi-key-fill text-info me-2"></i>Password Hash
                                        </span>
                                        <span class="badge bg-dark text-info border border-info border-opacity-25 font-monospace">
                                            BCrypt (Cost 12)
                                        </span>
                                    </li>
                                    <li class="d-flex align-items-center justify-content-between p-2 rounded bg-black bg-opacity-25 border border-secondary border-opacity-25">
                                        <span class="text-secondary small">
                                            <i class="bi bi-shield-lock text-success me-2"></i>CSRF Protection
                                        </span>
                                        <span class="badge bg-success bg-opacity-10 text-success border border-success border-opacity-25">
                                            Active
                                        </span>
                                    </li>
                                    <li class="d-flex align-items-center justify-content-between p-2 rounded bg-black bg-opacity-25 border border-secondary border-opacity-25">
                                        <span class="text-secondary small">
                                            <i class="bi bi-database text-primary me-2"></i>Database Engine
                                        </span>
                                        <span class="badge bg-dark text-light border border-secondary font-monospace">
                                            MySQL 8.x
                                        </span>
                                    </li>
                                    <li class="d-flex align-items-center justify-content-between p-2 rounded bg-black bg-opacity-25 border border-secondary border-opacity-25">
                                        <span class="text-secondary small">
                                            <i class="bi bi-server text-light me-2"></i>Servlet Container
                                        </span>
                                        <span class="badge bg-dark text-light border border-secondary">
                                            Tomcat 10.1 (Jakarta EE 10)
                                        </span>
                                    </li>
                                </ul>

                                <div class="mt-4 p-3 rounded bg-dark border border-secondary small text-secondary">
                                    <div class="fw-semibold text-white mb-1">
                                        <i class="bi bi-info-circle me-1 text-warning"></i> Session Boundary Protection
                                    </div>
                                    AuthenticationFilter ensures server-side role validation for all <code>/admin/*</code> routes. Direct JSP access is disabled via <code>WEB-INF/</code> containment.
                                </div>
                            </div>
                        </section>
                    </div>

                </div>

            </main>
        </div>

    </div>

    <!-- Bootstrap 5 JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-YvpcrYf0tY3lHB60NNkmXc4s9bIOgUxi8T/jzmLe3zxj8NxO8yVMnN5Z2X5ZZzEB"
            crossorigin="anonymous"></script>

    <!-- Admin Interactive Script (Sidebar mobile drawer toggle) -->
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            var sidebarToggle = document.getElementById('sidebarToggle');
            var adminSidebar  = document.getElementById('adminSidebar');
            var backdrop      = document.getElementById('sidebarBackdrop');

            if (sidebarToggle && adminSidebar && backdrop) {
                sidebarToggle.addEventListener('click', function () {
                    adminSidebar.classList.toggle('show');
                    backdrop.classList.toggle('show');
                });

                backdrop.addEventListener('click', function () {
                    adminSidebar.classList.remove('show');
                    backdrop.classList.remove('show');
                });
            }
        });
    </script>
</body>
</html>
