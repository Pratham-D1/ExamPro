<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<aside class="ep-admin-sidebar" id="adminSidebar" aria-label="Administrator Navigation">

    <!-- Sidebar Header / Brand -->
    <div class="ep-sidebar-header">
        <a href="${pageContext.request.contextPath}/admin/dashboard" class="ep-sidebar-brand">
            <div class="ep-sidebar-brand-icon">
                <i class="bi bi-mortarboard-fill"></i>
            </div>
            <div>
                <div class="ep-sidebar-brand-text">ExamPro</div>
                <span class="ep-sidebar-brand-badge">Admin Portal</span>
            </div>
        </a>
    </div>

    <!-- Navigation Links -->
    <nav class="ep-sidebar-nav">
        <div class="ep-nav-section-title">Academic &amp; Operations</div>

        <!-- 1. Dashboard -->
        <a href="${pageContext.request.contextPath}/admin/dashboard"
           class="ep-sidebar-link ${param.active == 'dashboard' ? 'active' : ''}">
            <i class="bi bi-grid-1x2-fill"></i>
            <span>Dashboard</span>
        </a>

        <!-- 2. Students -->
        <a href="${pageContext.request.contextPath}/admin/students"
           class="ep-sidebar-link ${param.active == 'students' ? 'active' : ''}">
            <i class="bi bi-people-fill"></i>
            <span>Students</span>
        </a>

        <!-- 3. Teachers -->
        <a href="${pageContext.request.contextPath}/admin/teachers"
           class="ep-sidebar-link ${param.active == 'teachers' ? 'active' : ''}">
            <i class="bi bi-person-video3"></i>
            <span>Teachers</span>
        </a>

        <!-- 4. Subjects -->
        <a href="${pageContext.request.contextPath}/admin/subjects"
           class="ep-sidebar-link ${param.active == 'subjects' ? 'active' : ''}">
            <i class="bi bi-journal-bookmark-fill"></i>
            <span>Subjects</span>
        </a>

        <!-- 5. Examinations -->
        <a href="${pageContext.request.contextPath}/admin/examinations"
           class="ep-sidebar-link ${param.active == 'examinations' ? 'active' : ''}">
            <i class="bi bi-file-earmark-text-fill"></i>
            <span>Examinations</span>
        </a>

        <!-- 6. Question Bank -->
        <a href="${pageContext.request.contextPath}/admin/questions"
           class="ep-sidebar-link ${param.active == 'questions' ? 'active' : ''}">
            <i class="bi bi-patch-question-fill"></i>
            <span>Question Bank</span>
        </a>

        <!-- 7. Results -->
        <a href="${pageContext.request.contextPath}/admin/results"
           class="ep-sidebar-link ${param.active == 'results' ? 'active' : ''}">
            <i class="bi bi-bar-chart-line-fill"></i>
            <span>Results &amp; Analytics</span>
        </a>

        <!-- 7. Announcements -->
        <a href="${pageContext.request.contextPath}/admin/announcements"
           class="ep-sidebar-link ${param.active == 'announcements' ? 'active' : ''}">
            <i class="bi bi-megaphone-fill"></i>
            <span>Announcements</span>
        </a>

        <!-- 8. Notifications -->
        <a href="${pageContext.request.contextPath}/notifications"
           class="ep-sidebar-link ${param.active == 'notifications' ? 'active' : ''}">
            <i class="bi bi-bell-fill"></i>
            <span>Notification Center</span>
        </a>

        <div class="ep-nav-section-title mt-3">Administration &amp; Security</div>

        <!-- 9. User Directory -->
        <a href="${pageContext.request.contextPath}/admin/users"
           class="ep-sidebar-link ${param.active == 'users' ? 'active' : ''}">
            <i class="bi bi-person-badge-fill"></i>
            <span>User Directory</span>
        </a>

        <!-- 10. Audit Logs -->
        <a href="${pageContext.request.contextPath}/admin/audit-logs"
           class="ep-sidebar-link ${param.active == 'audit' ? 'active' : ''}">
            <i class="bi bi-shield-check"></i>
            <span>Security &amp; Audit Logs</span>
        </a>

        <!-- 11. System Overview -->
        <a href="${pageContext.request.contextPath}/admin/system"
           class="ep-sidebar-link ${param.active == 'system' ? 'active' : ''}">
            <i class="bi bi-cpu-fill"></i>
            <span>System Overview</span>
        </a>

        <div class="ep-nav-section-title mt-3">Account &amp; Session</div>

        <!-- 12. Logout -->
        <form id="sidebarLogoutForm" action="${pageContext.request.contextPath}/logout" method="post" class="m-0">
            <input type="hidden" name="csrfToken" value="${csrfToken}">
            <button type="submit" class="ep-sidebar-link ep-sidebar-link-btn" id="sidebarLogoutBtn">
                <i class="bi bi-box-arrow-right"></i>
                <span>Sign Out</span>
            </button>
        </form>
    </nav>

    <!-- Sidebar Footer: Admin Profile Section -->
    <div class="ep-sidebar-footer">
        <div class="ep-admin-user-card">
            <div class="ep-admin-avatar">
                <c:choose>
                    <c:when test="${not empty sessionScope.userName}">
                        <c:out value="${fn:substring(sessionScope.userName, 0, 1)}"/>
                    </c:when>
                    <c:when test="${not empty currentUser.fullName}">
                        <c:out value="${fn:substring(currentUser.fullName, 0, 1)}"/>
                    </c:when>
                    <c:otherwise>A</c:otherwise>
                </c:choose>
            </div>
            <div class="text-truncate">
                <div class="fw-semibold text-white text-truncate small">
                    <c:out value="${not empty sessionScope.userName ? sessionScope.userName : currentUser.fullName}"/>
                </div>
                <div class="text-muted small text-truncate" style="font-size: 0.76rem;">
                    <c:out value="${not empty sessionScope.userEmail ? sessionScope.userEmail : currentUser.email}"/>
                </div>
            </div>
        </div>
    </div>
</aside>

