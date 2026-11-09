<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.exampro.model.User" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Subject Management | ExamPro Admin</title>

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
            <jsp:param name="active" value="subjects"/>
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
                            <li class="active" aria-current="page">Subjects</li>
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
                            <i class="bi bi-journal-bookmark-fill text-info me-2"></i>Subject &amp; Course Management
                        </h1>
                        <p class="text-secondary small mb-0">
                            Define course modules, manage academic credits, and organize examination curriculum mappings.
                        </p>
                    </div>
                    <div>
                        <button type="button" class="btn btn-info d-inline-flex align-items-center gap-2 px-3 py-2 text-dark fw-semibold" data-bs-toggle="modal" data-bs-target="#addSubjectModal">
                            <i class="bi bi-journal-plus"></i>
                            <span>Create New Subject</span>
                        </button>
                    </div>
                </div>

                <!-- Search Bar -->
                <div class="card bg-dark border-secondary mb-4">
                    <div class="card-body p-3">
                        <form action="${pageContext.request.contextPath}/admin/subjects" method="get" class="row g-2 align-items-center">
                            <div class="col-md-9 col-lg-10">
                                <div class="input-group">
                                    <span class="input-group-text bg-black border-secondary text-secondary">
                                        <i class="bi bi-search"></i>
                                    </span>
                                    <input type="text" name="search" class="form-control bg-black text-white border-secondary"
                                           placeholder="Search by subject code (e.g., CS101) or course title..."
                                           value="<c:out value='${searchQuery}'/>">
                                </div>
                            </div>
                            <div class="col-md-3 col-lg-2 d-flex gap-2">
                                <button type="submit" class="btn btn-primary flex-grow-1">Search</button>
                                <c:if test="${not empty searchQuery}">
                                    <a href="${pageContext.request.contextPath}/admin/subjects" class="btn btn-outline-secondary" title="Clear search">
                                        <i class="bi bi-x-lg"></i>
                                    </a>
                                </c:if>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- Subjects Table Card -->
                <div class="ep-card">
                    <div class="ep-card-header d-flex align-items-center justify-content-between">
                        <h2 class="ep-card-title m-0 h5">
                            <i class="bi bi-journal-text text-warning me-2"></i>Curriculum Catalog
                        </h2>
                        <span class="badge bg-dark border border-secondary text-secondary">
                            Total: <strong class="text-white">${fn:length(subjects)}</strong> Subjects
                        </span>
                    </div>

                    <div class="table-responsive">
                        <c:choose>
                            <c:when test="${not empty subjects}">
                                <table class="ep-table-custom" aria-label="Curriculum Catalog">
                                    <thead>
                                        <tr>
                                            <th scope="col">Code</th>
                                            <th scope="col">Subject Name</th>
                                            <th scope="col">Description</th>
                                            <th scope="col">Credits</th>
                                            <th scope="col">Faculty Assigned</th>
                                            <th scope="col">Linked Exams</th>
                                            <th scope="col">Status</th>
                                            <th scope="col" class="text-end">Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="s" items="${subjects}">
                                            <tr>
                                                <td>
                                                    <span class="badge bg-black border border-info text-info font-monospace px-2 py-1 fs-6">
                                                        <c:out value="${s.code}"/>
                                                    </span>
                                                </td>
                                                <td>
                                                    <span class="fw-semibold text-white fs-6">
                                                        <c:out value="${s.name}"/>
                                                    </span>
                                                </td>
                                                <td class="text-secondary small" style="max-width: 250px;">
                                                    <c:out value="${not empty s.description ? s.description : '—'}"/>
                                                </td>
                                                <td>
                                                    <span class="badge bg-dark border border-secondary text-light">
                                                        <c:out value="${s.credits}"/> Credits
                                                    </span>
                                                </td>
                                                <td>
                                                    <span class="badge bg-purple bg-opacity-10 text-purple border border-opacity-25" style="background: rgba(163, 113, 247, 0.15); color: #a371f7; border-color: rgba(163, 113, 247, 0.3) !important;">
                                                        <i class="bi bi-person-video3 me-1"></i><c:out value="${s.teacherCount}"/> Faculty
                                                    </span>
                                                </td>
                                                <td>
                                                    <span class="badge bg-warning bg-opacity-10 text-warning border border-warning border-opacity-25">
                                                        <i class="bi bi-file-earmark-text me-1"></i><c:out value="${s.examCount}"/> Exams
                                                    </span>
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
                                                <td class="text-end">
                                                    <div class="d-inline-flex gap-1">
                                                        <!-- Edit Button -->
                                                        <button type="button" class="btn btn-outline-warning btn-sm edit-subject-btn"
                                                                data-bs-toggle="modal" data-bs-target="#editSubjectModal"
                                                                data-id="<c:out value='${s.id}'/>"
                                                                data-code="<c:out value='${s.code}'/>"
                                                                data-name="<c:out value='${s.name}'/>"
                                                                data-desc="<c:out value='${s.description}'/>"
                                                                data-credits="<c:out value='${s.credits}'/>"
                                                                data-active="<c:out value='${s.active}'/>"
                                                                title="Edit Subject">
                                                            <i class="bi bi-pencil-square"></i>
                                                        </button>

                                                        <!-- Delete Button (triggers safety modal) -->
                                                        <button type="button" class="btn btn-outline-danger btn-sm delete-subject-btn"
                                                                data-bs-toggle="modal" data-bs-target="#deleteSubjectModal"
                                                                data-id="<c:out value='${s.id}'/>"
                                                                data-code="<c:out value='${s.code}'/>"
                                                                data-name="<c:out value='${s.name}'/>"
                                                                data-exams="<c:out value='${s.examCount}'/>"
                                                                title="Delete Subject">
                                                            <i class="bi bi-trash"></i>
                                                        </button>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </c:when>
                            <c:otherwise>
                                <div class="text-center py-5 text-muted">
                                    <i class="bi bi-journal-x display-4 d-block mb-3 opacity-50"></i>
                                    <h3 class="h5 text-white mb-1">No Subjects Registered</h3>
                                    <p class="small text-secondary mb-3">
                                        <c:choose>
                                            <c:when test="${not empty searchQuery}">
                                                No course modules match search: "<strong><c:out value="${searchQuery}"/></strong>".
                                            </c:when>
                                            <c:otherwise>
                                                Your curriculum catalog is empty. Create your first subject below.
                                            </c:otherwise>
                                        </c:choose>
                                    </p>
                                    <button type="button" class="btn btn-info btn-sm text-dark fw-semibold" data-bs-toggle="modal" data-bs-target="#addSubjectModal">
                                        <i class="bi bi-journal-plus me-1"></i>Create First Subject
                                    </button>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

            </main>
        </div>
    </div>

    <!-- ═══════════════════════════════════════
         ADD SUBJECT MODAL
    ═══════════════════════════════════════ -->
    <div class="modal fade" id="addSubjectModal" tabindex="-1" aria-labelledby="addSubjectModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content bg-dark border border-secondary text-white">
                <form action="${pageContext.request.contextPath}/admin/subjects" method="post" id="addSubjectForm">
                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                    <input type="hidden" name="action" value="add">

                    <div class="modal-header border-bottom border-secondary">
                        <h5 class="modal-title" id="addSubjectModalLabel">
                            <i class="bi bi-journal-plus text-info me-2"></i>Create New Subject
                        </h5>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>

                    <div class="modal-body p-4">
                        <div class="mb-3">
                            <label for="addCode" class="form-label text-secondary small fw-semibold">Subject Code <span class="text-danger">*</span></label>
                            <input type="text" class="form-control bg-black text-white border-secondary font-monospace" id="addCode" name="code" required placeholder="e.g. CS101, MATH201">
                        </div>
                        <div class="mb-3">
                            <label for="addName" class="form-label text-secondary small fw-semibold">Subject Name <span class="text-danger">*</span></label>
                            <input type="text" class="form-control bg-black text-white border-secondary" id="addName" name="name" required placeholder="e.g. Data Structures and Algorithms">
                        </div>
                        <div class="mb-3">
                            <label for="addCredits" class="form-label text-secondary small fw-semibold">Academic Credits</label>
                            <input type="number" class="form-control bg-black text-white border-secondary" id="addCredits" name="credits" value="3" min="1" max="10">
                        </div>
                        <div class="mb-3">
                            <label for="addDesc" class="form-label text-secondary small fw-semibold">Description</label>
                            <textarea class="form-control bg-black text-white border-secondary" id="addDesc" name="description" rows="3" placeholder="Course overview and syllabus details..."></textarea>
                        </div>
                        <div class="form-check form-switch mt-2">
                            <input class="form-check-input" type="checkbox" role="switch" id="addActive" name="active" value="true" checked>
                            <label class="form-check-label text-light small fw-semibold" for="addActive">Subject Active</label>
                        </div>
                    </div>

                    <div class="modal-footer border-top border-secondary">
                        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-info btn-sm text-dark fw-semibold px-3">Create Subject</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- ═══════════════════════════════════════
         EDIT SUBJECT MODAL
    ═══════════════════════════════════════ -->
    <div class="modal fade" id="editSubjectModal" tabindex="-1" aria-labelledby="editSubjectModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content bg-dark border border-secondary text-white">
                <form action="${pageContext.request.contextPath}/admin/subjects" method="post" id="editSubjectForm">
                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                    <input type="hidden" name="action" value="edit">
                    <input type="hidden" name="id" id="editSubjectId">

                    <div class="modal-header border-bottom border-secondary">
                        <h5 class="modal-title" id="editSubjectModalLabel">
                            <i class="bi bi-pencil-square text-warning me-2"></i>Edit Subject Details
                        </h5>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>

                    <div class="modal-body p-4">
                        <div class="mb-3">
                            <label for="editCode" class="form-label text-secondary small fw-semibold">Subject Code <span class="text-danger">*</span></label>
                            <input type="text" class="form-control bg-black text-white border-secondary font-monospace" id="editCode" name="code" required>
                        </div>
                        <div class="mb-3">
                            <label for="editName" class="form-label text-secondary small fw-semibold">Subject Name <span class="text-danger">*</span></label>
                            <input type="text" class="form-control bg-black text-white border-secondary" id="editName" name="name" required>
                        </div>
                        <div class="mb-3">
                            <label for="editCredits" class="form-label text-secondary small fw-semibold">Academic Credits</label>
                            <input type="number" class="form-control bg-black text-white border-secondary" id="editCredits" name="credits" min="1" max="10">
                        </div>
                        <div class="mb-3">
                            <label for="editDesc" class="form-label text-secondary small fw-semibold">Description</label>
                            <textarea class="form-control bg-black text-white border-secondary" id="editDesc" name="description" rows="3"></textarea>
                        </div>
                        <div class="form-check form-switch mt-2">
                            <input class="form-check-input" type="checkbox" role="switch" id="editActive" name="active" value="true">
                            <label class="form-check-label text-light small fw-semibold" for="editActive">Subject Active</label>
                        </div>
                    </div>

                    <div class="modal-footer border-top border-secondary">
                        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-warning btn-sm px-3">Update Subject</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- ═══════════════════════════════════════
         DELETE SUBJECT SAFETY CONFIRMATION MODAL
    ═══════════════════════════════════════ -->
    <div class="modal fade" id="deleteSubjectModal" tabindex="-1" aria-labelledby="deleteSubjectModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content bg-dark border border-secondary text-white">
                <form action="${pageContext.request.contextPath}/admin/subjects" method="post" id="deleteSubjectForm">
                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="id" id="deleteSubjectId">

                    <div class="modal-header border-bottom border-secondary">
                        <h5 class="modal-title text-danger" id="deleteSubjectModalLabel">
                            <i class="bi bi-exclamation-triangle-fill me-2"></i>Delete Subject
                        </h5>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>

                    <div class="modal-body p-4">
                        <div class="text-center mb-3">
                            <div class="p-3 d-inline-block rounded-circle bg-danger bg-opacity-10 text-danger mb-2">
                                <i class="bi bi-trash display-6"></i>
                            </div>
                            <h5 class="fw-bold text-white" id="deleteSubjectTitle">Delete Subject</h5>
                        </div>

                        <div id="deleteWarningExams" class="alert alert-danger d-none small">
                            <i class="bi bi-slash-circle me-1"></i>
                            <strong>Deletion Protected:</strong> This subject is referenced by <span id="deleteExamCount">0</span> existing examination(s).
                            The system will refuse deletion to protect examination historical records.
                        </div>

                        <p class="text-secondary small mb-0">
                            Are you sure you want to permanently remove this subject from the system?
                            This action cannot be undone.
                        </p>
                    </div>

                    <div class="modal-footer border-top border-secondary">
                        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-danger btn-sm px-3" id="deleteSubmitBtn">Confirm Deletion</button>
                    </div>
                </form>
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
                var addModal = new bootstrap.Modal(document.getElementById('addSubjectModal'));
                addModal.show();
            }

            // Edit Modal Population
            var editButtons = document.querySelectorAll('.edit-subject-btn');
            editButtons.forEach(function (btn) {
                btn.addEventListener('click', function () {
                    document.getElementById('editSubjectId').value = this.getAttribute('data-id') || '';
                    document.getElementById('editCode').value = this.getAttribute('data-code') || '';
                    document.getElementById('editName').value = this.getAttribute('data-name') || '';
                    document.getElementById('editCredits').value = this.getAttribute('data-credits') || '3';
                    document.getElementById('editDesc').value = this.getAttribute('data-desc') || '';
                    document.getElementById('editActive').checked = (this.getAttribute('data-active') === 'true');
                });
            });

            // Delete Modal Population & Protection logic
            var deleteButtons = document.querySelectorAll('.delete-subject-btn');
            deleteButtons.forEach(function (btn) {
                btn.addEventListener('click', function () {
                    var id = this.getAttribute('data-id') || '';
                    var code = this.getAttribute('data-code') || '';
                    var name = this.getAttribute('data-name') || '';
                    var examCount = parseInt(this.getAttribute('data-exams') || '0', 10);

                    document.getElementById('deleteSubjectId').value = id;
                    document.getElementById('deleteSubjectTitle').textContent = 'Delete [' + code + '] ' + name + '?';

                    var warningBox = document.getElementById('deleteWarningExams');
                    var submitBtn = document.getElementById('deleteSubmitBtn');

                    if (examCount > 0) {
                        warningBox.classList.remove('d-none');
                        document.getElementById('deleteExamCount').textContent = examCount;
                        submitBtn.disabled = true;
                        submitBtn.title = 'Cannot delete subject with linked examinations';
                    } else {
                        warningBox.classList.add('d-none');
                        submitBtn.disabled = false;
                        submitBtn.title = '';
                    }
                });
            });
        });
    </script>
</body>
</html>
