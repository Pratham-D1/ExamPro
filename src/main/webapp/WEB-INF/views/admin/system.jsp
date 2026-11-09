<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="ExamPro Enterprise System Overview & Diagnostics">
    <title>System Overview &amp; Diagnostics | ExamPro Admin</title>

    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">

    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">

    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap" rel="stylesheet">

    <!-- Custom Styles -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
    <style>
        .audit-mono { font-family: 'JetBrains Mono', monospace; font-size: 0.85rem; }
    </style>
</head>
<body class="ep-admin-body">

    <!-- Mobile Sidebar Backdrop -->
    <div class="ep-sidebar-backdrop" id="sidebarBackdrop" aria-hidden="true"></div>

    <div class="ep-admin-wrapper">

        <!-- Admin Sidebar Navigation -->
        <jsp:include page="sidebar.jsp">
            <jsp:param name="active" value="system"/>
        </jsp:include>

        <!-- Main Content Area -->
        <main class="ep-admin-main">

            <!-- Topbar -->
            <header class="ep-admin-topbar">
                <div class="d-flex align-items-center gap-3">
                    <button class="btn btn-outline-secondary btn-sm d-lg-none" id="sidebarToggle" type="button" aria-label="Toggle Navigation">
                        <i class="bi bi-list fs-5"></i>
                    </button>
                    <nav aria-label="breadcrumb">
                        <ol class="breadcrumb m-0 ep-topbar-breadcrumb">
                            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/admin/dashboard">Admin Portal</a></li>
                            <li class="breadcrumb-item active" aria-current="page">System Overview</li>
                        </ol>
                    </nav>
                </div>
                <div class="ep-topbar-actions d-flex align-items-center gap-2">
                    <jsp:include page="/WEB-INF/views/common/notif_bell.jsp"/>
                    <div class="d-flex align-items-center gap-2 ps-2 border-start border-secondary">
                        <span class="badge bg-danger">ADMIN</span>
                        <span class="small text-white fw-semibold d-none d-sm-inline"><c:out value="${currentUser.fullName}"/></span>
                    </div>
                </div>
            </header>

            <!-- Console Body -->
            <div class="p-3 p-md-4">

                <!-- Header Banner -->
                <div class="d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-3 mb-4">
                    <div>
                        <h2 class="text-white fw-bold mb-1">
                            <i class="bi bi-cpu-fill text-warning me-2"></i>System Overview &amp; Diagnostics
                        </h2>
                        <p class="text-muted small mb-0">
                            Enterprise telemetry, database metrics, JVM health, and real-time execution statistics.
                        </p>
                    </div>
                    <div class="d-flex gap-2">
                        <a href="${pageContext.request.contextPath}/admin/audit-logs" class="btn btn-outline-info btn-sm">
                            <i class="bi bi-shield-check me-1"></i> Audit Trail
                        </a>
                        <button type="button" class="btn btn-outline-secondary btn-sm" onclick="window.location.reload();">
                            <i class="bi bi-arrow-clockwise me-1"></i> Refresh Telemetry
                        </button>
                    </div>
                </div>

                <!-- 8 Genuine Database Metric Cards -->
                <div class="row g-3 mb-4">
                    <div class="col-6 col-md-3">
                        <div class="card bg-dark border-secondary h-100">
                            <div class="card-body p-3">
                                <span class="text-muted small text-uppercase fw-semibold d-block mb-1">Students</span>
                                <h3 class="text-white fw-bold mb-0 text-info"><c:out value="${stats.totalStudents}"/></h3>
                            </div>
                        </div>
                    </div>
                    <div class="col-6 col-md-3">
                        <div class="card bg-dark border-secondary h-100">
                            <div class="card-body p-3">
                                <span class="text-muted small text-uppercase fw-semibold d-block mb-1">Faculty</span>
                                <h3 class="text-white fw-bold mb-0 text-primary"><c:out value="${stats.totalTeachers}"/></h3>
                            </div>
                        </div>
                    </div>
                    <div class="col-6 col-md-3">
                        <div class="card bg-dark border-secondary h-100">
                            <div class="card-body p-3">
                                <span class="text-muted small text-uppercase fw-semibold d-block mb-1">Administrators</span>
                                <h3 class="text-white fw-bold mb-0 text-danger"><c:out value="${stats.totalAdmins}"/></h3>
                            </div>
                        </div>
                    </div>
                    <div class="col-6 col-md-3">
                        <div class="card bg-dark border-secondary h-100">
                            <div class="card-body p-3">
                                <span class="text-muted small text-uppercase fw-semibold d-block mb-1">Examinations</span>
                                <h3 class="text-white fw-bold mb-0 text-warning"><c:out value="${stats.totalExaminations}"/></h3>
                            </div>
                        </div>
                    </div>
                    <div class="col-6 col-md-3">
                        <div class="card bg-dark border-secondary h-100">
                            <div class="card-body p-3">
                                <span class="text-muted small text-uppercase fw-semibold d-block mb-1">Questions</span>
                                <h3 class="text-white fw-bold mb-0 text-light"><c:out value="${stats.totalQuestions}"/></h3>
                            </div>
                        </div>
                    </div>
                    <div class="col-6 col-md-3">
                        <div class="card bg-dark border-secondary h-100">
                            <div class="card-body p-3">
                                <span class="text-muted small text-uppercase fw-semibold d-block mb-1">Exam Attempts</span>
                                <h3 class="text-white fw-bold mb-0 text-success"><c:out value="${stats.totalAttempts}"/></h3>
                            </div>
                        </div>
                    </div>
                    <div class="col-6 col-md-3">
                        <div class="card bg-dark border-secondary h-100">
                            <div class="card-body p-3">
                                <span class="text-muted small text-uppercase fw-semibold d-block mb-1">Published Results</span>
                                <h3 class="text-white fw-bold mb-0 text-info"><c:out value="${stats.totalPublishedResults}"/></h3>
                            </div>
                        </div>
                    </div>
                    <div class="col-6 col-md-3">
                        <div class="card bg-dark border-secondary h-100">
                            <div class="card-body p-3">
                                <span class="text-muted small text-uppercase fw-semibold d-block mb-1">Audit Events</span>
                                <h3 class="text-white fw-bold mb-0 text-warning"><c:out value="${stats.totalAuditLogs}"/></h3>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Two-Column Diagnostics Section -->
                <div class="row g-4 mb-4">

                    <!-- Left: Server & Database Environment -->
                    <div class="col-lg-6">
                        <div class="card bg-dark border-secondary h-100">
                            <div class="card-header bg-dark border-secondary py-3">
                                <h5 class="text-white mb-0 fs-6 fw-semibold">
                                    <i class="bi bi-hdd-network text-info me-2"></i>Runtime &amp; Container Environment
                                </h5>
                            </div>
                            <div class="card-body p-3">
                                <ul class="list-group list-group-flush bg-dark text-white">
                                    <li class="list-group-item bg-dark border-secondary d-flex justify-content-between align-items-center py-2 px-0">
                                        <span class="text-muted small">Java Runtime</span>
                                        <span class="audit-mono text-light fw-medium"><c:out value="${stats.javaVersion}"/> (<c:out value="${stats.javaVendor}"/>)</span>
                                    </li>
                                    <li class="list-group-item bg-dark border-secondary d-flex justify-content-between align-items-center py-2 px-0">
                                        <span class="text-muted small">JVM Implementation</span>
                                        <span class="audit-mono text-light fw-medium"><c:out value="${stats.jvmName}"/></span>
                                    </li>
                                    <li class="list-group-item bg-dark border-secondary d-flex justify-content-between align-items-center py-2 px-0">
                                        <span class="text-muted small">Operating System</span>
                                        <span class="audit-mono text-light fw-medium"><c:out value="${stats.osName}"/> <c:out value="${stats.osVersion}"/> (<c:out value="${stats.osArch}"/>)</span>
                                    </li>
                                    <li class="list-group-item bg-dark border-secondary d-flex justify-content-between align-items-center py-2 px-0">
                                        <span class="text-muted small">Available Processors</span>
                                        <span class="badge bg-secondary"><c:out value="${stats.availableProcessors}"/> CPU Cores</span>
                                    </li>
                                    <li class="list-group-item bg-dark border-secondary d-flex justify-content-between align-items-center py-2 px-0">
                                        <span class="text-muted small">Servlet Container</span>
                                        <span class="audit-mono text-light fw-medium"><c:out value="${stats.serverInfo}"/></span>
                                    </li>
                                    <li class="list-group-item bg-dark border-secondary d-flex justify-content-between align-items-center py-2 px-0">
                                        <span class="text-muted small">Application Context</span>
                                        <span class="audit-mono text-light fw-medium"><c:out value="${stats.contextPath}"/></span>
                                    </li>
                                    <li class="list-group-item bg-dark border-secondary d-flex justify-content-between align-items-center py-2 px-0">
                                        <span class="text-muted small">Database Health</span>
                                        <span class="badge bg-success"><i class="bi bi-database-check me-1"></i><c:out value="${stats.dbStatus}"/></span>
                                    </li>
                                    <li class="list-group-item bg-dark border-secondary py-2 px-0">
                                        <span class="text-muted small d-block">Database Server</span>
                                        <div class="audit-mono text-light small text-break"><c:out value="${stats.dbVersion}"/></div>
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </div>

                    <!-- Right: JVM Memory Telemetry -->
                    <div class="col-lg-6">
                        <div class="card bg-dark border-secondary h-100">
                            <div class="card-header bg-dark border-secondary py-3">
                                <h5 class="text-white mb-0 fs-6 fw-semibold">
                                    <i class="bi bi-memory text-warning me-2"></i>JVM Memory Telemetry
                                </h5>
                            </div>
                            <div class="card-body p-4 d-flex flex-column justify-content-between">
                                <div>
                                    <div class="d-flex justify-content-between align-items-center mb-2">
                                        <span class="text-muted small">Allocated Heap Usage</span>
                                        <span class="fw-bold text-white audit-mono"><c:out value="${stats.memoryUsagePercent}"/>%</span>
                                    </div>
                                    <div class="progress mb-4" style="height: 12px; background-color: rgba(255,255,255,0.1);">
                                        <div class="progress-bar ${stats.memoryUsagePercent > 80 ? 'bg-danger' : (stats.memoryUsagePercent > 60 ? 'bg-warning' : 'bg-success')}"
                                             role="progressbar" style="width: ${stats.memoryUsagePercent}%;"
                                             aria-valuenow="${stats.memoryUsagePercent}" aria-valuemin="0" aria-valuemax="100"></div>
                                    </div>

                                    <div class="row g-3">
                                        <div class="col-6">
                                            <div class="p-3 rounded bg-black bg-opacity-25 border border-secondary">
                                                <span class="text-muted small d-block">Used Memory</span>
                                                <div class="fs-5 fw-bold text-white audit-mono"><c:out value="${stats.usedMemoryMb}"/> MB</div>
                                            </div>
                                        </div>
                                        <div class="col-6">
                                            <div class="p-3 rounded bg-black bg-opacity-25 border border-secondary">
                                                <span class="text-muted small d-block">Free Allocated</span>
                                                <div class="fs-5 fw-bold text-success audit-mono"><c:out value="${stats.freeMemoryMb}"/> MB</div>
                                            </div>
                                        </div>
                                        <div class="col-6">
                                            <div class="p-3 rounded bg-black bg-opacity-25 border border-secondary">
                                                <span class="text-muted small d-block">Total Allocated Heap</span>
                                                <div class="fs-5 fw-bold text-info audit-mono"><c:out value="${stats.totalMemoryMb}"/> MB</div>
                                            </div>
                                        </div>
                                        <div class="col-6">
                                            <div class="p-3 rounded bg-black bg-opacity-25 border border-secondary">
                                                <span class="text-muted small d-block">Max Configured Heap</span>
                                                <div class="fs-5 fw-bold text-warning audit-mono"><c:out value="${stats.maxMemoryMb}"/> MB</div>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div class="alert alert-secondary bg-dark border-secondary text-muted small mt-4 mb-0">
                                    <i class="bi bi-info-circle me-1 text-info"></i>
                                    Telemetry is queried live via <code>java.lang.Runtime</code> and MySQL JDBC connection metadata.
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Recent System Activity Table -->
                <div class="card bg-dark border-secondary shadow-sm mb-4">
                    <div class="card-header bg-dark border-secondary d-flex justify-content-between align-items-center py-3">
                        <h5 class="text-white mb-0 fs-6 fw-semibold">
                            <i class="bi bi-clock-history text-info me-2"></i>Recent System Activity Stream
                        </h5>
                        <a href="${pageContext.request.contextPath}/admin/audit-logs" class="btn btn-outline-info btn-sm">
                            View All Logs &raquo;
                        </a>
                    </div>
                    <div class="table-responsive">
                        <table class="table table-dark table-hover align-middle mb-0">
                            <thead>
                                <tr class="text-muted small text-uppercase border-secondary">
                                    <th style="width: 170px;">Timestamp</th>
                                    <th style="width: 220px;">User &amp; Role</th>
                                    <th style="width: 150px;">Module / Action</th>
                                    <th>Description</th>
                                    <th style="width: 90px;">Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty stats.recentActivity}">
                                        <c:forEach var="act" items="${stats.recentActivity}">
                                            <tr class="border-secondary">
                                                <td class="small audit-mono text-light">
                                                    <i class="bi bi-clock me-1 text-muted"></i><c:out value="${act.formattedCreatedAt}"/>
                                                </td>
                                                <td>
                                                    <div class="fw-semibold text-white small">
                                                        <c:out value="${act.userFullName != null ? act.userFullName : 'Anonymous / System'}"/>
                                                    </div>
                                                    <span class="badge bg-secondary bg-opacity-25 text-white" style="font-size: 0.68rem;">
                                                        <c:out value="${act.userRole}"/>
                                                    </span>
                                                </td>
                                                <td>
                                                    <span class="badge ${act.moduleBadgeClass} mb-1"><c:out value="${act.module}"/></span>
                                                    <div>
                                                        <span class="badge ${act.actionBadgeClass}" style="font-size: 0.72rem;">
                                                            <c:out value="${act.action}"/>
                                                        </span>
                                                    </div>
                                                </td>
                                                <td class="small text-light">
                                                    <c:out value="${act.description}"/>
                                                </td>
                                                <td>
                                                    <span class="badge ${act.statusBadgeClass}">
                                                        <c:out value="${act.status}"/>
                                                    </span>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="5" class="text-center py-4 text-muted small">
                                                No recent system activities recorded.
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
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz" crossorigin="anonymous"></script>

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

