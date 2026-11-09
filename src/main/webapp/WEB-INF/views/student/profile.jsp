<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Student Profile &amp; Security | ExamPro</title>

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
                            <li class="breadcrumb-item active" aria-current="page">My Profile</li>
                        </ol>
                    </nav>
                </div>
            </header>

            <!-- Profile Body -->
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

                <div class="row g-4">
                    <!-- Left: Profile Overview Card -->
                    <div class="col-lg-4">
                        <div class="card bg-dark border-secondary h-100 shadow-sm">
                            <div class="card-body text-center p-4">
                                <div class="rounded-circle d-inline-flex align-items-center justify-content-center mb-3"
                                     style="width: 80px; height: 80px; background: rgba(46, 160, 67, 0.2); color: #3fb950; font-size: 2.25rem;">
                                    <i class="bi bi-person-fill"></i>
                                </div>

                                <h5 class="fw-bold text-white mb-1">
                                    <c:out value="${student.fullName != null ? student.fullName : currentUser.fullName}"/>
                                </h5>
                                <div class="text-secondary small mb-3">
                                    <c:out value="${student.email != null ? student.email : currentUser.email}"/>
                                </div>

                                <div class="d-flex justify-content-center gap-2 mb-4">
                                    <span class="badge bg-success-subtle text-success border border-success-subtle px-3 py-1">
                                        <i class="bi bi-mortarboard-fill me-1"></i> Student Candidate
                                    </span>
                                    <span class="badge ${student.active ? 'bg-primary-subtle text-primary' : 'bg-danger-subtle text-danger'} px-3 py-1">
                                        ${student.active ? 'Active' : 'Inactive'}
                                    </span>
                                </div>

                                <ul class="list-group list-group-flush bg-transparent border-top border-secondary text-start small">
                                    <li class="list-group-item bg-transparent text-secondary border-secondary d-flex justify-content-between px-0 py-2">
                                        <span>Registration No:</span>
                                        <strong class="text-white"><c:out value="${student.registrationNo != null ? student.registrationNo : 'Not Assigned'}"/></strong>
                                    </li>
                                    <li class="list-group-item bg-transparent text-secondary border-secondary d-flex justify-content-between px-0 py-2">
                                        <span>Department:</span>
                                        <strong class="text-white"><c:out value="${student.department != null ? student.department : 'N/A'}"/></strong>
                                    </li>
                                    <li class="list-group-item bg-transparent text-secondary border-secondary d-flex justify-content-between px-0 py-2">
                                        <span>Class Level / Sem:</span>
                                        <strong class="text-white"><c:out value="${student.classLevel != null ? student.classLevel : 'N/A'}"/></strong>
                                    </li>
                                    <li class="list-group-item bg-transparent text-secondary border-secondary d-flex justify-content-between px-0 py-2">
                                        <span>Contact Phone:</span>
                                        <strong class="text-white"><c:out value="${student.phone != null ? student.phone : 'Not Set'}"/></strong>
                                    </li>
                                    <li class="list-group-item bg-transparent text-secondary border-secondary d-flex justify-content-between px-0 py-2">
                                        <span>Exams Completed:</span>
                                        <strong class="text-white"><c:out value="${student.examsAttempted}"/></strong>
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </div>

                    <!-- Right: Edit Details and Change Password Forms -->
                    <div class="col-lg-8">
                        <div class="card bg-dark border-secondary shadow-sm">
                            <div class="card-header bg-transparent border-secondary p-0">
                                <ul class="nav nav-tabs border-0 px-3 pt-2" id="profileTabs" role="tablist">
                                    <li class="nav-item" role="presentation">
                                        <button class="nav-link active bg-transparent text-white border-0 border-bottom border-success border-2 pb-2 fw-semibold"
                                                id="details-tab" data-bs-toggle="tab" data-bs-target="#detailsTab" type="button" role="tab">
                                            <i class="bi bi-person-gear me-1"></i> Edit Personal Information
                                        </button>
                                    </li>
                                    <li class="nav-item" role="presentation">
                                        <button class="nav-link bg-transparent text-secondary border-0 pb-2 fw-semibold"
                                                id="security-tab" data-bs-toggle="tab" data-bs-target="#securityTab" type="button" role="tab">
                                            <i class="bi bi-shield-lock me-1"></i> Change Password
                                        </button>
                                    </li>
                                </ul>
                            </div>

                            <div class="card-body p-4">
                                <div class="tab-content" id="profileTabsContent">

                                    <!-- TAB 1: Edit Profile -->
                                    <div class="tab-pane fade show active" id="detailsTab" role="tabpanel" aria-labelledby="details-tab">
                                        <form action="${pageContext.request.contextPath}/student/profile" method="post">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <input type="hidden" name="action" value="updateProfile">

                                            <div class="row g-3 mb-3">
                                                <div class="col-md-6">
                                                    <label class="form-label text-secondary small fw-semibold">Full Name (Admin Controlled)</label>
                                                    <input type="text" class="form-control bg-secondary text-white-50 border-secondary"
                                                           value="${student.fullName}" readonly disabled>
                                                </div>
                                                <div class="col-md-6">
                                                    <label class="form-label text-secondary small fw-semibold">Email Address (Login Identity)</label>
                                                    <input type="email" class="form-control bg-secondary text-white-50 border-secondary"
                                                           value="${student.email}" readonly disabled>
                                                </div>
                                            </div>

                                            <div class="row g-3 mb-3">
                                                <div class="col-md-6">
                                                    <label class="form-label text-secondary small fw-semibold">Registration Number</label>
                                                    <input type="text" class="form-control bg-secondary text-white-50 border-secondary"
                                                           value="${student.registrationNo}" readonly disabled>
                                                    <div class="form-text text-muted small">Managed centrally by administration.</div>
                                                </div>
                                                <div class="col-md-6">
                                                    <label for="phone" class="form-label text-white small fw-semibold">Phone Number</label>
                                                    <input type="text" class="form-control bg-dark text-white border-secondary"
                                                           id="phone" name="phone" value="${student.phone}" placeholder="+1 234 567 8900">
                                                </div>
                                            </div>

                                            <div class="row g-3 mb-4">
                                                <div class="col-md-6">
                                                    <label for="department" class="form-label text-white small fw-semibold">Department / Stream</label>
                                                    <input type="text" class="form-control bg-dark text-white border-secondary"
                                                           id="department" name="department" value="${student.department}" placeholder="Computer Science">
                                                </div>
                                                <div class="col-md-6">
                                                    <label for="classLevel" class="form-label text-white small fw-semibold">Class / Semester Level</label>
                                                    <input type="text" class="form-control bg-dark text-white border-secondary"
                                                           id="classLevel" name="classLevel" value="${student.classLevel}" placeholder="Semester 6">
                                                </div>
                                            </div>

                                            <div class="text-end">
                                                <button type="submit" class="btn btn-success px-4" style="background: #2ea043; border-color: #2ea043;">
                                                    <i class="bi bi-check-circle me-1"></i> Save Profile Details
                                                </button>
                                            </div>
                                        </form>
                                    </div>

                                    <!-- TAB 2: Change Password -->
                                    <div class="tab-pane fade" id="securityTab" role="tabpanel" aria-labelledby="security-tab">
                                        <form action="${pageContext.request.contextPath}/student/profile" method="post">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <input type="hidden" name="action" value="changePassword">

                                            <div class="mb-3">
                                                <label for="currentPassword" class="form-label text-white small fw-semibold">Current Password</label>
                                                <input type="password" class="form-control bg-dark text-white border-secondary"
                                                       id="currentPassword" name="currentPassword" required autocomplete="current-password">
                                            </div>

                                            <div class="mb-3">
                                                <label for="newPassword" class="form-label text-white small fw-semibold">New Password</label>
                                                <input type="password" class="form-control bg-dark text-white border-secondary"
                                                       id="newPassword" name="newPassword" required minlength="8" autocomplete="new-password">
                                                <div class="form-text text-secondary small">Minimum 8 characters with letters, numbers, or symbols.</div>
                                            </div>

                                            <div class="mb-4">
                                                <label for="confirmPassword" class="form-label text-white small fw-semibold">Confirm New Password</label>
                                                <input type="password" class="form-control bg-dark text-white border-secondary"
                                                       id="confirmPassword" name="confirmPassword" required minlength="8" autocomplete="new-password">
                                            </div>

                                            <div class="text-end">
                                                <button type="submit" class="btn btn-warning px-4 fw-bold">
                                                    <i class="bi bi-shield-check me-1"></i> Update Password
                                                </button>
                                            </div>
                                        </form>
                                    </div>

                                </div>
                            </div>
                        </div>
                    </div>
                </div>

            </div>
        </main>
    </div>

    <!-- Bootstrap 5 JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
            crossorigin="anonymous"></script>

    <!-- Sidebar & Tab Switching Script -->
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

            // Tab styling active toggle
            const triggerTabList = document.querySelectorAll('#profileTabs button');
            triggerTabList.forEach(triggerEl => {
                triggerEl.addEventListener('shown.bs.tab', event => {
                    triggerTabList.forEach(t => {
                        t.classList.remove('text-white', 'border-bottom', 'border-success', 'border-2');
                        t.classList.add('text-secondary');
                    });
                    event.target.classList.remove('text-secondary');
                    event.target.classList.add('text-white', 'border-bottom', 'border-success', 'border-2');
                });
            });
        });
    </script>
</body>
</html>

