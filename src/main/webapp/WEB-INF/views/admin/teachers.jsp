<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.exampro.model.User" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Faculty Management | ExamPro Admin</title>

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
            <jsp:param name="active" value="teachers"/>
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
                            <li class="active" aria-current="page">Teachers</li>
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
                            <i class="bi bi-person-video3 me-2" style="color: #a371f7;"></i>Faculty &amp; Teacher Management
                        </h1>
                        <p class="text-secondary small mb-0">
                            Manage instructional staff, configure academic departments, and assign curriculum subjects.
                        </p>
                    </div>
                    <div>
                        <button type="button" class="btn btn-primary d-inline-flex align-items-center gap-2 px-3 py-2" data-bs-toggle="modal" data-bs-target="#addTeacherModal" style="background-color: #8957e5; border-color: #8957e5;">
                            <i class="bi bi-person-plus-fill"></i>
                            <span class="fw-semibold">Add New Teacher</span>
                        </button>
                    </div>
                </div>

                <!-- Search Bar -->
                <div class="card bg-dark border-secondary mb-4">
                    <div class="card-body p-3">
                        <form action="${pageContext.request.contextPath}/admin/teachers" method="get" class="row g-2 align-items-center">
                            <div class="col-md-9 col-lg-10">
                                <div class="input-group">
                                    <span class="input-group-text bg-black border-secondary text-secondary">
                                        <i class="bi bi-search"></i>
                                    </span>
                                    <input type="text" name="search" class="form-control bg-black text-white border-secondary"
                                           placeholder="Search by faculty name, email, or employee ID..."
                                           value="<c:out value='${searchQuery}'/>">
                                </div>
                            </div>
                            <div class="col-md-3 col-lg-2 d-flex gap-2">
                                <button type="submit" class="btn btn-primary flex-grow-1">Search</button>
                                <c:if test="${not empty searchQuery}">
                                    <a href="${pageContext.request.contextPath}/admin/teachers" class="btn btn-outline-secondary" title="Clear search">
                                        <i class="bi bi-x-lg"></i>
                                    </a>
                                </c:if>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- Teachers Directory Table Card -->
                <div class="ep-card">
                    <div class="ep-card-header d-flex align-items-center justify-content-between">
                        <h2 class="ep-card-title m-0 h5">
                            <i class="bi bi-people-fill text-warning me-2"></i>Faculty Directory
                        </h2>
                        <span class="badge bg-dark border border-secondary text-secondary">
                            Total: <strong class="text-white"><c:out value="${totalCount}"/></strong> Faculty Members
                        </span>
                    </div>

                    <div class="table-responsive">
                        <c:choose>
                            <c:when test="${not empty teachers}">
                                <table class="ep-table-custom" aria-label="Faculty Directory">
                                    <thead>
                                        <tr>
                                            <th scope="col">Employee ID</th>
                                            <th scope="col">Faculty Member</th>
                                            <th scope="col">Department &amp; Title</th>
                                            <th scope="col">Assigned Subjects</th>
                                            <th scope="col">Status</th>
                                            <th scope="col">Joined</th>
                                            <th scope="col" class="text-end">Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="t" items="${teachers}">
                                            <tr>
                                                <td>
                                                    <span class="badge bg-black border border-secondary font-monospace text-light px-2 py-1">
                                                        <c:out value="${not empty t.employeeId ? t.employeeId : 'N/A'}"/>
                                                    </span>
                                                </td>
                                                <td>
                                                    <div class="d-flex align-items-center gap-2">
                                                        <div class="ep-admin-avatar" style="width: 34px; height: 34px; font-size: 0.85rem; background: linear-gradient(135deg, #a371f7, #8957e5);">
                                                            <c:out value="${fn:substring(t.fullName, 0, 1)}"/>
                                                        </div>
                                                        <div>
                                                            <div class="fw-semibold text-white">
                                                                <c:out value="${t.fullName}"/>
                                                            </div>
                                                            <div class="small text-secondary font-monospace" style="font-size: 0.8rem;">
                                                                <c:out value="${t.email}"/>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </td>
                                                <td>
                                                    <div class="text-light small fw-medium">
                                                        <c:out value="${not empty t.department ? t.department : 'General'}"/>
                                                    </div>
                                                    <div class="text-secondary small">
                                                        <c:out value="${not empty t.qualification ? t.qualification : 'Faculty'}"/>
                                                    </div>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${not empty t.assignedSubjects}">
                                                            <div class="d-flex flex-wrap gap-1">
                                                                <c:forEach var="sub" items="${t.assignedSubjects}">
                                                                    <span class="badge bg-info bg-opacity-10 text-info border border-info border-opacity-25" title="<c:out value='${sub.name}'/>">
                                                                        <c:out value="${sub.code}"/>
                                                                    </span>
                                                                </c:forEach>
                                                            </div>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="text-muted small">None assigned</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${t.active}">
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
                                                    <c:out value="${t.formattedCreatedAt}"/>
                                                </td>
                                                <td class="text-end">
                                                    <div class="d-inline-flex gap-1">
                                                        <!-- View Button -->
                                                        <button type="button" class="btn btn-outline-info btn-sm view-teacher-btn"
                                                                data-bs-toggle="modal" data-bs-target="#viewTeacherModal"
                                                                data-id="<c:out value='${t.id}'/>"
                                                                data-name="<c:out value='${t.fullName}'/>"
                                                                data-email="<c:out value='${t.email}'/>"
                                                                data-empid="<c:out value='${t.employeeId}'/>"
                                                                data-phone="<c:out value='${t.phone}'/>"
                                                                data-dept="<c:out value='${t.department}'/>"
                                                                data-qual="<c:out value='${t.qualification}'/>"
                                                                data-active="<c:out value='${t.active}'/>"
                                                                data-created="<c:out value='${t.formattedCreatedAt}'/>"
                                                                data-subjects="<c:out value='${t.assignedSubjectCodes}'/>"
                                                                title="View Faculty Details">
                                                            <i class="bi bi-eye"></i>
                                                        </button>

                                                        <!-- Edit Button -->
                                                        <button type="button" class="btn btn-outline-warning btn-sm edit-teacher-btn"
                                                                data-bs-toggle="modal" data-bs-target="#editTeacherModal"
                                                                data-id="<c:out value='${t.id}'/>"
                                                                data-name="<c:out value='${t.fullName}'/>"
                                                                data-email="<c:out value='${t.email}'/>"
                                                                data-empid="<c:out value='${t.employeeId}'/>"
                                                                data-phone="<c:out value='${t.phone}'/>"
                                                                data-dept="<c:out value='${t.department}'/>"
                                                                data-qual="<c:out value='${t.qualification}'/>"
                                                                data-active="<c:out value='${t.active}'/>"
                                                                data-subids="<c:forEach var='s' items='${t.assignedSubjects}'>${s.id},</c:forEach>"
                                                                title="Edit Faculty Member">
                                                            <i class="bi bi-pencil-square"></i>
                                                        </button>

                                                        <!-- Toggle Status -->
                                                        <form action="${pageContext.request.contextPath}/admin/teachers" method="post" class="d-inline m-0">
                                                            <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                                                            <input type="hidden" name="action" value="toggle-status">
                                                            <input type="hidden" name="id" value="<c:out value='${t.id}'/>">
                                                            <input type="hidden" name="active" value="<c:out value='${not t.active}'/>">
                                                            <button type="submit" class="btn btn-sm ${t.active ? 'btn-outline-danger' : 'btn-outline-success'}"
                                                                    title="${t.active ? 'Deactivate Teacher' : 'Activate Teacher'}">
                                                                <i class="bi ${t.active ? 'bi-person-dash' : 'bi-person-check'}"></i>
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
                                    <i class="bi bi-person-video3 display-4 d-block mb-3 opacity-50" style="color: #a371f7;"></i>
                                    <h3 class="h5 text-white mb-1">No Faculty Members Found</h3>
                                    <p class="small text-secondary mb-3">
                                        <c:choose>
                                            <c:when test="${not empty searchQuery}">
                                                No faculty records match search: "<strong><c:out value="${searchQuery}"/></strong>".
                                            </c:when>
                                            <c:otherwise>
                                                No teacher accounts have been created yet.
                                            </c:otherwise>
                                        </c:choose>
                                    </p>
                                    <button type="button" class="btn btn-primary btn-sm" data-bs-toggle="modal" data-bs-target="#addTeacherModal" style="background-color: #8957e5; border-color: #8957e5;">
                                        <i class="bi bi-person-plus-fill me-1"></i>Add First Teacher
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
                            <nav aria-label="Faculty directory pagination">
                                <ul class="pagination pagination-sm m-0">
                                    <li class="page-item ${currentPage le 1 ? 'disabled' : ''}">
                                        <a class="page-link bg-black border-secondary text-light" href="${pageContext.request.contextPath}/admin/teachers?page=${currentPage - 1}&search=<c:out value='${searchQuery}'/>">
                                            Previous
                                        </a>
                                    </li>
                                    <c:forEach var="p" begin="1" end="${totalPages}">
                                        <li class="page-item ${p eq currentPage ? 'active' : ''}">
                                            <a class="page-link ${p eq currentPage ? 'bg-primary border-primary text-white' : 'bg-black border-secondary text-light'}"
                                               href="${pageContext.request.contextPath}/admin/teachers?page=${p}&search=<c:out value='${searchQuery}'/>">
                                                <c:out value="${p}"/>
                                            </a>
                                        </li>
                                    </c:forEach>
                                    <li class="page-item ${currentPage ge totalPages ? 'disabled' : ''}">
                                        <a class="page-link bg-black border-secondary text-light" href="${pageContext.request.contextPath}/admin/teachers?page=${currentPage + 1}&search=<c:out value='${searchQuery}'/>">
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
         ADD TEACHER MODAL
    ═══════════════════════════════════════ -->
    <div class="modal fade" id="addTeacherModal" tabindex="-1" aria-labelledby="addTeacherModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered modal-lg">
            <div class="modal-content bg-dark border border-secondary text-white">
                <form action="${pageContext.request.contextPath}/admin/teachers" method="post" id="addTeacherForm">
                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                    <input type="hidden" name="action" value="add">

                    <div class="modal-header border-bottom border-secondary">
                        <h5 class="modal-title" id="addTeacherModalLabel">
                            <i class="bi bi-person-plus-fill text-purple me-2" style="color: #a371f7;"></i>Register Faculty Member
                        </h5>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>

                    <div class="modal-body p-4">
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label for="addTeachName" class="form-label text-secondary small fw-semibold">Full Legal Name <span class="text-danger">*</span></label>
                                <input type="text" class="form-control bg-black text-white border-secondary" id="addTeachName" name="fullName" required placeholder="e.g. Dr. Sarah Jenkins">
                            </div>
                            <div class="col-md-6">
                                <label for="addTeachEmail" class="form-label text-secondary small fw-semibold">Email Address (Login ID) <span class="text-danger">*</span></label>
                                <input type="email" class="form-control bg-black text-white border-secondary" id="addTeachEmail" name="email" required placeholder="teacher@university.edu">
                            </div>
                            <div class="col-md-6">
                                <label for="addTeachPassword" class="form-label text-secondary small fw-semibold">Initial Password <span class="text-danger">*</span></label>
                                <input type="password" class="form-control bg-black text-white border-secondary" id="addTeachPassword" name="password" required minlength="6" placeholder="Min. 6 characters">
                            </div>
                            <div class="col-md-6">
                                <label for="addTeachEmpId" class="form-label text-secondary small fw-semibold">Employee ID (Optional)</label>
                                <input type="text" class="form-control bg-black text-white border-secondary" id="addTeachEmpId" name="employeeId" placeholder="Auto-generated if blank">
                            </div>
                            <div class="col-md-6">
                                <label for="addTeachDept" class="form-label text-secondary small fw-semibold">Academic Department</label>
                                <input type="text" class="form-control bg-black text-white border-secondary" id="addTeachDept" name="department" placeholder="e.g. Computer Science">
                            </div>
                            <div class="col-md-6">
                                <label for="addTeachQual" class="form-label text-secondary small fw-semibold">Qualification / Title</label>
                                <input type="text" class="form-control bg-black text-white border-secondary" id="addTeachQual" name="qualification" placeholder="e.g. Ph.D., Associate Professor">
                            </div>
                            <div class="col-12">
                                <label for="addTeachPhone" class="form-label text-secondary small fw-semibold">Phone Number</label>
                                <input type="tel" class="form-control bg-black text-white border-secondary" id="addTeachPhone" name="phone" placeholder="+1 (555) 123-4567">
                            </div>
                            <div class="col-12 mt-3">
                                <label class="form-label text-secondary small fw-semibold">Assign Curriculum Subjects</label>
                                <div class="p-3 rounded bg-black border border-secondary" style="max-height: 140px; overflow-y: auto;">
                                    <c:choose>
                                        <c:when test="${not empty allSubjects}">
                                            <div class="row g-2">
                                                <c:forEach var="sub" items="${allSubjects}">
                                                    <div class="col-sm-6">
                                                        <div class="form-check">
                                                            <input class="form-check-input" type="checkbox" name="subjectIds" value="${sub.id}" id="add_sub_${sub.id}">
                                                            <label class="form-check-label text-light small" for="add_sub_${sub.id}">
                                                                <strong class="text-info">${sub.code}</strong> - ${sub.name}
                                                            </label>
                                                        </div>
                                                    </div>
                                                </c:forEach>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="text-muted small">No subjects configured yet. You can assign subjects later.</div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="modal-footer border-top border-secondary">
                        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-primary btn-sm px-3" style="background-color: #8957e5; border-color: #8957e5;">Register Faculty</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- ═══════════════════════════════════════
         EDIT TEACHER MODAL
    ═══════════════════════════════════════ -->
    <div class="modal fade" id="editTeacherModal" tabindex="-1" aria-labelledby="editTeacherModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered modal-lg">
            <div class="modal-content bg-dark border border-secondary text-white">
                <form action="${pageContext.request.contextPath}/admin/teachers" method="post" id="editTeacherForm">
                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                    <input type="hidden" name="action" value="edit">
                    <input type="hidden" name="id" id="editTeacherId">

                    <div class="modal-header border-bottom border-secondary">
                        <h5 class="modal-title" id="editTeacherModalLabel">
                            <i class="bi bi-pencil-square text-warning me-2"></i>Edit Faculty Profile
                        </h5>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>

                    <div class="modal-body p-4">
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label for="editTeachName" class="form-label text-secondary small fw-semibold">Full Legal Name <span class="text-danger">*</span></label>
                                <input type="text" class="form-control bg-black text-white border-secondary" id="editTeachName" name="fullName" required>
                            </div>
                            <div class="col-md-6">
                                <label for="editTeachEmail" class="form-label text-secondary small fw-semibold">Email Address <span class="text-danger">*</span></label>
                                <input type="email" class="form-control bg-black text-white border-secondary" id="editTeachEmail" name="email" required>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-secondary small fw-semibold">Employee ID</label>
                                <input type="text" class="form-control bg-dark text-muted border-secondary font-monospace" id="editTeachEmpId" name="employeeId" readonly>
                            </div>
                            <div class="col-md-6">
                                <label for="editTeachPhone" class="form-label text-secondary small fw-semibold">Phone Number</label>
                                <input type="tel" class="form-control bg-black text-white border-secondary" id="editTeachPhone" name="phone">
                            </div>
                            <div class="col-md-6">
                                <label for="editTeachDept" class="form-label text-secondary small fw-semibold">Department</label>
                                <input type="text" class="form-control bg-black text-white border-secondary" id="editTeachDept" name="department">
                            </div>
                            <div class="col-md-6">
                                <label for="editTeachQual" class="form-label text-secondary small fw-semibold">Qualification / Title</label>
                                <input type="text" class="form-control bg-black text-white border-secondary" id="editTeachQual" name="qualification">
                            </div>
                            <div class="col-12 mt-3">
                                <label class="form-label text-secondary small fw-semibold">Assigned Subjects</label>
                                <div class="p-3 rounded bg-black border border-secondary" style="max-height: 140px; overflow-y: auto;">
                                    <div class="row g-2">
                                        <c:forEach var="sub" items="${allSubjects}">
                                            <div class="col-sm-6">
                                                <div class="form-check">
                                                    <input class="form-check-input edit-sub-checkbox" type="checkbox" name="subjectIds" value="${sub.id}" id="edit_sub_${sub.id}">
                                                    <label class="form-check-label text-light small" for="edit_sub_${sub.id}">
                                                        <strong class="text-info">${sub.code}</strong> - ${sub.name}
                                                    </label>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </div>
                            </div>
                            <div class="col-12">
                                <div class="form-check form-switch mt-2">
                                    <input class="form-check-input" type="checkbox" role="switch" id="editTeachActive" name="active" value="true">
                                    <label class="form-check-label text-light small fw-semibold" for="editTeachActive">Account Active Status</label>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="modal-footer border-top border-secondary">
                        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-warning btn-sm px-3">Update Faculty</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- ═══════════════════════════════════════
         VIEW TEACHER DETAILS MODAL
    ═══════════════════════════════════════ -->
    <div class="modal fade" id="viewTeacherModal" tabindex="-1" aria-labelledby="viewTeacherModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content bg-dark border border-secondary text-white">
                <div class="modal-header border-bottom border-secondary">
                    <h5 class="modal-title" id="viewTeacherModalLabel">
                        <i class="bi bi-person-badge text-purple me-2" style="color: #a371f7;"></i>Faculty Profile Details
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body p-4">
                    <div class="text-center mb-4">
                        <div class="ep-admin-avatar mx-auto mb-2" id="viewTeachAvatar" style="width: 56px; height: 56px; font-size: 1.5rem; background: linear-gradient(135deg, #a371f7, #8957e5);">
                            T
                        </div>
                        <h4 class="h5 fw-bold text-white mb-0" id="viewTeachFullName">Faculty Name</h4>
                        <span class="font-monospace text-secondary small" id="viewTeachEmail">teacher@university.edu</span>
                    </div>

                    <ul class="list-unstyled d-flex flex-column gap-2 mb-0">
                        <li class="d-flex justify-content-between p-2 rounded bg-black border border-secondary">
                            <span class="text-secondary small">Employee ID</span>
                            <span class="font-monospace text-light small fw-bold" id="viewTeachEmpId">—</span>
                        </li>
                        <li class="d-flex justify-content-between p-2 rounded bg-black border border-secondary">
                            <span class="text-secondary small">Department</span>
                            <span class="text-light small" id="viewTeachDept">—</span>
                        </li>
                        <li class="d-flex justify-content-between p-2 rounded bg-black border border-secondary">
                            <span class="text-secondary small">Qualification</span>
                            <span class="text-light small" id="viewTeachQual">—</span>
                        </li>
                        <li class="d-flex justify-content-between p-2 rounded bg-black border border-secondary">
                            <span class="text-secondary small">Contact Phone</span>
                            <span class="font-monospace text-light small" id="viewTeachPhone">—</span>
                        </li>
                        <li class="d-flex justify-content-between p-2 rounded bg-black border border-secondary">
                            <span class="text-secondary small">Assigned Subjects</span>
                            <span class="text-info small fw-medium" id="viewTeachSubjects">—</span>
                        </li>
                        <li class="d-flex justify-content-between p-2 rounded bg-black border border-secondary">
                            <span class="text-secondary small">Account Status</span>
                            <span id="viewTeachStatusBadge"></span>
                        </li>
                        <li class="d-flex justify-content-between p-2 rounded bg-black border border-secondary">
                            <span class="text-secondary small">Date Registered</span>
                            <span class="text-secondary small" id="viewTeachCreatedAt">—</span>
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

            var urlParams = new URLSearchParams(window.location.search);
            if (urlParams.get('openAdd') === 'true') {
                var addModal = new bootstrap.Modal(document.getElementById('addTeacherModal'));
                addModal.show();
            }

            // Edit Modal Population
            var editButtons = document.querySelectorAll('.edit-teacher-btn');
            editButtons.forEach(function (btn) {
                btn.addEventListener('click', function () {
                    document.getElementById('editTeacherId').value = this.getAttribute('data-id') || '';
                    document.getElementById('editTeachName').value = this.getAttribute('data-name') || '';
                    document.getElementById('editTeachEmail').value = this.getAttribute('data-email') || '';
                    document.getElementById('editTeachEmpId').value = this.getAttribute('data-empid') || '';
                    document.getElementById('editTeachPhone').value = this.getAttribute('data-phone') || '';
                    document.getElementById('editTeachDept').value = this.getAttribute('data-dept') || '';
                    document.getElementById('editTeachQual').value = this.getAttribute('data-qual') || '';
                    document.getElementById('editTeachActive').checked = (this.getAttribute('data-active') === 'true');

                    var assignedIds = (this.getAttribute('data-subids') || '').split(',');
                    document.querySelectorAll('.edit-sub-checkbox').forEach(function (cb) {
                        cb.checked = assignedIds.includes(cb.value);
                    });
                });
            });

            // View Modal Population
            var viewButtons = document.querySelectorAll('.view-teacher-btn');
            viewButtons.forEach(function (btn) {
                btn.addEventListener('click', function () {
                    var name = this.getAttribute('data-name') || 'Teacher';
                    document.getElementById('viewTeachFullName').textContent = name;
                    document.getElementById('viewTeachEmail').textContent = this.getAttribute('data-email') || '';
                    document.getElementById('viewTeachEmpId').textContent = this.getAttribute('data-empid') || '—';
                    document.getElementById('viewTeachPhone').textContent = this.getAttribute('data-phone') || '—';
                    document.getElementById('viewTeachDept').textContent = this.getAttribute('data-dept') || '—';
                    document.getElementById('viewTeachQual').textContent = this.getAttribute('data-qual') || '—';
                    document.getElementById('viewTeachSubjects').textContent = this.getAttribute('data-subjects') || 'None';
                    document.getElementById('viewTeachCreatedAt').textContent = this.getAttribute('data-created') || '—';
                    document.getElementById('viewTeachAvatar').textContent = name.charAt(0).toUpperCase();

                    var isActive = (this.getAttribute('data-active') === 'true');
                    var statusBadge = document.getElementById('viewTeachStatusBadge');
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
