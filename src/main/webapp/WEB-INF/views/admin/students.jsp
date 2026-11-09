<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.exampro.model.User" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Student Management | ExamPro Admin</title>

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
            <jsp:param name="active" value="students"/>
        </jsp:include>

        <!-- ═══════════════════════════════════════
             MAIN CONTENT
        ═══════════════════════════════════════ -->
        <div class="ep-admin-main">

            <!-- Topbar -->
            <header class="ep-admin-topbar">
                <div class="d-flex align-items-center gap-3">
                    <button class="btn btn-outline-secondary btn-sm d-lg-none" id="sidebarToggle" type="button" aria-label="Toggle sidebar navigation">
                        <i class="bi bi-list fs-5"></i>
                    </button>
                    <nav aria-label="breadcrumb">
                        <ol class="ep-topbar-breadcrumb list-unstyled m-0">
                            <li><a href="${pageContext.request.contextPath}/"><i class="bi bi-house-door me-1"></i>Home</a></li>
                            <li class="text-muted">/</li>
                            <li><a href="${pageContext.request.contextPath}/admin/dashboard">Admin</a></li>
                            <li class="text-muted">/</li>
                            <li class="active" aria-current="page">Students</li>
                        </ol>
                    </nav>
                </div>
                <div class="ep-topbar-actions d-flex align-items-center">
                    <jsp:include page="/WEB-INF/views/common/notif_bell.jsp"/>
                    <div class="d-flex align-items-center gap-2 ps-2 border-start border-secondary">
                        <span class="badge badge-role-admin d-none d-sm-inline-block">ADMIN</span>
                        <span class="text-white small fw-medium d-none d-sm-inline-block">
                            <c:out value="${not empty sessionScope.userName ? sessionScope.userName : currentUser.fullName}"/>
                        </span>
                        <form action="${pageContext.request.contextPath}/logout" method="post" class="m-0">
                            <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                            <button type="submit" class="btn btn-outline-danger btn-sm" title="Sign out">
                                <i class="bi bi-box-arrow-right"></i>
                            </button>
                        </form>
                    </div>
                </div>
            </header>

            <main class="container-fluid p-4 p-lg-5">

                <!-- Flash Alerts -->
                <c:if test="${not empty flashSuccess}">
                    <div class="alert alert-success alert-dismissible fade show d-flex align-items-center mb-4" role="alert">
                        <i class="bi bi-check-circle-fill fs-5 me-3"></i>
                        <div><c:out value="${flashSuccess}"/></div>
                        <button type="button" class="btn-close ms-auto" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:if>

                <c:if test="${not empty flashError}">
                    <div class="alert alert-danger alert-dismissible fade show d-flex align-items-center mb-4" role="alert">
                        <i class="bi bi-exclamation-triangle-fill fs-5 me-3"></i>
                        <div><c:out value="${flashError}"/></div>
                        <button type="button" class="btn-close ms-auto" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:if>

                <!-- Page Header -->
                <div class="d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-3 mb-4">
                    <div>
                        <h1 class="h3 fw-bold text-white mb-1">
                            <i class="bi bi-people-fill text-success me-2"></i>Student Management
                        </h1>
                        <p class="text-secondary small mb-0">
                            Enroll students, manage identity credentials, verify registrations, and manage account statuses.
                        </p>
                    </div>
                    <div>
                        <button type="button" class="btn btn-success d-inline-flex align-items-center gap-2 px-3 py-2" data-bs-toggle="modal" data-bs-target="#addStudentModal">
                            <i class="bi bi-person-plus-fill"></i>
                            <span class="fw-semibold">Add New Student</span>
                        </button>
                    </div>
                </div>

                <!-- Search & Filters Bar -->
                <div class="card bg-dark border-secondary mb-4">
                    <div class="card-body p-3">
                        <form action="${pageContext.request.contextPath}/admin/students" method="get" class="row g-2 align-items-center">
                            <div class="col-md-9 col-lg-10">
                                <div class="input-group">
                                    <span class="input-group-text bg-black border-secondary text-secondary">
                                        <i class="bi bi-search"></i>
                                    </span>
                                    <input type="text" name="search" class="form-control bg-black text-white border-secondary"
                                           placeholder="Search by student name, email, or registration number..."
                                           value="<c:out value='${searchQuery}'/>">
                                </div>
                            </div>
                            <div class="col-md-3 col-lg-2 d-flex gap-2">
                                <button type="submit" class="btn btn-primary flex-grow-1">Search</button>
                                <c:if test="${not empty searchQuery}">
                                    <a href="${pageContext.request.contextPath}/admin/students" class="btn btn-outline-secondary" title="Clear search">
                                        <i class="bi bi-x-lg"></i>
                                    </a>
                                </c:if>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- Students Table Card -->
                <div class="ep-card">
                    <div class="ep-card-header d-flex align-items-center justify-content-between">
                        <h2 class="ep-card-title m-0 h5">
                            <i class="bi bi-list-check text-info me-2"></i>Student Directory
                        </h2>
                        <span class="badge bg-dark border border-secondary text-secondary">
                            Total: <strong class="text-white"><c:out value="${totalCount}"/></strong> Students
                        </span>
                    </div>

                    <div class="table-responsive">
                        <c:choose>
                            <c:when test="${not empty students}">
                                <table class="ep-table-custom" aria-label="Students Directory">
                                    <thead>
                                        <tr>
                                            <th scope="col">Reg. Number</th>
                                            <th scope="col">Student</th>
                                            <th scope="col">Department &amp; Class</th>
                                            <th scope="col">Contact</th>
                                            <th scope="col">Status</th>
                                            <th scope="col">Joined</th>
                                            <th scope="col" class="text-end">Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="s" items="${students}">
                                            <tr>
                                                <td>
                                                    <span class="badge bg-black border border-secondary font-monospace text-light px-2 py-1">
                                                        <c:out value="${not empty s.registrationNo ? s.registrationNo : 'N/A'}"/>
                                                    </span>
                                                </td>
                                                <td>
                                                    <div class="d-flex align-items-center gap-2">
                                                        <div class="ep-admin-avatar" style="width: 34px; height: 34px; font-size: 0.85rem; background: linear-gradient(135deg, #238636, #2ea043);">
                                                            <c:out value="${fn:substring(s.fullName, 0, 1)}"/>
                                                        </div>
                                                        <div>
                                                            <div class="fw-semibold text-white">
                                                                <c:out value="${s.fullName}"/>
                                                            </div>
                                                            <div class="small text-secondary font-monospace" style="font-size: 0.8rem;">
                                                                <c:out value="${s.email}"/>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </td>
                                                <td>
                                                    <div class="text-light small fw-medium">
                                                        <c:out value="${not empty s.department ? s.department : 'General'}"/>
                                                    </div>
                                                    <div class="text-secondary small">
                                                        <c:out value="${not empty s.classLevel ? s.classLevel : 'Year 1'}"/>
                                                    </div>
                                                </td>
                                                <td class="text-secondary small font-monospace">
                                                    <c:out value="${not empty s.phone ? s.phone : '—'}"/>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${s.active}">
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
                                                    <c:out value="${s.formattedCreatedAt}"/>
                                                </td>
                                                <td class="text-end">
                                                    <div class="d-inline-flex gap-1">
                                                        <!-- View Button -->
                                                        <button type="button" class="btn btn-outline-info btn-sm view-student-btn"
                                                                data-bs-toggle="modal" data-bs-target="#viewStudentModal"
                                                                data-id="<c:out value='${s.id}'/>"
                                                                data-name="<c:out value='${s.fullName}'/>"
                                                                data-email="<c:out value='${s.email}'/>"
                                                                data-reg="<c:out value='${s.registrationNo}'/>"
                                                                data-phone="<c:out value='${s.phone}'/>"
                                                                data-dept="<c:out value='${s.department}'/>"
                                                                data-class="<c:out value='${s.classLevel}'/>"
                                                                data-active="<c:out value='${s.active}'/>"
                                                                data-exams="<c:out value='${s.examsAttempted}'/>"
                                                                data-created="<c:out value='${s.formattedCreatedAt}'/>"
                                                                title="View Student Details">
                                                            <i class="bi bi-eye"></i>
                                                        </button>

                                                        <!-- Edit Button -->
                                                        <button type="button" class="btn btn-outline-warning btn-sm edit-student-btn"
                                                                data-bs-toggle="modal" data-bs-target="#editStudentModal"
                                                                data-id="<c:out value='${s.id}'/>"
                                                                data-name="<c:out value='${s.fullName}'/>"
                                                                data-email="<c:out value='${s.email}'/>"
                                                                data-reg="<c:out value='${s.registrationNo}'/>"
                                                                data-phone="<c:out value='${s.phone}'/>"
                                                                data-dept="<c:out value='${s.department}'/>"
                                                                data-class="<c:out value='${s.classLevel}'/>"
                                                                data-active="<c:out value='${s.active}'/>"
                                                                title="Edit Student">
                                                            <i class="bi bi-pencil-square"></i>
                                                        </button>

                                                        <!-- Toggle Status Form (Prefer deactivation over permanent deletion) -->
                                                        <form action="${pageContext.request.contextPath}/admin/students" method="post" class="d-inline m-0">
                                                            <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                                                            <input type="hidden" name="action" value="toggle-status">
                                                            <input type="hidden" name="id" value="<c:out value='${s.id}'/>">
                                                            <input type="hidden" name="active" value="<c:out value='${not s.active}'/>">
                                                            <button type="submit" class="btn btn-sm ${s.active ? 'btn-outline-danger' : 'btn-outline-success'}"
                                                                    title="${s.active ? 'Deactivate Student Account' : 'Activate Student Account'}">
                                                                <i class="bi ${s.active ? 'bi-person-dash' : 'bi-person-check'}"></i>
                                                            </button>
                                                        </form>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </c:when>
                            <c:otherwise>
                                <div class="text-center py-5 text-muted">
                                    <i class="bi bi-people display-4 d-block mb-3 opacity-50"></i>
                                    <h3 class="h5 text-white mb-1">No Students Found</h3>
                                    <p class="small text-secondary mb-3">
                                        <c:choose>
                                            <c:when test="${not empty searchQuery}">
                                                No student records match search: "<strong><c:out value="${searchQuery}"/></strong>".
                                            </c:when>
                                            <c:otherwise>
                                                No student accounts have been created yet.
                                            </c:otherwise>
                                        </c:choose>
                                    </p>
                                    <button type="button" class="btn btn-success btn-sm" data-bs-toggle="modal" data-bs-target="#addStudentModal">
                                        <i class="bi bi-person-plus-fill me-1"></i>Add First Student
                                    </button>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <!-- Pagination -->
                    <c:if test="${totalPages > 1}">
                        <div class="card-footer bg-dark border-secondary p-3 d-flex align-items-center justify-content-between">
                            <div class="small text-secondary">
                                Showing page <strong class="text-white"><c:out value="${currentPage}"/></strong> of <strong class="text-white"><c:out value="${totalPages}"/></strong>
                            </div>
                            <nav aria-label="Student directory pagination">
                                <ul class="pagination pagination-sm m-0">
                                    <li class="page-item ${currentPage le 1 ? 'disabled' : ''}">
                                        <a class="page-link bg-black border-secondary text-light" href="${pageContext.request.contextPath}/admin/students?page=${currentPage - 1}&search=<c:out value='${searchQuery}'/>">
                                            Previous
                                        </a>
                                    </li>
                                    <c:forEach var="p" begin="1" end="${totalPages}">
                                        <li class="page-item ${p eq currentPage ? 'active' : ''}">
                                            <a class="page-link ${p eq currentPage ? 'bg-primary border-primary text-white' : 'bg-black border-secondary text-light'}"
                                               href="${pageContext.request.contextPath}/admin/students?page=${p}&search=<c:out value='${searchQuery}'/>">
                                                <c:out value="${p}"/>
                                            </a>
                                        </li>
                                    </c:forEach>
                                    <li class="page-item ${currentPage ge totalPages ? 'disabled' : ''}">
                                        <a class="page-link bg-black border-secondary text-light" href="${pageContext.request.contextPath}/admin/students?page=${currentPage + 1}&search=<c:out value='${searchQuery}'/>">
                                            Next
                                        </a>
                                    </li>
                                </ul>
                            </nav>
                        </div>
                    </c:if>
                </div>

            </main>
        </div>
    </div>

    <!-- ═══════════════════════════════════════
         ADD STUDENT MODAL
    ═══════════════════════════════════════ -->
    <div class="modal fade" id="addStudentModal" tabindex="-1" aria-labelledby="addStudentModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered modal-lg">
            <div class="modal-content bg-dark border border-secondary text-white">
                <form action="${pageContext.request.contextPath}/admin/students" method="post" id="addStudentForm">
                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                    <input type="hidden" name="action" value="add">

                    <div class="modal-header border-bottom border-secondary">
                        <h5 class="modal-title" id="addStudentModalLabel">
                            <i class="bi bi-person-plus-fill text-success me-2"></i>Register New Student
                        </h5>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>

                    <div class="modal-body p-4">
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label for="addFullName" class="form-label text-secondary small fw-semibold">Full Legal Name <span class="text-danger">*</span></label>
                                <input type="text" class="form-control bg-black text-white border-secondary" id="addFullName" name="fullName" required placeholder="e.g. John Doe">
                            </div>
                            <div class="col-md-6">
                                <label for="addEmail" class="form-label text-secondary small fw-semibold">Email Address (Login ID) <span class="text-danger">*</span></label>
                                <input type="email" class="form-control bg-black text-white border-secondary" id="addEmail" name="email" required placeholder="student@example.com">
                            </div>
                            <div class="col-md-6">
                                <label for="addPassword" class="form-label text-secondary small fw-semibold">Initial Password <span class="text-danger">*</span></label>
                                <input type="password" class="form-control bg-black text-white border-secondary" id="addPassword" name="password" required minlength="6" placeholder="Min. 6 characters">
                            </div>
                            <div class="col-md-6">
                                <label for="addRegNo" class="form-label text-secondary small fw-semibold">Registration / Roll No. (Optional)</label>
                                <input type="text" class="form-control bg-black text-white border-secondary" id="addRegNo" name="registrationNo" placeholder="Auto-generated if blank">
                            </div>
                            <div class="col-md-6">
                                <label for="addDepartment" class="form-label text-secondary small fw-semibold">Department / Faculty</label>
                                <input type="text" class="form-control bg-black text-white border-secondary" id="addDepartment" name="department" placeholder="e.g. Computer Science">
                            </div>
                            <div class="col-md-6">
                                <label for="addClassLevel" class="form-label text-secondary small fw-semibold">Class / Batch / Semester</label>
                                <input type="text" class="form-control bg-black text-white border-secondary" id="addClassLevel" name="classLevel" placeholder="e.g. Batch 2026, Year 1">
                            </div>
                            <div class="col-12">
                                <label for="addPhone" class="form-label text-secondary small fw-semibold">Phone Number</label>
                                <input type="tel" class="form-control bg-black text-white border-secondary" id="addPhone" name="phone" placeholder="+1 (555) 000-0000">
                            </div>
                        </div>
                    </div>

                    <div class="modal-footer border-top border-secondary">
                        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-success btn-sm px-3">Create Student Account</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- ═══════════════════════════════════════
         EDIT STUDENT MODAL
    ═══════════════════════════════════════ -->
    <div class="modal fade" id="editStudentModal" tabindex="-1" aria-labelledby="editStudentModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered modal-lg">
            <div class="modal-content bg-dark border border-secondary text-white">
                <form action="${pageContext.request.contextPath}/admin/students" method="post" id="editStudentForm">
                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                    <input type="hidden" name="action" value="edit">
                    <input type="hidden" name="id" id="editStudentId">

                    <div class="modal-header border-bottom border-secondary">
                        <h5 class="modal-title" id="editStudentModalLabel">
                            <i class="bi bi-pencil-square text-warning me-2"></i>Edit Student Information
                        </h5>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>

                    <div class="modal-body p-4">
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label for="editFullName" class="form-label text-secondary small fw-semibold">Full Legal Name <span class="text-danger">*</span></label>
                                <input type="text" class="form-control bg-black text-white border-secondary" id="editFullName" name="fullName" required>
                            </div>
                            <div class="col-md-6">
                                <label for="editEmail" class="form-label text-secondary small fw-semibold">Email Address <span class="text-danger">*</span></label>
                                <input type="email" class="form-control bg-black text-white border-secondary" id="editEmail" name="email" required>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-secondary small fw-semibold">Registration Number</label>
                                <input type="text" class="form-control bg-dark text-muted border-secondary font-monospace" id="editRegNo" readonly>
                            </div>
                            <div class="col-md-6">
                                <label for="editPhone" class="form-label text-secondary small fw-semibold">Phone Number</label>
                                <input type="tel" class="form-control bg-black text-white border-secondary" id="editPhone" name="phone">
                            </div>
                            <div class="col-md-6">
                                <label for="editDepartment" class="form-label text-secondary small fw-semibold">Department</label>
                                <input type="text" class="form-control bg-black text-white border-secondary" id="editDepartment" name="department">
                            </div>
                            <div class="col-md-6">
                                <label for="editClassLevel" class="form-label text-secondary small fw-semibold">Class / Batch</label>
                                <input type="text" class="form-control bg-black text-white border-secondary" id="editClassLevel" name="classLevel">
                            </div>
                            <div class="col-12">
                                <div class="form-check form-switch mt-2">
                                    <input class="form-check-input" type="checkbox" role="switch" id="editActive" name="active" value="true">
                                    <label class="form-check-label text-light small fw-semibold" for="editActive">Account Active Status</label>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="modal-footer border-top border-secondary">
                        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-warning btn-sm px-3">Save Changes</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- ═══════════════════════════════════════
         VIEW STUDENT DETAILS MODAL
    ═══════════════════════════════════════ -->
    <div class="modal fade" id="viewStudentModal" tabindex="-1" aria-labelledby="viewStudentModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content bg-dark border border-secondary text-white">
                <div class="modal-header border-bottom border-secondary">
                    <h5 class="modal-title" id="viewStudentModalLabel">
                        <i class="bi bi-person-badge text-info me-2"></i>Student Profile Details
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body p-4">
                    <div class="text-center mb-4">
                        <div class="ep-admin-avatar mx-auto mb-2" id="viewAvatar" style="width: 56px; height: 56px; font-size: 1.5rem; background: linear-gradient(135deg, #238636, #2ea043);">
                            S
                        </div>
                        <h4 class="h5 fw-bold text-white mb-0" id="viewFullName">Student Name</h4>
                        <span class="font-monospace text-secondary small" id="viewEmail">student@example.com</span>
                    </div>

                    <ul class="list-unstyled d-flex flex-column gap-2 mb-0">
                        <li class="d-flex justify-content-between p-2 rounded bg-black border border-secondary">
                            <span class="text-secondary small">Registration Number</span>
                            <span class="font-monospace text-light small fw-bold" id="viewRegNo">—</span>
                        </li>
                        <li class="d-flex justify-content-between p-2 rounded bg-black border border-secondary">
                            <span class="text-secondary small">Department</span>
                            <span class="text-light small" id="viewDepartment">—</span>
                        </li>
                        <li class="d-flex justify-content-between p-2 rounded bg-black border border-secondary">
                            <span class="text-secondary small">Class / Batch</span>
                            <span class="text-light small" id="viewClassLevel">—</span>
                        </li>
                        <li class="d-flex justify-content-between p-2 rounded bg-black border border-secondary">
                            <span class="text-secondary small">Contact Phone</span>
                            <span class="font-monospace text-light small" id="viewPhone">—</span>
                        </li>
                        <li class="d-flex justify-content-between p-2 rounded bg-black border border-secondary">
                            <span class="text-secondary small">Account Status</span>
                            <span id="viewStatusBadge"></span>
                        </li>
                        <li class="d-flex justify-content-between p-2 rounded bg-black border border-secondary">
                            <span class="text-secondary small">Exams Attempted</span>
                            <span class="badge bg-primary" id="viewExamsAttempted">0</span>
                        </li>
                        <li class="d-flex justify-content-between p-2 rounded bg-black border border-secondary">
                            <span class="text-secondary small">Date Registered</span>
                            <span class="text-secondary small" id="viewCreatedAt">—</span>
                        </li>
                    </ul>
                </div>
                <div class="modal-footer border-top border-secondary">
                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Close</button>
                </div>
            </div>
        </div>
    </div>

    <!-- Bootstrap 5 JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-YvpcrYf0tY3lHB60NNkmXc4s9bIOgUxi8T/jzmLe3zxj8NxO8yVMnN5Z2X5ZZzEB"
            crossorigin="anonymous"></script>

    <!-- Interactive script -->
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            // Sidebar Mobile Toggle
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

            // Auto-open Add Modal if requested via URL param
            var urlParams = new URLSearchParams(window.location.search);
            if (urlParams.get('openAdd') === 'true') {
                var addModal = new bootstrap.Modal(document.getElementById('addStudentModal'));
                addModal.show();
            }

            // Edit Modal Population
            var editButtons = document.querySelectorAll('.edit-student-btn');
            editButtons.forEach(function (btn) {
                btn.addEventListener('click', function () {
                    document.getElementById('editStudentId').value = this.getAttribute('data-id') || '';
                    document.getElementById('editFullName').value = this.getAttribute('data-name') || '';
                    document.getElementById('editEmail').value = this.getAttribute('data-email') || '';
                    document.getElementById('editRegNo').value = this.getAttribute('data-reg') || '';
                    document.getElementById('editPhone').value = this.getAttribute('data-phone') || '';
                    document.getElementById('editDepartment').value = this.getAttribute('data-dept') || '';
                    document.getElementById('editClassLevel').value = this.getAttribute('data-class') || '';
                    document.getElementById('editActive').checked = (this.getAttribute('data-active') === 'true');
                });
            });

            // View Modal Population
            var viewButtons = document.querySelectorAll('.view-student-btn');
            viewButtons.forEach(function (btn) {
                btn.addEventListener('click', function () {
                    var name = this.getAttribute('data-name') || 'Student';
                    document.getElementById('viewFullName').textContent = name;
                    document.getElementById('viewEmail').textContent = this.getAttribute('data-email') || '';
                    document.getElementById('viewRegNo').textContent = this.getAttribute('data-reg') || '—';
                    document.getElementById('viewPhone').textContent = this.getAttribute('data-phone') || '—';
                    document.getElementById('viewDepartment').textContent = this.getAttribute('data-dept') || '—';
                    document.getElementById('viewClassLevel').textContent = this.getAttribute('data-class') || '—';
                    document.getElementById('viewCreatedAt').textContent = this.getAttribute('data-created') || '—';
                    document.getElementById('viewExamsAttempted').textContent = this.getAttribute('data-exams') || '0';
                    document.getElementById('viewAvatar').textContent = name.charAt(0).toUpperCase();

                    var isActive = (this.getAttribute('data-active') === 'true');
                    var statusBadge = document.getElementById('viewStatusBadge');
                    if (isActive) {
                        statusBadge.className = 'badge bg-success bg-opacity-10 text-success border border-success border-opacity-25 px-2 py-1';
                        statusBadge.innerHTML = '<i class="bi bi-check-circle-fill me-1"></i>Active';
                    } else {
                        statusBadge.className = 'badge bg-danger bg-opacity-10 text-danger border border-danger border-opacity-25 px-2 py-1';
                        statusBadge.innerHTML = '<i class="bi bi-slash-circle me-1"></i>Inactive';
                    }
                });
            });
        });
    </script>
</body>
</html>
