<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Teacher Profile &amp; Security | ExamPro</title>

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
            <jsp:param name="active" value="profile"/>
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
                            <li class="breadcrumb-item active" aria-current="page">My Profile</li>
                        </ol>
                    </nav>
                </div>
                <div class="ep-topbar-actions">
                    <span class="text-secondary small d-none d-md-inline">
                        <i class="bi bi-person-check me-1"></i> Account: Active
                    </span>
                </div>
            </header>

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

                <!-- Profile Header Banner -->
                <div class="card bg-dark border-secondary p-4 mb-4">
                    <div class="d-flex flex-column flex-md-row align-items-center gap-3">
                        <div class="rounded-circle d-flex align-items-center justify-content-center text-white fw-bold fs-2"
                             style="width: 72px; height: 72px; background: linear-gradient(135deg, #a371f7, #6f42c1); box-shadow: 0 4px 20px rgba(163, 113, 247, 0.4);">
                            <c:out value="${currentUser.fullName.substring(0, 1).toUpperCase()}"/>
                        </div>
                        <div class="text-center text-md-start flex-grow-1">
                            <div class="d-flex flex-wrap align-items-center justify-content-center justify-content-md-start gap-2 mb-1">
                                <h1 class="h4 fw-bold text-white mb-0"><c:out value="${currentUser.fullName}"/></h1>
                                <span class="badge bg-purple px-2 py-1" style="background: rgba(163, 113, 247, 0.2); color: #a371f7; border: 1px solid rgba(163, 113, 247, 0.4);">
                                    <i class="bi bi-person-workspace me-1"></i> FACULTY INSTRUCTOR
                                </span>
                            </div>
                            <div class="text-secondary small">
                                <i class="bi bi-envelope me-1"></i> <code><c:out value="${currentUser.email}"/></code>
                                <span class="mx-2">&bull;</span>
                                <i class="bi bi-person-badge me-1"></i> Employee ID: <strong><c:out value="${teacher != null ? teacher.employeeId : 'N/A'}"/></strong>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="row g-4">

                    <!-- Column 1: Editable Profile Details -->
                    <div class="col-lg-7">
                        <div class="card bg-dark border-secondary mb-4">
                            <div class="card-header bg-transparent border-secondary py-3">
                                <h2 class="h6 fw-bold text-white mb-0">
                                    <i class="bi bi-person-gear text-accent me-2"></i> Instructor Profile Information
                                </h2>
                            </div>
                            <div class="card-body p-3 p-md-4">
                                <form method="post" action="${pageContext.request.contextPath}/teacher/profile">
                                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                                    <input type="hidden" name="action" value="updateProfile">

                                    <!-- Read-only core fields -->
                                    <div class="row g-3 mb-3">
                                        <div class="col-md-6">
                                            <label class="form-label small text-secondary">Full Name</label>
                                            <input type="text" class="form-control form-control-sm bg-secondary text-white border-secondary"
                                                   value="<c:out value='${currentUser.fullName}'/>" disabled>
                                            <small class="text-muted" style="font-size: 0.7rem;">Managed by administrator</small>
                                        </div>
                                        <div class="col-md-6">
                                            <label class="form-label small text-secondary">Email Address</label>
                                            <input type="email" class="form-control form-control-sm bg-secondary text-white border-secondary"
                                                   value="<c:out value='${currentUser.email}'/>" disabled>
                                            <small class="text-muted" style="font-size: 0.7rem;">Primary authentication identifier</small>
                                        </div>
                                    </div>

                                    <div class="row g-3 mb-3">
                                        <div class="col-md-6">
                                            <label class="form-label small text-secondary">Employee Identifier</label>
                                            <input type="text" class="form-control form-control-sm bg-secondary text-white border-secondary"
                                                   value="<c:out value='${teacher != null ? teacher.employeeId : ""}'/>" disabled>
                                        </div>
                                        <div class="col-md-6">
                                            <label class="form-label small text-secondary">System Role</label>
                                            <input type="text" class="form-control form-control-sm bg-secondary text-white border-secondary"
                                                   value="<c:out value='${currentUser.role}'/>" disabled>
                                        </div>
                                    </div>

                                    <hr class="border-secondary my-3">

                                    <!-- Permitted Editable Fields -->
                                    <div class="row g-3 mb-3">
                                        <div class="col-md-6">
                                            <label class="form-label small text-secondary">Contact Phone</label>
                                            <input type="text" name="phone" class="form-control form-control-sm bg-dark text-white border-secondary"
                                                   value="<c:out value='${teacher != null ? teacher.phone : ""}'/>"
                                                   placeholder="+1 555-0199">
                                        </div>
                                        <div class="col-md-6">
                                            <label class="form-label small text-secondary">Department / Faculty</label>
                                            <input type="text" name="department" class="form-control form-control-sm bg-dark text-white border-secondary"
                                                   value="<c:out value='${teacher != null ? teacher.department : ""}'/>"
                                                   placeholder="Computer Science &amp; Engineering">
                                        </div>
                                    </div>

                                    <div class="mb-4">
                                        <label class="form-label small text-secondary">Academic Qualification / Title</label>
                                        <input type="text" name="qualification" class="form-control form-control-sm bg-dark text-white border-secondary"
                                               value="<c:out value='${teacher != null ? teacher.qualification : ""}'/>"
                                               placeholder="Ph.D. in Computer Science, Professor">
                                    </div>

                                    <div class="d-flex justify-content-end">
                                        <button type="submit" class="btn btn-primary btn-sm px-4" style="background: #a371f7; border-color: #a371f7;">
                                            <i class="bi bi-save me-1"></i> Save Profile Details
                                        </button>
                                    </div>
                                </form>
                            </div>
                        </div>

                        <!-- Assigned Subjects List -->
                        <div class="card bg-dark border-secondary">
                            <div class="card-header bg-transparent border-secondary py-3">
                                <h2 class="h6 fw-bold text-white mb-0">
                                    <i class="bi bi-journal-check text-success me-2"></i> Authorized Subjects
                                </h2>
                            </div>
                            <div class="card-body p-3">
                                <c:choose>
                                    <c:when test="${not empty assignedSubjects}">
                                        <div class="list-group list-group-flush bg-transparent">
                                            <c:forEach var="sub" items="${assignedSubjects}">
                                                <div class="list-group-item bg-transparent border-secondary text-white d-flex justify-content-between align-items-center px-0">
                                                    <div>
                                                        <span class="badge bg-primary font-monospace me-2"><c:out value="${sub.code}"/></span>
                                                        <span class="fw-semibold"><c:out value="${sub.name}"/></span>
                                                    </div>
                                                    <span class="badge bg-secondary"><c:out value="${sub.credits}"/> Credits</span>
                                                </div>
                                            </c:forEach>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <p class="text-muted small mb-0">No assigned subjects. Contact the system administrator.</p>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>

                    <!-- Column 2: Security & Password Change -->
                    <div class="col-lg-5">
                        <div class="card bg-dark border-secondary">
                            <div class="card-header bg-transparent border-secondary py-3">
                                <h2 class="h6 fw-bold text-white mb-0">
                                    <i class="bi bi-shield-lock-fill text-warning me-2"></i> Change Password
                                </h2>
                            </div>
                            <div class="card-body p-3 p-md-4">
                                <form method="post" action="${pageContext.request.contextPath}/teacher/profile">
                                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                                    <input type="hidden" name="action" value="changePassword">

                                    <div class="mb-3">
                                        <label class="form-label small text-secondary">Current Password <span class="text-danger">*</span></label>
                                        <input type="password" name="currentPassword" class="form-control form-control-sm bg-dark text-white border-secondary"
                                               required autocomplete="current-password">
                                    </div>

                                    <div class="mb-3">
                                        <label class="form-label small text-secondary">New Password <span class="text-danger">*</span></label>
                                        <input type="password" name="newPassword" class="form-control form-control-sm bg-dark text-white border-secondary"
                                               minlength="8" required autocomplete="new-password">
                                        <small class="text-muted" style="font-size: 0.72rem;">Minimum 8 characters with letters, digits, and symbols</small>
                                    </div>

                                    <div class="mb-4">
                                        <label class="form-label small text-secondary">Confirm New Password <span class="text-danger">*</span></label>
                                        <input type="password" name="confirmPassword" class="form-control form-control-sm bg-dark text-white border-secondary"
                                               minlength="8" required autocomplete="new-password">
                                    </div>

                                    <button type="submit" class="btn btn-warning btn-sm w-100">
                                        <i class="bi bi-key-fill me-1"></i> Update Password
                                    </button>
                                </form>
                            </div>
                        </div>
                    </div>

                </div>

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

