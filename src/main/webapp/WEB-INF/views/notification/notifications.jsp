<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Notification Center | ExamPro</title>

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

        <!-- Role-Specific Sidebar Inclusion -->
        <c:choose>
            <c:when test="${currentUser.role == 'TEACHER'}">
                <jsp:include page="/WEB-INF/views/teacher/sidebar.jsp">
                    <jsp:param name="active" value="notifications"/>
                </jsp:include>
            </c:when>
            <c:when test="${currentUser.role == 'STUDENT'}">
                <jsp:include page="/WEB-INF/views/student/sidebar.jsp">
                    <jsp:param name="active" value="notifications"/>
                </jsp:include>
            </c:when>
            <c:otherwise>
                <!-- Mobile Backdrop -->
                <div class="ep-sidebar-backdrop" id="sidebarBackdrop" aria-hidden="true"></div>
                <!-- Admin Sidebar -->
                <aside class="ep-admin-sidebar" id="adminSidebar" aria-label="Administrator Navigation">
                    <div class="ep-sidebar-header">
                        <a href="${pageContext.request.contextPath}/admin/dashboard" class="ep-sidebar-brand">
                            <div class="ep-sidebar-brand-icon"><i class="bi bi-mortarboard-fill"></i></div>
                            <div>
                                <div class="ep-sidebar-brand-text">ExamPro</div>
                                <span class="ep-sidebar-brand-badge">Admin Portal</span>
                            </div>
                        </a>
                    </div>
                    <nav class="ep-sidebar-nav">
                        <div class="ep-nav-section-title">Main Menu</div>
                        <a href="${pageContext.request.contextPath}/admin/dashboard" class="ep-sidebar-link"><i class="bi bi-grid-1x2-fill"></i><span>Dashboard</span></a>
                        <a href="${pageContext.request.contextPath}/admin/students" class="ep-sidebar-link"><i class="bi bi-people-fill"></i><span>Students</span></a>
                        <a href="${pageContext.request.contextPath}/admin/teachers" class="ep-sidebar-link"><i class="bi bi-person-video3"></i><span>Teachers</span></a>
                        <a href="${pageContext.request.contextPath}/admin/subjects" class="ep-sidebar-link"><i class="bi bi-journal-bookmark-fill"></i><span>Subjects</span></a>
                        <a href="${pageContext.request.contextPath}/admin/examinations" class="ep-sidebar-link"><i class="bi bi-file-earmark-text-fill"></i><span>Examinations</span></a>
                        <a href="${pageContext.request.contextPath}/admin/results" class="ep-sidebar-link"><i class="bi bi-bar-chart-line-fill"></i><span>Results</span></a>
                        <a href="${pageContext.request.contextPath}/admin/announcements" class="ep-sidebar-link"><i class="bi bi-megaphone-fill"></i><span>Announcements</span></a>
                        <a href="${pageContext.request.contextPath}/notifications" class="ep-sidebar-link active"><i class="bi bi-bell-fill"></i><span>Notifications</span></a>
                        <div class="ep-nav-section-title mt-3">Administration &amp; Security</div>
                        <a href="${pageContext.request.contextPath}/admin/users" class="ep-sidebar-link"><i class="bi bi-person-badge-fill"></i><span>User Directory</span></a>
                        <a href="${pageContext.request.contextPath}/admin/audit-logs" class="ep-sidebar-link"><i class="bi bi-shield-check"></i><span>Security Logs</span></a>
                        <a href="${pageContext.request.contextPath}/admin/system" class="ep-sidebar-link"><i class="bi bi-cpu-fill"></i><span>System Overview</span></a>
                        <div class="ep-nav-section-title mt-3">Account</div>
                        <form action="${pageContext.request.contextPath}/logout" method="post" class="m-0">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                            <button type="submit" class="ep-sidebar-link ep-sidebar-link-btn"><i class="bi bi-box-arrow-right"></i><span>Sign Out</span></button>
                        </form>
                    </nav>
                </aside>
            </c:otherwise>
        </c:choose>

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
                            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/">Home</a></li>
                            <li class="breadcrumb-item active" aria-current="page">Notification Center</li>
                        </ol>
                    </nav>
                </div>
                <div class="ep-topbar-actions d-flex align-items-center gap-2">
                    <jsp:include page="/WEB-INF/views/common/notif_bell.jsp"/>
                    <div class="d-flex align-items-center gap-2 ps-2 border-start border-secondary">
                        <span class="badge bg-secondary text-uppercase">${currentUser.role}</span>
                        <span class="small text-white fw-semibold d-none d-sm-inline"><c:out value="${currentUser.fullName}"/></span>
                    </div>
                </div>
            </header>

            <!-- Notification Center Body -->
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

                <!-- Page Header & Action Toolbar -->
                <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
                    <div>
                        <h1 class="h4 fw-bold text-white mb-1">
                            <i class="bi bi-bell-fill text-info me-2"></i>Notification Center
                        </h1>
                        <p class="text-secondary small mb-0">
                            Showing <span class="text-white fw-semibold"><c:out value="${totalCount}"/></span> alerts &bull;
                            <span class="badge bg-danger rounded-pill"><c:out value="${unreadCount}"/> Unread</span>
                        </p>
                    </div>

                    <div class="d-flex flex-wrap gap-2">
                        <!-- Filter Tabs -->
                        <div class="btn-group btn-group-sm" role="group">
                            <a href="${pageContext.request.contextPath}/notifications?filter=ALL"
                               class="btn ${filter == 'ALL' ? 'btn-info' : 'btn-outline-secondary'}">
                                All Alerts
                            </a>
                            <a href="${pageContext.request.contextPath}/notifications?filter=UNREAD"
                               class="btn ${filter == 'UNREAD' ? 'btn-info' : 'btn-outline-secondary'}">
                                Unread (${unreadCount})
                            </a>
                        </div>

                        <!-- Mark All as Read Form -->
                        <form action="${pageContext.request.contextPath}/notifications" method="post" class="m-0">
                            <input type="hidden" name="action" value="markAllRead">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                            <button type="submit" class="btn btn-outline-success btn-sm ${unreadCount == 0 ? 'disabled' : ''}">
                                <i class="bi bi-check2-all me-1"></i> Mark All as Read
                            </button>
                        </form>

                        <!-- Clear Read Form -->
                        <form action="${pageContext.request.contextPath}/notifications" method="post" class="m-0">
                            <input type="hidden" name="action" value="clearRead">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                            <button type="submit" class="btn btn-outline-danger btn-sm" onclick="return confirm('Clear all read alerts from your inbox?');">
                                <i class="bi bi-trash3 me-1"></i> Clear Read
                            </button>
                        </form>
                    </div>
                </div>

                <!-- Active Institutional Announcements Section -->
                <c:if test="${not empty activeAnnouncements}">
                    <div class="card bg-dark border-secondary mb-4 shadow-sm">
                        <div class="card-header bg-transparent border-secondary py-2 d-flex align-items-center justify-content-between">
                            <span class="fw-semibold text-white small">
                                <i class="bi bi-megaphone-fill text-warning me-2"></i>Active Institutional Announcements
                            </span>
                            <span class="badge bg-warning text-dark"><c:out value="${activeAnnouncements.size()}"/> Active</span>
                        </div>
                        <div class="card-body p-0">
                            <div class="list-group list-group-flush">
                                <c:forEach var="ann" items="${activeAnnouncements}">
                                    <div class="list-group-item list-group-item-dark p-3 border-secondary bg-opacity-25">
                                        <div class="d-flex align-items-start justify-content-between gap-2 mb-1">
                                            <div class="d-flex align-items-center gap-2">
                                                <span class="badge ${ann.priorityBadgeClass} px-2 py-1"><c:out value="${ann.priority}"/></span>
                                                <h6 class="mb-0 fw-bold text-white"><c:out value="${ann.title}"/></h6>
                                                <c:if test="${not empty ann.subjectCode}">
                                                    <span class="badge bg-secondary"><c:out value="${ann.subjectCode}"/></span>
                                                </c:if>
                                            </div>
                                            <span class="text-secondary small"><i class="bi bi-clock me-1"></i><c:out value="${ann.formattedCreatedAt}"/></span>
                                        </div>
                                        <p class="text-light mb-1 small" style="white-space: pre-line;"><c:out value="${ann.content}"/></p>
                                        <div class="text-secondary" style="font-size: 0.75rem;">
                                            Posted by: <span class="text-white-50"><c:out value="${ann.creatorName}"/></span> (<c:out value="${ann.creatorRole}"/>)
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </div>
                </c:if>

                <!-- Notifications List -->
                <div class="card bg-dark border-secondary shadow-sm">
                    <div class="card-body p-0">
                        <c:choose>
                            <c:when test="${not empty notifications}">
                                <div class="list-group list-group-flush">
                                    <c:forEach var="n" items="${notifications}">
                                        <div class="list-group-item list-group-item-dark p-3 border-secondary d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 ${!n.read ? 'bg-dark bg-opacity-75 border-start border-4 border-info' : ''}">
                                            <div class="d-flex align-items-start gap-3">
                                                <div class="rounded-circle p-2 d-flex align-items-center justify-content-center"
                                                     style="width: 42px; height: 42px; background: rgba(56, 139, 253, 0.15); color: #58a6ff;">
                                                    <i class="bi ${n.categoryIcon} fs-5"></i>
                                                </div>
                                                <div>
                                                    <div class="d-flex align-items-center gap-2 flex-wrap mb-1">
                                                        <span class="badge ${n.categoryBadgeClass} px-2 py-1 small"><c:out value="${n.category}"/></span>
                                                        <c:if test="${n.priority != 'NORMAL'}">
                                                            <span class="badge ${n.priorityBadgeClass} px-2 py-1 small"><c:out value="${n.priority}"/></span>
                                                        </c:if>
                                                        <c:if test="${!n.read}">
                                                            <span class="badge bg-info-subtle text-info border border-info-subtle">NEW</span>
                                                        </c:if>
                                                        <span class="text-secondary small">&bull; <c:out value="${n.timeAgo}"/></span>
                                                    </div>
                                                    <h6 class="fw-bold text-white mb-1"><c:out value="${n.title}"/></h6>
                                                    <p class="text-secondary small mb-0" style="white-space: pre-line;"><c:out value="${n.message}"/></p>
                                                </div>
                                            </div>

                                            <!-- Contextual Actions -->
                                            <div class="d-flex align-items-center gap-2 align-self-end align-self-md-center">
                                                <c:if test="${not empty n.actionUrl}">
                                                    <a href="${pageContext.request.contextPath}${n.actionUrl}"
                                                       class="btn btn-outline-info btn-sm">
                                                        <i class="bi bi-box-arrow-up-right me-1"></i> View
                                                    </a>
                                                </c:if>
                                                <c:if test="${!n.read}">
                                                    <form action="${pageContext.request.contextPath}/notifications" method="post" class="m-0">
                                                        <input type="hidden" name="action" value="markRead">
                                                        <input type="hidden" name="id" value="${n.id}">
                                                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                        <button type="submit" class="btn btn-outline-secondary btn-sm" title="Mark as Read">
                                                            <i class="bi bi-check-lg"></i>
                                                        </button>
                                                    </form>
                                                </c:if>
                                                <form action="${pageContext.request.contextPath}/notifications" method="post" class="m-0">
                                                    <input type="hidden" name="action" value="delete">
                                                    <input type="hidden" name="id" value="${n.id}">
                                                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                    <button type="submit" class="btn btn-outline-secondary btn-sm text-danger border-secondary" title="Dismiss Alert">
                                                        <i class="bi bi-x-lg"></i>
                                                    </button>
                                                </form>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>

                                <!-- Pagination -->
                                <c:if test="${totalPages > 1}">
                                    <div class="card-footer bg-transparent border-secondary py-3">
                                        <ul class="pagination pagination-sm justify-content-center m-0">
                                            <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                                <a class="page-link bg-dark text-white border-secondary"
                                                   href="${pageContext.request.contextPath}/notifications?page=${currentPage - 1}&filter=${filter}">
                                                    Previous
                                                </a>
                                            </li>
                                            <c:forEach var="p" begin="1" end="${totalPages}">
                                                <li class="page-item ${currentPage == p ? 'active' : ''}">
                                                    <a class="page-link ${currentPage == p ? 'bg-info border-info text-dark' : 'bg-dark text-white border-secondary'}"
                                                       href="${pageContext.request.contextPath}/notifications?page=${p}&filter=${filter}">
                                                        ${p}
                                                    </a>
                                                </li>
                                            </c:forEach>
                                            <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                                <a class="page-link bg-dark text-white border-secondary"
                                                   href="${pageContext.request.contextPath}/notifications?page=${currentPage + 1}&filter=${filter}">
                                                    Next
                                                </a>
                                            </li>
                                        </ul>
                                    </div>
                                </c:if>
                            </c:when>
                            <c:otherwise>
                                <div class="text-center py-5">
                                    <i class="bi bi-bell-slash fs-1 text-secondary opacity-50 mb-3 d-block"></i>
                                    <h5 class="text-white fw-bold">No Notifications</h5>
                                    <p class="text-secondary small mb-0">
                                        You are completely caught up! No alerts match your current filter.
                                    </p>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

            </div>
        </main>
    </div>

    <!-- Bootstrap 5 JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
            crossorigin="anonymous"></script>

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
        });
    </script>
</body>
</html>

