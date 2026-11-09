<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Announcements &amp; Communications | ExamPro Admin</title>

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

        <!-- Mobile Backdrop -->
        <div class="ep-sidebar-backdrop" id="sidebarBackdrop" aria-hidden="true"></div>

        <!-- Admin Sidebar Navigation -->
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
                            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/admin/dashboard">Admin Portal</a></li>
                            <li class="breadcrumb-item active" aria-current="page">Announcements</li>
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

            <!-- Announcements Console Body -->
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

                <!-- Header & Quick Actions -->
                <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
                    <div>
                        <h1 class="h4 fw-bold text-white mb-1">
                            <i class="bi bi-megaphone-fill text-warning me-2"></i>Announcement &amp; Broadcast Center
                        </h1>
                        <p class="text-secondary small mb-0">
                            Broadcast institutional updates, schedule notices, and system-wide announcements to candidates and faculty.
                        </p>
                    </div>
                    <button class="btn btn-primary btn-sm d-flex align-items-center gap-2" data-bs-toggle="modal" data-bs-target="#createAnnouncementModal">
                        <i class="bi bi-plus-circle-fill"></i>
                        <span>New Announcement</span>
                    </button>
                </div>

                <!-- KPI Statistics Cards -->
                <div class="row g-3 mb-4">
                    <div class="col-sm-6 col-xl-3">
                        <div class="card bg-dark border-secondary shadow-sm h-100">
                            <div class="card-body d-flex align-items-center justify-content-between">
                                <div>
                                    <span class="text-secondary small text-uppercase fw-semibold">Total Notices</span>
                                    <h3 class="text-white fw-bold mb-0 mt-1"><c:out value="${stats.totalAnnouncements}"/></h3>
                                </div>
                                <div class="rounded p-3 bg-primary bg-opacity-10 text-primary">
                                    <i class="bi bi-megaphone fs-4"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-6 col-xl-3">
                        <div class="card bg-dark border-secondary shadow-sm h-100">
                            <div class="card-body d-flex align-items-center justify-content-between">
                                <div>
                                    <span class="text-secondary small text-uppercase fw-semibold">Published</span>
                                    <h3 class="text-white fw-bold mb-0 mt-1"><c:out value="${stats.publishedAnnouncements}"/></h3>
                                </div>
                                <div class="rounded p-3 bg-success bg-opacity-10 text-success">
                                    <i class="bi bi-check2-circle fs-4"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-6 col-xl-3">
                        <div class="card bg-dark border-secondary shadow-sm h-100">
                            <div class="card-body d-flex align-items-center justify-content-between">
                                <div>
                                    <span class="text-secondary small text-uppercase fw-semibold">Active &bull; Live</span>
                                    <h3 class="text-white fw-bold mb-0 mt-1"><c:out value="${stats.activeAnnouncements}"/></h3>
                                </div>
                                <div class="rounded p-3 bg-warning bg-opacity-10 text-warning">
                                    <i class="bi bi-broadcast fs-4"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-6 col-xl-3">
                        <div class="card bg-dark border-secondary shadow-sm h-100">
                            <div class="card-body d-flex align-items-center justify-content-between">
                                <div>
                                    <span class="text-secondary small text-uppercase fw-semibold">In-App Alerts Sent</span>
                                    <h3 class="text-white fw-bold mb-0 mt-1"><c:out value="${stats.totalNotificationsSent}"/></h3>
                                </div>
                                <div class="rounded p-3 bg-info bg-opacity-10 text-info">
                                    <i class="bi bi-bell-fill fs-4"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Multi-Criteria Filter Bar -->
                <div class="card bg-dark border-secondary shadow-sm mb-4">
                    <div class="card-body p-3">
                        <form action="${pageContext.request.contextPath}/admin/announcements" method="get" class="row g-2 align-items-center">
                            <div class="col-md-4">
                                <div class="input-group input-group-sm">
                                    <span class="input-group-text bg-dark border-secondary text-secondary"><i class="bi bi-search"></i></span>
                                    <input type="text" name="search" class="form-control bg-dark text-white border-secondary"
                                           placeholder="Search by title, message, or author..." value="${search}">
                                </div>
                            </div>
                            <div class="col-6 col-md-2">
                                <select name="priority" class="form-select form-select-sm bg-dark text-white border-secondary">
                                    <option value="ALL" ${priority == 'ALL' ? 'selected' : ''}>All Priorities</option>
                                    <option value="NORMAL" ${priority == 'NORMAL' ? 'selected' : ''}>Normal</option>
                                    <option value="IMPORTANT" ${priority == 'IMPORTANT' ? 'selected' : ''}>Important</option>
                                    <option value="URGENT" ${priority == 'URGENT' ? 'selected' : ''}>Urgent</option>
                                </select>
                            </div>
                            <div class="col-6 col-md-2">
                                <select name="targetRole" class="form-select form-select-sm bg-dark text-white border-secondary">
                                    <option value="ALL" ${targetRole == 'ALL' ? 'selected' : ''}>Target: All Users</option>
                                    <option value="STUDENT" ${targetRole == 'STUDENT' ? 'selected' : ''}>Students Only</option>
                                    <option value="TEACHER" ${targetRole == 'TEACHER' ? 'selected' : ''}>Teachers Only</option>
                                    <option value="ADMIN" ${targetRole == 'ADMIN' ? 'selected' : ''}>Admins Only</option>
                                </select>
                            </div>
                            <div class="col-6 col-md-2">
                                <select name="publishStatus" class="form-select form-select-sm bg-dark text-white border-secondary">
                                    <option value="ALL" ${publishStatus == 'ALL' ? 'selected' : ''}>All Statuses</option>
                                    <option value="PUBLISHED" ${publishStatus == 'PUBLISHED' ? 'selected' : ''}>Published</option>
                                    <option value="UNPUBLISHED" ${publishStatus == 'UNPUBLISHED' ? 'selected' : ''}>Unpublished</option>
                                </select>
                            </div>
                            <div class="col-6 col-md-2 d-flex gap-2">
                                <button type="submit" class="btn btn-outline-info btn-sm flex-grow-1">Filter</button>
                                <a href="${pageContext.request.contextPath}/admin/announcements" class="btn btn-outline-secondary btn-sm" title="Reset Filters">
                                    <i class="bi bi-x-circle"></i>
                                </a>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- Announcements Roster Table -->
                <div class="card bg-dark border-secondary shadow-sm">
                    <div class="card-body p-0">
                        <c:choose>
                            <c:when test="${not empty announcements}">
                                <div class="table-responsive">
                                    <table class="table table-dark table-hover align-middle mb-0">
                                        <thead>
                                            <tr class="text-secondary small">
                                                <th class="ps-3 py-3">Announcement</th>
                                                <th>Audience</th>
                                                <th>Priority</th>
                                                <th>Validity Window</th>
                                                <th>Publication</th>
                                                <th>Author</th>
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
                                                        <span class="badge bg-secondary"><c:out value="${a.targetRole}"/></span>
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
                                                        <div class="text-secondary" style="font-size: 0.75rem;">
                                                            <c:out value="${a.formattedStartDate}"/> &rarr; <c:out value="${a.formattedExpiryDate}"/>
                                                        </div>
                                                    </td>
                                                    <td>
                                                        <form action="${pageContext.request.contextPath}/admin/announcements" method="post" class="m-0">
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
                                                    <td class="small">
                                                        <div class="text-white"><c:out value="${a.creatorName}"/></div>
                                                        <div class="text-secondary" style="font-size: 0.75rem;"><c:out value="${a.formattedCreatedAt}"/></div>
                                                    </td>
                                                    <td class="text-end pe-3">
                                                        <div class="btn-group btn-group-sm">
                                                            <button type="button" class="btn btn-outline-info edit-ann-btn"
                                                                    data-id="${a.id}"
                                                                    data-title="<c:out value='${a.title}'/>"
                                                                    data-content="<c:out value='${a.content}'/>"
                                                                    data-priority="${a.priority}"
                                                                    data-role="${a.targetRole}"
                                                                    data-start="${a.inputStartDate}"
                                                                    data-expiry="${a.inputExpiryDate}"
                                                                    data-published="${a.published}"
                                                                    title="Edit Announcement">
                                                                <i class="bi bi-pencil-square"></i>
                                                            </button>
                                                            <form action="${pageContext.request.contextPath}/admin/announcements" method="post" class="d-inline"
                                                                  onsubmit="return confirm('Permanently delete this announcement?');">
                                                                <input type="hidden" name="action" value="delete">
                                                                <input type="hidden" name="id" value="${a.id}">
                                                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                                <button type="submit" class="btn btn-outline-danger" title="Delete Announcement">
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
                                                   href="${pageContext.request.contextPath}/admin/announcements?page=${currentPage - 1}&search=${search}&priority=${priority}&targetRole=${targetRole}&publishStatus=${publishStatus}">
                                                    Previous
                                                </a>
                                            </li>
                                            <c:forEach var="p" begin="1" end="${totalPages}">
                                                <li class="page-item ${currentPage == p ? 'active' : ''}">
                                                    <a class="page-link ${currentPage == p ? 'bg-primary border-primary text-white' : 'bg-dark text-white border-secondary'}"
                                                       href="${pageContext.request.contextPath}/admin/announcements?page=${p}&search=${search}&priority=${priority}&targetRole=${targetRole}&publishStatus=${publishStatus}">
                                                        ${p}
                                                    </a>
                                                </li>
                                            </c:forEach>
                                            <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                                <a class="page-link bg-dark text-white border-secondary"
                                                   href="${pageContext.request.contextPath}/admin/announcements?page=${currentPage + 1}&search=${search}&priority=${priority}&targetRole=${targetRole}&publishStatus=${publishStatus}">
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
                                    <h5 class="text-white fw-bold">No Announcements Found</h5>
                                    <p class="text-secondary small mb-0">
                                        Click "New Announcement" above to broadcast communications across the institution.
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
         CREATE ANNOUNCEMENT MODAL
    ═══════════════════════════════════════════════════════════════ -->
    <div class="modal fade" id="createAnnouncementModal" tabindex="-1" aria-labelledby="createModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered modal-lg">
            <div class="modal-content bg-dark border-secondary text-white">
                <form action="${pageContext.request.contextPath}/admin/announcements" method="post">
                    <input type="hidden" name="action" value="create">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <div class="modal-header border-secondary">
                        <h5 class="modal-title fw-bold" id="createModalLabel"><i class="bi bi-megaphone-fill text-warning me-2"></i>Post New Announcement</h5>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <div class="mb-3">
                            <label class="form-label small fw-semibold text-secondary">Announcement Title <span class="text-danger">*</span></label>
                            <input type="text" name="title" class="form-control bg-dark text-white border-secondary" placeholder="e.g. Mid-Term Examination Schedule Released" required maxlength="200">
                        </div>
                        <div class="mb-3">
                            <label class="form-label small fw-semibold text-secondary">Message Content <span class="text-danger">*</span></label>
                            <textarea name="content" class="form-control bg-dark text-white border-secondary" rows="4" placeholder="Detailed notification message..." required></textarea>
                        </div>
                        <div class="row g-3 mb-3">
                            <div class="col-md-6">
                                <label class="form-label small fw-semibold text-secondary">Target Audience</label>
                                <select name="targetRole" class="form-select bg-dark text-white border-secondary">
                                    <option value="ALL">All Users (Institutional Broadcast)</option>
                                    <option value="STUDENT">Students Only</option>
                                    <option value="TEACHER">Teachers / Faculty Only</option>
                                    <option value="ADMIN">Administrators Only</option>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label small fw-semibold text-secondary">Priority Level</label>
                                <select name="priority" class="form-select bg-dark text-white border-secondary">
                                    <option value="NORMAL">Normal (Standard Bulletin)</option>
                                    <option value="IMPORTANT">Important (Highlighted Notice)</option>
                                    <option value="URGENT">Urgent (Critical System Alert)</option>
                                </select>
                            </div>
                        </div>
                        <div class="row g-3 mb-3">
                            <div class="col-md-6">
                                <label class="form-label small fw-semibold text-secondary">Start Date &amp; Time (Optional)</label>
                                <input type="datetime-local" name="startDate" class="form-control bg-dark text-white border-secondary">
                                <div class="form-text text-secondary" style="font-size: 0.75rem;">Leave empty to publish immediately.</div>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label small fw-semibold text-secondary">Expiry Date &amp; Time (Optional)</label>
                                <input type="datetime-local" name="expiryDate" class="form-control bg-dark text-white border-secondary">
                                <div class="form-text text-secondary" style="font-size: 0.75rem;">Leave empty for perpetual notice.</div>
                            </div>
                        </div>
                        <div class="form-check form-switch mb-2">
                            <input class="form-check-input" type="checkbox" name="isPublished" value="true" id="publishCheck" checked>
                            <label class="form-check-label small" for="publishCheck">Publish announcement</label>
                        </div>
                        <div class="form-check form-switch">
                            <input class="form-check-input" type="checkbox" name="broadcastNotification" value="true" id="broadcastCheck" checked>
                            <label class="form-check-label small text-info fw-semibold" for="broadcastCheck">
                                <i class="bi bi-bell-fill me-1"></i> Broadcast In-App Notification to targeted audience
                            </label>
                        </div>
                    </div>
                    <div class="modal-footer border-secondary">
                        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-primary"><i class="bi bi-send-fill me-1"></i> Publish Announcement</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- ═══════════════════════════════════════════════════════════════
         EDIT ANNOUNCEMENT MODAL
    ═══════════════════════════════════════════════════════════════ -->
    <div class="modal fade" id="editAnnouncementModal" tabindex="-1" aria-labelledby="editModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered modal-lg">
            <div class="modal-content bg-dark border-secondary text-white">
                <form action="${pageContext.request.contextPath}/admin/announcements" method="post">
                    <input type="hidden" name="action" value="update">
                    <input type="hidden" name="id" id="editAnnId">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <div class="modal-header border-secondary">
                        <h5 class="modal-title fw-bold" id="editModalLabel"><i class="bi bi-pencil-square text-info me-2"></i>Edit Announcement</h5>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <div class="mb-3">
                            <label class="form-label small fw-semibold text-secondary">Announcement Title <span class="text-danger">*</span></label>
                            <input type="text" name="title" id="editAnnTitle" class="form-control bg-dark text-white border-secondary" required maxlength="200">
                        </div>
                        <div class="mb-3">
                            <label class="form-label small fw-semibold text-secondary">Message Content <span class="text-danger">*</span></label>
                            <textarea name="content" id="editAnnContent" class="form-control bg-dark text-white border-secondary" rows="4" required></textarea>
                        </div>
                        <div class="row g-3 mb-3">
                            <div class="col-md-6">
                                <label class="form-label small fw-semibold text-secondary">Target Audience</label>
                                <select name="targetRole" id="editAnnRole" class="form-select bg-dark text-white border-secondary">
                                    <option value="ALL">All Users (Institutional Broadcast)</option>
                                    <option value="STUDENT">Students Only</option>
                                    <option value="TEACHER">Teachers / Faculty Only</option>
                                    <option value="ADMIN">Administrators Only</option>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label small fw-semibold text-secondary">Priority Level</label>
                                <select name="priority" id="editAnnPriority" class="form-select bg-dark text-white border-secondary">
                                    <option value="NORMAL">Normal</option>
                                    <option value="IMPORTANT">Important</option>
                                    <option value="URGENT">Urgent</option>
                                </select>
                            </div>
                        </div>
                        <div class="row g-3 mb-3">
                            <div class="col-md-6">
                                <label class="form-label small fw-semibold text-secondary">Start Date &amp; Time</label>
                                <input type="datetime-local" name="startDate" id="editAnnStart" class="form-control bg-dark text-white border-secondary">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label small fw-semibold text-secondary">Expiry Date &amp; Time</label>
                                <input type="datetime-local" name="expiryDate" id="editAnnExpiry" class="form-control bg-dark text-white border-secondary">
                            </div>
                        </div>
                        <div class="form-check form-switch">
                            <input class="form-check-input" type="checkbox" name="isPublished" value="true" id="editAnnPublished">
                            <label class="form-check-label small" for="editAnnPublished">Published</label>
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

    <!-- Modal Population Script -->
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            const editBtns = document.querySelectorAll('.edit-ann-btn');
            const editModal = new bootstrap.Modal(document.getElementById('editAnnouncementModal'));

            editBtns.forEach(btn => {
                btn.addEventListener('click', function() {
                    document.getElementById('editAnnId').value = this.dataset.id;
                    document.getElementById('editAnnTitle').value = this.dataset.title;
                    document.getElementById('editAnnContent').value = this.dataset.content;
                    document.getElementById('editAnnPriority').value = this.dataset.priority;
                    document.getElementById('editAnnRole').value = this.dataset.role;
                    document.getElementById('editAnnStart').value = this.dataset.start || '';
                    document.getElementById('editAnnExpiry').value = this.dataset.expiry || '';
                    document.getElementById('editAnnPublished').checked = (this.dataset.published === 'true');
                    editModal.show();
                });
            });

            // Sidebar toggle
            const sidebar = document.getElementById('adminSidebar');
            const toggle = document.getElementById('sidebarToggle');
            const backdrop = document.getElementById('sidebarBackdrop');
            if (toggle && sidebar && backdrop) {
                toggle.addEventListener('click', () => { sidebar.classList.toggle('show'); backdrop.classList.toggle('show'); });
                backdrop.addEventListener('click', () => { sidebar.classList.remove('show'); backdrop.classList.remove('show'); });
            }
        });
    </script>
</body>
</html>

