<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="ExamPro Enterprise User Directory and Security Management">
    <title>User Directory | ExamPro Admin</title>

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
            <jsp:param name="active" value="users"/>
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
                            <li class="breadcrumb-item active" aria-current="page">User Directory</li>
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

                <!-- Flash Messages -->
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

                <!-- Header Banner -->
                <div class="d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-3 mb-4">
                    <div>
                        <h2 class="text-white fw-bold mb-1">
                            <i class="bi bi-person-badge-fill text-primary me-2"></i>User Directory &amp; Access Controls
                        </h2>
                        <p class="text-muted small mb-0">
                            Enterprise directory across Administrators, Faculty, and Candidates. Manage account lifecycles and security states.
                        </p>
                    </div>
                    <div class="d-flex gap-2">
                        <a href="${pageContext.request.contextPath}/admin/students" class="btn btn-outline-info btn-sm">
                            <i class="bi bi-people me-1"></i> Students Module
                        </a>
                        <a href="${pageContext.request.contextPath}/admin/teachers" class="btn btn-outline-secondary btn-sm">
                            <i class="bi bi-person-video3 me-1"></i> Teachers Module
                        </a>
                    </div>
                </div>

                <!-- 3 Role Summary Cards -->
                <div class="row g-3 mb-4">
                    <div class="col-sm-4">
                        <div class="card bg-dark border-secondary h-100">
                            <div class="card-body d-flex align-items-center gap-3">
                                <div class="p-3 rounded-3 bg-danger bg-opacity-10 text-danger fs-3">
                                    <i class="bi bi-shield-lock-fill"></i>
                                </div>
                                <div>
                                    <span class="text-muted small text-uppercase fw-semibold">Administrators</span>
                                    <h3 class="text-white fw-bold mb-0 mt-1"><c:out value="${adminCount}"/></h3>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-4">
                        <div class="card bg-dark border-secondary h-100">
                            <div class="card-body d-flex align-items-center gap-3">
                                <div class="p-3 rounded-3 bg-purple bg-opacity-10 text-primary fs-3">
                                    <i class="bi bi-person-video3"></i>
                                </div>
                                <div>
                                    <span class="text-muted small text-uppercase fw-semibold">Faculty / Teachers</span>
                                    <h3 class="text-white fw-bold mb-0 mt-1"><c:out value="${teacherCount}"/></h3>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-4">
                        <div class="card bg-dark border-secondary h-100">
                            <div class="card-body d-flex align-items-center gap-3">
                                <div class="p-3 rounded-3 bg-info bg-opacity-10 text-info fs-3">
                                    <i class="bi bi-people-fill"></i>
                                </div>
                                <div>
                                    <span class="text-muted small text-uppercase fw-semibold">Students / Candidates</span>
                                    <h3 class="text-white fw-bold mb-0 mt-1"><c:out value="${studentCount}"/></h3>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Filter Toolbar -->
                <div class="card bg-dark border-secondary mb-4">
                    <div class="card-body p-3">
                        <form action="${pageContext.request.contextPath}/admin/users" method="get" class="row g-2 align-items-end">
                            <div class="col-12 col-md-4">
                                <label class="form-label text-muted small mb-1">Search User</label>
                                <div class="input-group input-group-sm">
                                    <span class="input-group-text bg-secondary border-secondary text-white"><i class="bi bi-search"></i></span>
                                    <input type="text" name="search" class="form-control bg-dark border-secondary text-white"
                                           placeholder="Full Name, Email..." value="<c:out value='${search}'/>">
                                </div>
                            </div>

                            <div class="col-6 col-md-3">
                                <label class="form-label text-muted small mb-1">Role</label>
                                <select name="role" class="form-select form-select-sm bg-dark border-secondary text-white">
                                    <option value="ALL" ${selectedRole == 'ALL' ? 'selected' : ''}>All Roles</option>
                                    <option value="ADMIN" ${selectedRole == 'ADMIN' ? 'selected' : ''}>ADMIN</option>
                                    <option value="TEACHER" ${selectedRole == 'TEACHER' ? 'selected' : ''}>TEACHER</option>
                                    <option value="STUDENT" ${selectedRole == 'STUDENT' ? 'selected' : ''}>STUDENT</option>
                                </select>
                            </div>

                            <div class="col-6 col-md-3">
                                <label class="form-label text-muted small mb-1">Status</label>
                                <select name="status" class="form-select form-select-sm bg-dark border-secondary text-white">
                                    <option value="ALL" ${selectedStatus == 'ALL' ? 'selected' : ''}>All Statuses</option>
                                    <option value="ACTIVE" ${selectedStatus == 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                                    <option value="INACTIVE" ${selectedStatus == 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
                                </select>
                            </div>

                            <div class="col-12 col-md-2 d-flex gap-2">
                                <button type="submit" class="btn btn-primary btn-sm flex-grow-1">
                                    <i class="bi bi-funnel me-1"></i> Filter
                                </button>
                                <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-outline-secondary btn-sm" title="Reset Filters">
                                    <i class="bi bi-x-circle"></i>
                                </a>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- User Directory Table Card -->
                <div class="card bg-dark border-secondary shadow-sm mb-4">
                    <div class="card-header bg-dark border-secondary d-flex justify-content-between align-items-center py-3">
                        <h5 class="text-white mb-0 fs-6 fw-semibold">
                            <i class="bi bi-people text-primary me-2"></i>User Roster
                            <span class="badge bg-secondary ms-2"><c:out value="${totalCount}"/> Users</span>
                        </h5>
                        <span class="text-muted small">Page ${currentPage} of ${totalPages}</span>
                    </div>

                    <div class="table-responsive">
                        <table class="table table-dark table-hover align-middle mb-0">
                            <thead>
                                <tr class="text-muted small text-uppercase border-secondary">
                                    <th style="width: 70px;">ID</th>
                                    <th>User</th>
                                    <th style="width: 130px;">Role</th>
                                    <th style="width: 120px;">Status</th>
                                    <th style="width: 180px;">Security / Lockout</th>
                                    <th style="width: 170px;">Registration Date</th>
                                    <th style="width: 170px;">Last Login</th>
                                    <th style="width: 140px;" class="text-center">Lifecycle Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty users}">
                                        <c:forEach var="u" items="${users}">
                                            <tr class="border-secondary">
                                                <td class="text-muted small">#<c:out value="${u.id}"/></td>
                                                <td>
                                                    <div class="d-flex align-items-center gap-2">
                                                        <div class="rounded-circle d-flex align-items-center justify-content-center text-white fw-bold bg-secondary"
                                                             style="width: 34px; height: 34px; font-size: 0.85rem;">
                                                            <c:out value="${fn:substring(u.fullName, 0, 1)}"/>
                                                        </div>
                                                        <div>
                                                            <div class="fw-semibold text-white small"><c:out value="${u.fullName}"/></div>
                                                            <div class="text-muted small"><c:out value="${u.email}"/></div>
                                                        </div>
                                                    </div>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${u.role == 'ADMIN'}">
                                                            <span class="badge bg-danger"><i class="bi bi-shield-lock me-1"></i>ADMIN</span>
                                                        </c:when>
                                                        <c:when test="${u.role == 'TEACHER'}">
                                                            <span class="badge" style="background: rgba(163, 113, 247, 0.2); color: #a371f7;">
                                                                <i class="bi bi-person-video3 me-1"></i>TEACHER
                                                            </span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge bg-info text-dark"><i class="bi bi-person me-1"></i>STUDENT</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${u.active}">
                                                            <span class="badge bg-success bg-opacity-25 text-success border border-success">
                                                                <i class="bi bi-check-circle-fill me-1"></i>Active
                                                            </span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge bg-secondary text-light border border-secondary">
                                                                <i class="bi bi-x-circle-fill me-1"></i>Deactivated
                                                            </span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${u.lockedOut}">
                                                            <span class="badge bg-danger text-white mb-1">
                                                                <i class="bi bi-lock-fill me-1"></i>Locked (${u.lockoutRemainingMinutes}m)
                                                            </span>
                                                            <form action="${pageContext.request.contextPath}/admin/users" method="post" class="d-inline">
                                                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                                <input type="hidden" name="action" value="resetLockout">
                                                                <input type="hidden" name="userId" value="${u.id}">
                                                                <button type="submit" class="btn btn-outline-warning btn-sm p-0 px-2" style="font-size: 0.7rem;"
                                                                        title="Unlock Account">
                                                                    Unlock
                                                                </button>
                                                            </form>
                                                        </c:when>
                                                        <c:when test="${u.failedLoginAttempts > 0}">
                                                            <span class="badge bg-warning text-dark small">
                                                                ${u.failedLoginAttempts} failed attempts
                                                            </span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="text-muted small"><i class="bi bi-shield-check text-success me-1"></i>Clear</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="small audit-mono text-muted">
                                                    <c:out value="${u.formattedCreatedAt}"/>
                                                </td>
                                                <td class="small audit-mono text-light">
                                                    <c:out value="${u.formattedLastLoginAt}"/>
                                                </td>
                                                <td class="text-center">
                                                    <c:choose>
                                                        <c:when test="${u.id == currentUser.id}">
                                                            <span class="badge bg-secondary" title="You cannot modify your own administrative account">
                                                                Current User
                                                            </span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <form action="${pageContext.request.contextPath}/admin/users" method="post" class="m-0 d-inline">
                                                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                                <input type="hidden" name="action" value="toggleStatus">
                                                                <input type="hidden" name="userId" value="${u.id}">
                                                                <input type="hidden" name="active" value="${!u.active}">
                                                                <button type="submit" class="btn ${u.active ? 'btn-outline-danger' : 'btn-outline-success'} btn-sm py-1 px-2"
                                                                        onclick="return confirm('${u.active ? 'Are you sure you want to deactivate this account?' : 'Reactivate this account?'}');">
                                                                    <i class="bi ${u.active ? 'bi-person-dash' : 'bi-person-check'} me-1"></i>
                                                                    ${u.active ? 'Deactivate' : 'Activate'}
                                                                </button>
                                                            </form>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="8" class="text-center py-5">
                                                <i class="bi bi-people fs-1 text-muted d-block mb-2"></i>
                                                <h6 class="text-white">No Users Found</h6>
                                                <p class="text-muted small mb-0">No accounts matched your filter criteria.</p>
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
                            <span class="text-muted small">Total: <strong><c:out value="${totalCount}"/></strong> users</span>
                            <nav aria-label="User Directory Pagination">
                                <ul class="pagination pagination-sm m-0">
                                    <li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
                                        <a class="page-link bg-dark border-secondary text-white"
                                           href="${pageContext.request.contextPath}/admin/users?page=${currentPage - 1}&search=${search}&role=${selectedRole}&status=${selectedStatus}">
                                            &laquo;
                                        </a>
                                    </li>
                                    <c:forEach begin="1" end="${totalPages}" var="p">
                                        <c:if test="${p >= currentPage - 2 && p <= currentPage + 2}">
                                            <li class="page-item ${currentPage == p ? 'active' : ''}">
                                                <a class="page-link ${currentPage == p ? 'bg-primary border-primary' : 'bg-dark border-secondary text-white'}"
                                                   href="${pageContext.request.contextPath}/admin/users?page=${p}&search=${search}&role=${selectedRole}&status=${selectedStatus}">
                                                    ${p}
                                                </a>
                                            </li>
                                        </c:if>
                                    </c:forEach>
                                    <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                                        <a class="page-link bg-dark border-secondary text-white"
                                           href="${pageContext.request.contextPath}/admin/users?page=${currentPage + 1}&search=${search}&role=${selectedRole}&status=${selectedStatus}">
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

