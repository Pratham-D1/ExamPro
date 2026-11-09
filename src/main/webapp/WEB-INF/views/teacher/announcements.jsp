<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Course Notices &amp; Announcements | ExamPro Faculty</title>

    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">

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
            <jsp:param name="active" value="announcements"/>
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
                            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/teacher/dashboard">Teacher Portal</a></li>
                            <li class="breadcrumb-item active" aria-current="page">Course Notices</li>
                        </ol>
                    </nav>
                </div>
                <div class="ep-topbar-actions d-flex align-items-center gap-2">
                    <jsp:include page="/WEB-INF/views/common/notif_bell.jsp"/>
                    <div class="d-flex align-items-center gap-2 ps-2 border-start border-secondary">
                        <span class="badge" style="background: rgba(163, 113, 247, 0.2); color: #a371f7;">TEACHER</span>
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

                <!-- Header Toolbar -->
                <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
                    <div>
                        <h1 class="h4 fw-bold text-white mb-1">
                            <i class="bi bi-megaphone-fill text-warning me-2"></i>Course Notices &amp; Student Communication
                        </h1>
                        <p class="text-secondary small mb-0">
                            Broadcast schedule updates, test instructions, and curriculum notices to students in your assigned subjects.
                        </p>
                    </div>
                    <button class="btn btn-primary btn-sm d-flex align-items-center gap-2" data-bs-toggle="modal" data-bs-target="#createTeacherAnnModal">
                        <i class="bi bi-plus-circle-fill"></i>
                        <span>New Course Notice</span>
                    </button>
                </div>

                <!-- Filter Form -->
                <div class="card bg-dark border-secondary shadow-sm mb-4">
                    <div class="card-body p-3">
                        <form action="${pageContext.request.contextPath}/teacher/announcements" method="get" class="row g-2 align-items-center">
                            <div class="col-md-5">
                                <div class="input-group input-group-sm">
                                    <span class="input-group-text bg-dark border-secondary text-secondary"><i class="bi bi-search"></i></span>
                                    <input type="text" name="search" class="form-control bg-dark text-white border-secondary"
                                           placeholder="Search by title or notice text..." value="${search}">
                                </div>
                            </div>
                            <div class="col-md-4">
                                <select name="subjectId" class="form-select form-select-sm bg-dark text-white border-secondary">
                                    <option value="">All My Assigned Subjects</option>
                                    <c:forEach var="sub" items="${assignedSubjects}">
                                        <option value="${sub.id}" ${selectedSubjectId == sub.id ? 'selected' : ''}>
                                            <c:out value="${sub.code}"/> - <c:out value="${sub.name}"/>
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-3 d-flex gap-2">
                                <button type="submit" class="btn btn-outline-info btn-sm flex-grow-1">Filter</button>
                                <a href="${pageContext.request.contextPath}/teacher/announcements" class="btn btn-outline-secondary btn-sm" title="Reset Filters">
                                    <i class="bi bi-x-circle"></i>
                                </a>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- Notices Table -->
                <div class="card bg-dark border-secondary shadow-sm">
                    <div class="card-body p-0">
                        <c:choose>
                            <c:when test="${not empty announcements}">
                                <div class="table-responsive">
                                    <table class="table table-dark table-hover align-middle mb-0">
                                        <thead>
                                            <tr class="text-secondary small">
                                                <th class="ps-3 py-3">Notice</th>
                                                <th>Subject</th>
                                                <th>Priority</th>
                                                <th>Active Status</th>
                                                <th>Publication</th>
                                                <th>Created</th>
                                                <th class="text-end pe-3">Actions</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="a" items="${announcements}">
                                                <tr>
                                                    <td class="ps-3" style="max-width: 320px;">
                                                        <div class="fw-bold text-white text-truncate"><c:out value="${a.title}"/></div>
                                                        <div class="text-secondary small text-truncate"><c:out value="${a.content}"/></div>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${not empty a.subjectCode}">
                                                                <span class="badge bg-secondary"><c:out value="${a.subjectCode}"/></span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge bg-dark border border-secondary text-secondary">General</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <span class="badge ${a.priorityBadgeClass} px-2 py-1"><c:out value="${a.priority}"/></span>
                                                    </td>
                                                    <td class="small">
                                                        <c:choose>
                                                            <c:when test="${a.active}">
                                                                <span class="text-success"><i class="bi bi-broadcast me-1"></i>Active</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="text-secondary fst-italic">Inactive / Scheduled</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <form action="${pageContext.request.contextPath}/teacher/announcements" method="post" class="m-0">
                                                            <input type="hidden" name="action" value="togglePublish">
                                                            <input type="hidden" name="id" value="${a.id}">
                                                            <input type="hidden" name="publish" value="${!a.published}">
                                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                            <button type="submit" class="btn btn-sm ${a.published ? 'btn-outline-success' : 'btn-outline-warning'}" style="font-size: 0.78rem;">
                                                                <i class="bi ${a.published ? 'bi-check2' : 'bi-pause'} me-1"></i>
                                                                ${a.published ? 'Published' : 'Hidden'}
                                                            </button>
                                                        </form>
                                                    </td>
                                                    <td class="small text-secondary">
                                                        <c:out value="${a.formattedCreatedAt}"/>
                                                    </td>
                                                    <td class="text-end pe-3">
                                                        <div class="btn-group btn-group-sm">
                                                            <button type="button" class="btn btn-outline-info edit-tann-btn"
                                                                    data-id="${a.id}"
                                                                    data-title="<c:out value='${a.title}'/>"
                                                                    data-content="<c:out value='${a.content}'/>"
                                                                    data-priority="${a.priority}"
                                                                    data-subject="${a.subjectId != null ? a.subjectId : ''}"
                                                                    data-start="${a.inputStartDate}"
                                                                    data-expiry="${a.inputExpiryDate}"
                                                                    data-published="${a.published}"
                                                                    title="Edit Notice">
                                                                <i class="bi bi-pencil-square"></i>
                                                            </button>
                                                            <form action="${pageContext.request.contextPath}/teacher/announcements" method="post" class="d-inline"
                                                                  onsubmit="return confirm('Permanently delete this course notice?');">
                                                                <input type="hidden" name="action" value="delete">
                                                                <input type="hidden" name="id" value="${a.id}">
                                                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                                <button type="submit" class="btn btn-outline-danger" title="Delete Notice">
                                                                    <i class="bi bi-trash3"></i>
                                                                </button>
                                                            </form>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>

                                <!-- Pagination -->
                                <c:if test="${totalPages > 1}">
                                    <div class="card-footer bg-transparent border-secondary py-3">
                                        <ul class="pagination pagination-sm justify-content-center m-0">
                                            <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                                <a class="page-link bg-dark text-white border-secondary"
                                                   href="${pageContext.request.contextPath}/teacher/announcements?page=${currentPage - 1}&subjectId=${selectedSubjectId}&search=${search}">
                                                    Previous
                                                </a>
                                            </li>
                                            <c:forEach var="p" begin="1" end="${totalPages}">
                                                <li class="page-item ${currentPage == p ? 'active' : ''}">
                                                    <a class="page-link ${currentPage == p ? 'bg-primary border-primary text-white' : 'bg-dark text-white border-secondary'}"
                                                       href="${pageContext.request.contextPath}/teacher/announcements?page=${p}&subjectId=${selectedSubjectId}&search=${search}">
                                                        ${p}
                                                    </a>
                                                </li>
                                            </c:forEach>
                                            <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                                <a class="page-link bg-dark text-white border-secondary"
                                                   href="${pageContext.request.contextPath}/teacher/announcements?page=${currentPage + 1}&subjectId=${selectedSubjectId}&search=${search}">
                                                    Next
                                                </a>
                                            </li>
                                        </ul>
                                    </div>
                                </c:if>
                            </c:when>
                            <c:otherwise>
                                <div class="text-center py-5">
                                    <i class="bi bi-megaphone fs-1 text-secondary opacity-50 mb-3 d-block"></i>
                                    <h5 class="text-white fw-bold">No Course Notices</h5>
                                    <p class="text-secondary small mb-0">
                                        You have not posted any course announcements matching the selected filter.
                                    </p>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

            </div>
        </main>
    </div>

    <!-- ═══════════════════════════════════════════════════════════════
         CREATE COURSE NOTICE MODAL
    ═══════════════════════════════════════════════════════════════ -->
    <div class="modal fade" id="createTeacherAnnModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered modal-lg">
            <div class="modal-content bg-dark border-secondary text-white">
                <form action="${pageContext.request.contextPath}/teacher/announcements" method="post">
                    <input type="hidden" name="action" value="create">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <div class="modal-header border-secondary">
                        <h5 class="modal-title fw-bold"><i class="bi bi-megaphone-fill text-warning me-2"></i>Post Course Notice</h5>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <div class="mb-3">
                            <label class="form-label small fw-semibold text-secondary">Notice Title <span class="text-danger">*</span></label>
                            <input type="text" name="title" class="form-control bg-dark text-white border-secondary" placeholder="e.g. Supplementary Notes for Data Structures Exam" required maxlength="200">
                        </div>
                        <div class="mb-3">
                            <label class="form-label small fw-semibold text-secondary">Notice Content <span class="text-danger">*</span></label>
                            <textarea name="content" class="form-control bg-dark text-white border-secondary" rows="4" placeholder="Course notice or instruction details..." required></textarea>
                        </div>
                        <div class="row g-3 mb-3">
                            <div class="col-md-6">
                                <label class="form-label small fw-semibold text-secondary">Associated Subject (Optional)</label>
                                <select name="subjectId" class="form-select bg-dark text-white border-secondary">
                                    <option value="">General (All My Students)</option>
                                    <c:forEach var="sub" items="${assignedSubjects}">
                                        <option value="${sub.id}"><c:out value="${sub.code}"/> - <c:out value="${sub.name}"/></option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label small fw-semibold text-secondary">Priority Level</label>
                                <select name="priority" class="form-select bg-dark text-white border-secondary">
                                    <option value="NORMAL">Normal</option>
                                    <option value="IMPORTANT">Important</option>
                                    <option value="URGENT">Urgent</option>
                                </select>
                            </div>
                        </div>
                        <div class="row g-3 mb-3">
                            <div class="col-md-6">
                                <label class="form-label small fw-semibold text-secondary">Start Date &amp; Time</label>
                                <input type="datetime-local" name="startDate" class="form-control bg-dark text-white border-secondary">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label small fw-semibold text-secondary">Expiry Date &amp; Time</label>
                                <input type="datetime-local" name="expiryDate" class="form-control bg-dark text-white border-secondary">
                            </div>
                        </div>
                        <div class="form-check form-switch mb-2">
                            <input class="form-check-input" type="checkbox" name="isPublished" value="true" id="tpubCheck" checked>
                            <label class="form-check-label small" for="tpubCheck">Publish notice immediately</label>
                        </div>
                        <div class="form-check form-switch">
                            <input class="form-check-input" type="checkbox" name="broadcastNotification" value="true" id="tbroadcastCheck" checked>
                            <label class="form-check-label small text-info fw-semibold" for="tbroadcastCheck">
                                <i class="bi bi-bell-fill me-1"></i> Send in-app notification to students
                            </label>
                        </div>
                    </div>
                    <div class="modal-footer border-secondary">
                        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-primary"><i class="bi bi-send-fill me-1"></i> Post Notice</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- ═══════════════════════════════════════════════════════════════
         EDIT COURSE NOTICE MODAL
    ═══════════════════════════════════════════════════════════════ -->
    <div class="modal fade" id="editTeacherAnnModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered modal-lg">
            <div class="modal-content bg-dark border-secondary text-white">
                <form action="${pageContext.request.contextPath}/teacher/announcements" method="post">
                    <input type="hidden" name="action" value="update">
                    <input type="hidden" name="id" id="editTAnnId">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <div class="modal-header border-secondary">
                        <h5 class="modal-title fw-bold"><i class="bi bi-pencil-square text-info me-2"></i>Edit Course Notice</h5>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <div class="mb-3">
                            <label class="form-label small fw-semibold text-secondary">Notice Title <span class="text-danger">*</span></label>
                            <input type="text" name="title" id="editTAnnTitle" class="form-control bg-dark text-white border-secondary" required maxlength="200">
                        </div>
                        <div class="mb-3">
                            <label class="form-label small fw-semibold text-secondary">Notice Content <span class="text-danger">*</span></label>
                            <textarea name="content" id="editTAnnContent" class="form-control bg-dark text-white border-secondary" rows="4" required></textarea>
                        </div>
                        <div class="row g-3 mb-3">
                            <div class="col-md-6">
                                <label class="form-label small fw-semibold text-secondary">Associated Subject</label>
                                <select name="subjectId" id="editTAnnSubject" class="form-select bg-dark text-white border-secondary">
                                    <option value="">General (All My Students)</option>
                                    <c:forEach var="sub" items="${assignedSubjects}">
                                        <option value="${sub.id}"><c:out value="${sub.code}"/> - <c:out value="${sub.name}"/></option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label small fw-semibold text-secondary">Priority Level</label>
                                <select name="priority" id="editTAnnPriority" class="form-select bg-dark text-white border-secondary">
                                    <option value="NORMAL">Normal</option>
                                    <option value="IMPORTANT">Important</option>
                                    <option value="URGENT">Urgent</option>
                                </select>
                            </div>
                        </div>
                        <div class="row g-3 mb-3">
                            <div class="col-md-6">
                                <label class="form-label small fw-semibold text-secondary">Start Date &amp; Time</label>
                                <input type="datetime-local" name="startDate" id="editTAnnStart" class="form-control bg-dark text-white border-secondary">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label small fw-semibold text-secondary">Expiry Date &amp; Time</label>
                                <input type="datetime-local" name="expiryDate" id="editTAnnExpiry" class="form-control bg-dark text-white border-secondary">
                            </div>
                        </div>
                        <div class="form-check form-switch">
                            <input class="form-check-input" type="checkbox" name="isPublished" value="true" id="editTAnnPublished">
                            <label class="form-check-label small" for="editTAnnPublished">Published</label>
                        </div>
                    </div>
                    <div class="modal-footer border-secondary">
                        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-info"><i class="bi bi-save me-1"></i> Save Changes</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Bootstrap 5 JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
            crossorigin="anonymous"></script>

    <script>
        document.addEventListener('DOMContentLoaded', function () {
            const editBtns = document.querySelectorAll('.edit-tann-btn');
            const editModal = new bootstrap.Modal(document.getElementById('editTeacherAnnModal'));

            editBtns.forEach(btn => {
                btn.addEventListener('click', function() {
                    document.getElementById('editTAnnId').value = this.dataset.id;
                    document.getElementById('editTAnnTitle').value = this.dataset.title;
                    document.getElementById('editTAnnContent').value = this.dataset.content;
                    document.getElementById('editTAnnPriority').value = this.dataset.priority;
                    document.getElementById('editTAnnSubject').value = this.dataset.subject || '';
                    document.getElementById('editTAnnStart').value = this.dataset.start || '';
                    document.getElementById('editTAnnExpiry').value = this.dataset.expiry || '';
                    document.getElementById('editTAnnPublished').checked = (this.dataset.published === 'true');
                    editModal.show();
                });
            });
        });
    </script>
</body>
</html>

