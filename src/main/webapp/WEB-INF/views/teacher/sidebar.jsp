<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!-- Mobile Sidebar Backdrop -->
<div class="ep-sidebar-backdrop" id="sidebarBackdrop" aria-hidden="true"></div>

<!-- ═══════════════════════════════════════
     TEACHER SIDEBAR NAVIGATION
═══════════════════════════════════════ -->
<aside class="ep-admin-sidebar" id="adminSidebar" aria-label="Teacher Navigation">

    <!-- Brand Header -->
    <div class="ep-sidebar-header">
        <a href="${pageContext.request.contextPath}/teacher/dashboard" class="ep-sidebar-brand">
            <div class="ep-sidebar-brand-icon ep-teacher-brand-icon">
                <i class="bi bi-mortarboard-fill"></i>
            </div>
            <div>
                <div class="ep-sidebar-brand-text">ExamPro</div>
                <span class="ep-sidebar-brand-badge ep-teacher-brand-badge">Teacher Portal</span>
            </div>
        </a>
    </div>

    <!-- Navigation Links -->
    <nav class="ep-sidebar-nav">
        <div class="ep-nav-section-title">Teaching Workspace</div>

        <!-- 1. Dashboard -->
        <a href="${pageContext.request.contextPath}/teacher/dashboard"
           class="ep-sidebar-link ${param.active == 'dashboard' ? 'teacher-active' : ''}">
            <i class="bi bi-grid-1x2-fill"></i>
            <span>Dashboard</span>
        </a>

        <!-- 2. Question Bank -->
        <a href="${pageContext.request.contextPath}/teacher/questions"
           class="ep-sidebar-link ${param.active == 'questions' ? 'teacher-active' : ''}">
            <i class="bi bi-patch-question-fill"></i>
            <span>Question Bank</span>
        </a>

        <!-- 3. Examinations -->
        <a href="${pageContext.request.contextPath}/teacher/examinations"
           class="ep-sidebar-link ${param.active == 'examinations' ? 'teacher-active' : ''}">
            <i class="bi bi-file-earmark-text-fill"></i>
            <span>Examinations</span>
        </a>

        <!-- 4. Results & Performance -->
        <a href="${pageContext.request.contextPath}/teacher/results"
           class="ep-sidebar-link ${param.active == 'results' ? 'teacher-active' : ''}">
            <i class="bi bi-bar-chart-line-fill"></i>
            <span>Student Results</span>
        </a>

        <!-- 5. Course Notices -->
        <a href="${pageContext.request.contextPath}/teacher/announcements"
           class="ep-sidebar-link ${param.active == 'announcements' ? 'teacher-active' : ''}">
            <i class="bi bi-megaphone-fill"></i>
            <span>Course Notices</span>
        </a>

        <!-- 6. Notifications -->
        <a href="${pageContext.request.contextPath}/notifications"
           class="ep-sidebar-link ${param.active == 'notifications' ? 'teacher-active' : ''}">
            <i class="bi bi-bell-fill"></i>
            <span>Notifications</span>
        </a>

        <div class="ep-nav-section-title mt-3">Account &amp; Security</div>

        <!-- 5. Profile & Security -->
        <a href="${pageContext.request.contextPath}/teacher/profile"
           class="ep-sidebar-link ${param.active == 'profile' ? 'teacher-active' : ''}">
            <i class="bi bi-person-gear"></i>
            <span>My Profile</span>
        </a>

        <!-- 6. Sign Out -->
        <form id="sidebarLogoutForm" action="${pageContext.request.contextPath}/logout" method="post" class="m-0">
            <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
            <button type="submit" class="ep-sidebar-link ep-sidebar-link-btn" id="sidebarLogoutBtn">
                <i class="bi bi-box-arrow-right"></i>
                <span>Sign Out</span>
            </button>
        </form>
    </nav>

    <!-- Sidebar Footer (Teacher Profile Card) -->
    <div class="ep-sidebar-footer">
        <div class="ep-admin-user-card">
            <div class="ep-admin-avatar ep-teacher-avatar" style="background: linear-gradient(135deg, #a371f7, #6f42c1);">
                <c:choose>
                    <c:when test="${not empty sessionScope.userName}">
                        <c:out value="${sessionScope.userName.substring(0, 1).toUpperCase()}"/>
                    </c:when>
                    <c:otherwise>T</c:otherwise>
                </c:choose>
            </div>
            <div class="overflow-hidden">
                <div class="text-truncate fw-semibold text-white small" title="<c:out value='${sessionScope.userName}'/>">
                    <c:out value="${sessionScope.userName}"/>
                </div>
                <div class="text-truncate text-secondary" style="font-size: 0.75rem;" title="<c:out value='${sessionScope.userEmail}'/>">
                    <c:out value="${sessionScope.userEmail}"/>
                </div>
            </div>
        </div>
    </div>
</aside>

