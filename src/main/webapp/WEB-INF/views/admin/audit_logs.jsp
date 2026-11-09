<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="ExamPro Security & System Audit Trail Console">
    <title>Security &amp; Audit Logs | ExamPro Admin</title>

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
        .bg-indigo { background-color: #6610f2 !important; }
        .bg-orange { background-color: #fd7e14 !important; }
    </style>
</head>
<body class="ep-admin-body">

    <!-- Mobile Sidebar Backdrop -->
    <div class="ep-sidebar-backdrop" id="sidebarBackdrop" aria-hidden="true"></div>

    <div class="ep-admin-wrapper">

        <!-- Admin Sidebar Navigation -->
        <jsp:include page="sidebar.jsp">
            <jsp:param name="active" value="audit"/>
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
                            <li class="breadcrumb-item active" aria-current="page">Audit Logs</li>
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

            <!-- Audit Logs Console Body -->
            <div class="p-3 p-md-4">

                <!-- Header Banner -->
                <div class="d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-3 mb-4">
                    <div>
                        <h2 class="text-white fw-bold mb-1">
                            <i class="bi bi-shield-check text-info me-2"></i>Security &amp; Audit Trail
                        </h2>
                        <p class="text-muted small mb-0">
                            Centralized immutable audit log capturing user authentication, operational actions, and administrative activity.
                        </p>
                    </div>
                    <div class="d-flex gap-2">
                        <a href="${pageContext.request.contextPath}/admin/system" class="btn btn-outline-secondary btn-sm">
                            <i class="bi bi-cpu me-1"></i> System Overview
                        </a>
                        <button type="button" class="btn btn-outline-info btn-sm" onclick="window.location.reload();">
                            <i class="bi bi-arrow-clockwise me-1"></i> Refresh
                        </button>
                    </div>
                </div>

                <!-- 4 KPI Overview Cards -->
                <div class="row g-3 mb-4">
                    <div class="col-sm-6 col-xl-3">
                        <div class="card bg-dark border-secondary h-100">
                            <div class="card-body d-flex align-items-center gap-3">
                                <div class="p-3 rounded-3 bg-info bg-opacity-10 text-info fs-3">
                                    <i class="bi bi-list-check"></i>
                                </div>
                                <div>
                                    <span class="text-muted small text-uppercase fw-semibold">Total Activities</span>
                                    <h3 class="text-white fw-bold mb-0 mt-1"><c:out value="${stats.totalActivities}"/></h3>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-6 col-xl-3">
                        <div class="card bg-dark border-secondary h-100">
                            <div class="card-body d-flex align-items-center gap-3">
                                <div class="p-3 rounded-3 bg-success bg-opacity-10 text-success fs-3">
                                    <i class="bi bi-shield-check"></i>
                                </div>
                                <div>
                                    <span class="text-muted small text-uppercase fw-semibold">Successful Logins</span>
                                    <h3 class="text-white fw-bold mb-0 mt-1"><c:out value="${stats.successfulLogins}"/></h3>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-6 col-xl-3">
                        <div class="card bg-dark border-secondary h-100">
                            <div class="card-body d-flex align-items-center gap-3">
                                <div class="p-3 rounded-3 bg-danger bg-opacity-10 text-danger fs-3">
                                    <i class="bi bi-shield-x"></i>
                                </div>
                                <div>
                                    <span class="text-muted small text-uppercase fw-semibold">Failed Login Attempts</span>
                                    <h3 class="text-white fw-bold mb-0 mt-1"><c:out value="${stats.failedLogins}"/></h3>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-6 col-xl-3">
                        <div class="card bg-dark border-secondary h-100">
                            <div class="card-body d-flex align-items-center gap-3">
                                <div class="p-3 rounded-3 bg-warning bg-opacity-10 text-warning fs-3">
                                    <i class="bi bi-gear-fill"></i>
                                </div>
                                <div>
                                    <span class="text-muted small text-uppercase fw-semibold">Administrative Actions</span>
                                    <h3 class="text-white fw-bold mb-0 mt-1"><c:out value="${stats.adminActions}"/></h3>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Multi-criteria Filter Toolbar -->
                <div class="card bg-dark border-secondary mb-4">
                    <div class="card-body p-3">
                        <form action="${pageContext.request.contextPath}/admin/audit-logs" method="get" class="row g-2 align-items-end">
                            <div class="col-12 col-md-3">
                                <label class="form-label text-muted small mb-1">Search Keywords</label>
                                <div class="input-group input-group-sm">
                                    <span class="input-group-text bg-secondary border-secondary text-white"><i class="bi bi-search"></i></span>
                                    <input type="text" name="search" class="form-control bg-dark border-secondary text-white"
                                           placeholder="Description, IP, User..." value="<c:out value='${search}'/>">
                                </div>
                            </div>

                            <div class="col-6 col-md-2">
                                <label class="form-label text-muted small mb-1">Role</label>
                                <select name="role" class="form-select form-select-sm bg-dark border-secondary text-white">
                                    <option value="ALL" ${selectedRole == 'ALL' ? 'selected' : ''}>All Roles</option>
                                    <option value="ADMIN" ${selectedRole == 'ADMIN' ? 'selected' : ''}>ADMIN</option>
                                    <option value="TEACHER" ${selectedRole == 'TEACHER' ? 'selected' : ''}>TEACHER</option>
                                    <option value="STUDENT" ${selectedRole == 'STUDENT' ? 'selected' : ''}>STUDENT</option>
                                    <option value="ANONYMOUS" ${selectedRole == 'ANONYMOUS' ? 'selected' : ''}>ANONYMOUS</option>
                                </select>
                            </div>

                            <div class="col-6 col-md-2">
                                <label class="form-label text-muted small mb-1">Module</label>
                                <select name="module" class="form-select form-select-sm bg-dark border-secondary text-white">
                                    <option value="ALL" ${selectedModule == 'ALL' ? 'selected' : ''}>All Modules</option>
                                    <option value="AUTH" ${selectedModule == 'AUTH' ? 'selected' : ''}>AUTH</option>
                                    <option value="USER" ${selectedModule == 'USER' ? 'selected' : ''}>USER</option>
                                    <option value="SUBJECT" ${selectedModule == 'SUBJECT' ? 'selected' : ''}>SUBJECT</option>
                                    <option value="EXAM" ${selectedModule == 'EXAM' ? 'selected' : ''}>EXAM</option>
                                    <option value="QUESTION" ${selectedModule == 'QUESTION' ? 'selected' : ''}>QUESTION</option>
                                    <option value="RESULT" ${selectedModule == 'RESULT' ? 'selected' : ''}>RESULT</option>
                                    <option value="ANNOUNCEMENT" ${selectedModule == 'ANNOUNCEMENT' ? 'selected' : ''}>ANNOUNCEMENT</option>
                                    <option value="SYSTEM" ${selectedModule == 'SYSTEM' ? 'selected' : ''}>SYSTEM</option>
                                </select>
                            </div>

                            <div class="col-6 col-md-2">
                                <label class="form-label text-muted small mb-1">Status</label>
                                <select name="status" class="form-select form-select-sm bg-dark border-secondary text-white">
                                    <option value="ALL" ${selectedStatus == 'ALL' ? 'selected' : ''}>All Statuses</option>
                                    <option value="SUCCESS" ${selectedStatus == 'SUCCESS' ? 'selected' : ''}>SUCCESS</option>
                                    <option value="FAILURE" ${selectedStatus == 'FAILURE' ? 'selected' : ''}>FAILURE</option>
                                </select>
                            </div>

                            <div class="col-6 col-md-3 d-flex gap-2">
                                <button type="submit" class="btn btn-primary btn-sm flex-grow-1">
                                    <i class="bi bi-funnel me-1"></i> Filter
                                </button>
                                <a href="${pageContext.request.contextPath}/admin/audit-logs" class="btn btn-outline-secondary btn-sm" title="Reset Filters">
                                    <i class="bi bi-x-circle"></i>
                                </a>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- Audit Log Table Card -->
                <div class="card bg-dark border-secondary shadow-sm mb-4">
                    <div class="card-header bg-dark border-secondary d-flex justify-content-between align-items-center py-3">
                        <h5 class="text-white mb-0 fs-6 fw-semibold">
                            <i class="bi bi-activity text-info me-2"></i>Activity Ledger
                            <span class="badge bg-secondary ms-2"><c:out value="${totalCount}"/> Records</span>
                        </h5>
                        <span class="text-muted small">Displaying Page ${currentPage} of ${totalPages}</span>
                    </div>

                    <div class="table-responsive">
                        <table class="table table-dark table-hover align-middle mb-0">
                            <thead>
                                <tr class="text-muted small text-uppercase border-secondary">
                                    <th style="width: 70px;">ID</th>
                                    <th style="width: 170px;">Timestamp</th>
                                    <th style="width: 220px;">User &amp; Role</th>
                                    <th style="width: 160px;">Module / Action</th>
                                    <th>Description</th>
                                    <th style="width: 130px;">IP Address</th>
                                    <th style="width: 90px;">Status</th>
                                    <th style="width: 80px;" class="text-center">Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty logs}">
                                        <c:forEach var="l" items="${logs}">
                                            <tr class="border-secondary">
                                                <td class="text-muted small">#<c:out value="${l.id}"/></td>
                                                <td class="small text-nowrap audit-mono text-light">
                                                    <i class="bi bi-clock me-1 text-muted"></i><c:out value="${l.formattedCreatedAt}"/>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${not empty l.userEmail}">
                                                            <div class="fw-semibold text-white small"><c:out value="${l.userFullName}"/></div>
                                                            <div class="text-muted small text-truncate" style="max-width: 190px;"><c:out value="${l.userEmail}"/></div>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="text-muted small fst-italic">Anonymous / System</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                    <span class="badge bg-secondary bg-opacity-25 text-white" style="font-size: 0.68rem;">
                                                        <c:out value="${l.userRole}"/>
                                                    </span>
                                                </td>
                                                <td>
                                                    <span class="badge ${l.moduleBadgeClass} mb-1"><c:out value="${l.module}"/></span>
                                                    <div>
                                                        <span class="badge ${l.actionBadgeClass}" style="font-size: 0.72rem;">
                                                            <c:out value="${l.action}"/>
                                                        </span>
                                                    </div>
                                                </td>
                                                <td class="small text-light">
                                                    <div class="text-break"><c:out value="${l.description}"/></div>
                                                </td>
                                                <td class="small audit-mono text-muted text-nowrap">
                                                    <i class="bi bi-geo-alt me-1"></i><c:out value="${l.ipAddress}"/>
                                                </td>
                                                <td>
                                                    <span class="badge ${l.statusBadgeClass}">
                                                        <c:out value="${l.status}"/>
                                                    </span>
                                                </td>
                                                <td class="text-center">
                                                    <button type="button" class="btn btn-outline-info btn-sm p-1 px-2"
                                                            onclick="viewLogDetail('${l.id}', '${l.formattedCreatedAt}', '<c:out value="${l.userFullName != null ? l.userFullName : 'Anonymous'}"/>', '<c:out value="${l.userEmail != null ? l.userEmail : 'N/A'}"/>', '${l.userRole}', '${l.module}', '${l.action}', '${l.status}', '<c:out value="${l.ipAddress}"/>', '<c:out value="${l.description}"/>')"
                                                            title="Inspect Log Entry">
                                                        <i class="bi bi-eye"></i>
                                                    </button>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="8" class="text-center py-5">
                                                <i class="bi bi-shield-slash fs-1 text-muted d-block mb-2"></i>
                                                <h6 class="text-white">No Audit Records Found</h6>
                                                <p class="text-muted small mb-0">No log entries matched your filter and search criteria.</p>
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>

                    <!-- Pagination -->
                    <c:if test="${totalPages > 1}">
                        <div class="card-footer bg-dark border-secondary py-3 d-flex justify-content-between align-items-center">
                            <span class="text-muted small">Total: <strong><c:out value="${totalCount}"/></strong> events</span>
                            <nav aria-label="Audit Log Pagination">
                                <ul class="pagination pagination-sm m-0">
                                    <li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
                                        <a class="page-link bg-dark border-secondary text-white"
                                           href="${pageContext.request.contextPath}/admin/audit-logs?page=${currentPage - 1}&search=${search}&role=${selectedRole}&module=${selectedModule}&action=${selectedAction}&status=${selectedStatus}&startDate=${startDate}&endDate=${endDate}">
                                            &laquo;
                                        </a>
                                    </li>
                                    <c:forEach begin="1" end="${totalPages}" var="p">
                                        <c:if test="${p >= currentPage - 2 && p <= currentPage + 2}">
                                            <li class="page-item ${currentPage == p ? 'active' : ''}">
                                                <a class="page-link ${currentPage == p ? 'bg-primary border-primary' : 'bg-dark border-secondary text-white'}"
                                                   href="${pageContext.request.contextPath}/admin/audit-logs?page=${p}&search=${search}&role=${selectedRole}&module=${selectedModule}&action=${selectedAction}&status=${selectedStatus}&startDate=${startDate}&endDate=${endDate}">
                                                    ${p}
                                                </a>
                                            </li>
                                        </c:if>
                                    </c:forEach>
                                    <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                                        <a class="page-link bg-dark border-secondary text-white"
                                           href="${pageContext.request.contextPath}/admin/audit-logs?page=${currentPage + 1}&search=${search}&role=${selectedRole}&module=${selectedModule}&action=${selectedAction}&status=${selectedStatus}&startDate=${startDate}&endDate=${endDate}">
                                            &raquo;
                                        </a>
                                    </li>
                                </ul>
                            </nav>
                        </div>
                    </c:if>
                </div>

            </div>
        </main>
    </div>

    <!-- Log Detail Modal -->
    <div class="modal fade" id="logDetailModal" tabindex="-1" aria-labelledby="logDetailTitle" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content bg-dark border-secondary text-white">
                <div class="modal-header border-secondary">
                    <h5 class="modal-title fs-6 fw-bold" id="logDetailTitle">
                        <i class="bi bi-file-earmark-text text-info me-2"></i>Audit Log Record #<span id="modalLogId"></span>
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body p-4">
                    <div class="mb-3">
                        <span class="text-muted small d-block">Timestamp</span>
                        <div class="fw-semibold audit-mono" id="modalTimestamp"></div>
                    </div>
                    <div class="row g-2 mb-3">
                        <div class="col-6">
                            <span class="text-muted small d-block">Module</span>
                            <span class="badge bg-primary" id="modalModule"></span>
                        </div>
                        <div class="col-6">
                            <span class="text-muted small d-block">Action</span>
                            <span class="badge border border-info text-info" id="modalAction"></span>
                        </div>
                    </div>
                    <div class="row g-2 mb-3">
                        <div class="col-6">
                            <span class="text-muted small d-block">Status</span>
                            <span class="badge bg-success" id="modalStatus"></span>
                        </div>
                        <div class="col-6">
                            <span class="text-muted small d-block">Client IP Address</span>
                            <span class="audit-mono text-light" id="modalIp"></span>
                        </div>
                    </div>
                    <div class="mb-3">
                        <span class="text-muted small d-block">User &amp; Role</span>
                        <div class="fw-semibold" id="modalUserName"></div>
                        <div class="text-muted small" id="modalUserEmail"></div>
                    </div>
                    <div class="mb-0">
                        <span class="text-muted small d-block mb-1">Event Description</span>
                        <div class="p-3 rounded bg-black bg-opacity-50 border border-secondary text-light small text-break audit-mono"
                             id="modalDescription"></div>
                    </div>
                </div>
                <div class="modal-footer border-secondary">
                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Close</button>
                </div>
            </div>
        </div>
    </div>

    <!-- Bootstrap 5 JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz" crossorigin="anonymous"></script>

    <script>
        // Mobile Sidebar Toggle
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

        // View Log Detail Modal
        function viewLogDetail(id, timestamp, name, email, role, module, action, status, ip, desc) {
            document.getElementById('modalLogId').innerText = id;
            document.getElementById('modalTimestamp').innerText = timestamp;
            document.getElementById('modalModule').innerText = module;
            document.getElementById('modalAction').innerText = action;
            document.getElementById('modalStatus').innerText = status;
            document.getElementById('modalStatus').className = (status === 'SUCCESS') ? 'badge bg-success' : 'badge bg-danger';
            document.getElementById('modalIp').innerText = ip;
            document.getElementById('modalUserName').innerText = name + ' (' + role + ')';
            document.getElementById('modalUserEmail').innerText = email;
            document.getElementById('modalDescription').innerText = desc;

            const modal = new bootstrap.Modal(document.getElementById('logDetailModal'));
            modal.show();
        }
    </script>
</body>
</html>

