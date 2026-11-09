<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!-- Mobile Sidebar Backdrop -->
<div class="ep-sidebar-backdrop" id="sidebarBackdrop" aria-hidden="true"></div>

<!-- ═══════════════════════════════════════
     STUDENT SIDEBAR NAVIGATION
═══════════════════════════════════════ -->
<aside class="ep-admin-sidebar" id="adminSidebar" aria-label="Student Navigation">

    <!-- Brand Header -->
    <div class="ep-sidebar-header">
        <a href="${pageContext.request.contextPath}/student/dashboard" class="ep-sidebar-brand">
            <div class="ep-sidebar-brand-icon" style="background: rgba(46, 160, 67, 0.15); color: #3fb950; border-color: rgba(46, 160, 67, 0.3);">
                <i class="bi bi-mortarboard-fill"></i>
            </div>
            <div>
                <div class="ep-sidebar-brand-text">ExamPro</div>
                <span class="ep-sidebar-brand-badge" style="background: rgba(46, 160, 67, 0.15); color: #3fb950; border-color: rgba(46, 160, 67, 0.3);">Student Portal</span>
            </div>
        </a>
    </div>

    <!-- Navigation Links -->
    <nav class="ep-sidebar-nav">
        <div class="ep-nav-section-title">Candidate Workspace</div>

        <!-- 1. Dashboard -->
        <a href="${pageContext.request.contextPath}/student/dashboard"
           class="ep-sidebar-link ${param.active == 'dashboard' ? 'student-active' : ''}">
            <i class="bi bi-grid-1x2-fill"></i>
            <span>Dashboard</span>
        </a>

        <!-- 2. Available Exams -->
        <a href="${pageContext.request.contextPath}/student/exams"
           class="ep-sidebar-link ${param.active == 'exams' ? 'student-active' : ''}">
            <i class="bi bi-journal-check"></i>
            <span>Available Exams</span>
        </a>

        <!-- 3. My Examinations -->
        <a href="${pageContext.request.contextPath}/student/my-exams"
           class="ep-sidebar-link ${param.active == 'my-exams' ? 'student-active' : ''}">
            <i class="bi bi-card-checklist"></i>
            <span>My Examinations</span>
        </a>

        <!-- 4. My Results -->
        <a href="${pageContext.request.contextPath}/student/results"
           class="ep-sidebar-link ${param.active == 'results' ? 'student-active' : ''}">
            <i class="bi bi-award-fill"></i>
            <span>My Results</span>
        </a>

        <!-- 5. Performance Analytics -->
        <a href="${pageContext.request.contextPath}/student/analytics"
           class="ep-sidebar-link ${param.active == 'analytics' ? 'student-active' : ''}">
            <i class="bi bi-graph-up-arrow"></i>
            <span>Performance Analytics</span>
        </a>

        <!-- 6. Notifications -->
        <a href="${pageContext.request.contextPath}/notifications"
           class="ep-sidebar-link ${param.active == 'notifications' ? 'student-active' : ''}">
            <i class="bi bi-bell-fill"></i>
            <span>Notifications</span>
        </a>

        <div class="ep-nav-section-title mt-3">Account &amp; Security</div>

        <!-- 5. Profile & Security -->
        <a href="${pageContext.request.contextPath}/student/profile"
           class="ep-sidebar-link ${param.active == 'profile' ? 'student-active' : ''}">
            <i class="bi bi-person-gear"></i>
            <span>My Profile</span>
        </a>

        <!-- 6. Sign Out Button -->
        <form action="${pageContext.request.contextPath}/logout" method="post" class="mt-4 px-3">
            <input type="hidden" name="csrfToken" value="${csrfToken}">
            <button type="submit" class="btn btn-outline-danger btn-sm w-100 d-flex align-items-center justify-content-center gap-2 py-2">
                <i class="bi bi-box-arrow-right"></i>
                <span>Sign Out</span>
            </button>
        </form>
    </nav>

    <!-- Student Footer Info -->
    <div class="ep-sidebar-footer">
        <div class="d-flex align-items-center gap-2">
            <div class="ep-user-avatar" style="background: rgba(46, 160, 67, 0.2); color: #3fb950;">
                <i class="bi bi-person-fill"></i>
            </div>
            <div class="overflow-hidden">
                <div class="ep-user-name text-truncate">
                    <c:out value="${currentUser.fullName != null ? currentUser.fullName : sessionScope.userName}"/>
                </div>
                <div class="ep-user-role" style="color: #3fb950;">
                    Candidate
                </div>
            </div>
        </div>
    </div>
</aside>

