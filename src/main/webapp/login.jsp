<%-- ════════════════════════════════════════════════════════
     login.jsp  –  ExamPro Login Page
     ════════════════════════════════════════════════════════
     This is a PUBLIC page (accessible without authentication).
     URL: http://localhost:8080/ExamPro/login.jsp

     What it does:
     • Displays a professional login form.
     • Shows error messages from the LoginServlet (if login failed).
     • Submits to POST /ExamPro/login which is handled by LoginServlet.
     • Redirects already-logged-in users to their dashboard.

     Security notes:
     • Uses JSTL <c:out> to escape any dynamic content (prevents XSS).
     • No plain passwords are ever echoed back.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.exampro.util.CsrfUtil" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    // Redirect if already logged in
    if (session.getAttribute("user") != null) {
        String role = (String) session.getAttribute("userRole");
        if ("ADMIN".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        } else if ("TEACHER".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/teacher/dashboard");
        } else {
            response.sendRedirect(request.getContextPath() + "/student/dashboard");
        }
        return;
    }

    // Always ensure CSRF token is available even if login.jsp is accessed directly
    String token = (String) request.getAttribute("csrfToken");
    if (token == null || token.isBlank()) {
        token = CsrfUtil.getToken(session);
        request.setAttribute("csrfToken", token);
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Log in to ExamPro - Your online examination platform.">
    <title>Login | ExamPro</title>

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
<body class="ep-login-page">

    <!-- ═══════════════════════════════════════
         LOGIN CONTAINER
    ═══════════════════════════════════════ -->
    <div class="ep-login-wrapper">

        <!-- Left Panel: Branding -->
        <div class="ep-login-brand d-none d-lg-flex">
            <div class="ep-brand-content text-white text-center">
                <div class="ep-brand-logo mb-4">
                    <i class="bi bi-mortarboard-fill"></i>
                </div>
                <h2 class="fw-bold mb-3">Welcome to ExamPro</h2>
                <p class="ep-brand-tagline">
                    Your complete online examination<br>management platform.
                </p>
                <div class="ep-brand-features mt-5">
                    <div class="ep-brand-feature">
                        <i class="bi bi-shield-lock-fill"></i>
                        <span>Secure Authentication</span>
                    </div>
                    <div class="ep-brand-feature">
                        <i class="bi bi-clock-history"></i>
                        <span>Real-time Exam Timer</span>
                    </div>
                    <div class="ep-brand-feature">
                        <i class="bi bi-bar-chart-line-fill"></i>
                        <span>Instant Results</span>
                    </div>
                </div>
            </div>
        </div>

        <!-- Right Panel: Login Form -->
        <div class="ep-login-form-panel d-flex align-items-center justify-content-center">
            <div class="ep-login-form-container w-100">

                <!-- Mobile Logo (shown only on small screens) -->
                <div class="text-center d-lg-none mb-4">
                    <i class="bi bi-mortarboard-fill text-accent fs-1"></i>
                    <h4 class="fw-bold mt-2">ExamPro</h4>
                </div>

                <h1 class="ep-login-title">Sign In</h1>
                <p class="ep-login-subtitle text-muted mb-4">
                    Enter your credentials to access your dashboard.
                </p>

                <!--
                    Error Message Display
                    LoginServlet sets a "errorMessage" request attribute when
                    login fails. We display it here safely using c:if and c:out.
                    c:out escapes HTML characters to prevent XSS attacks.
                -->
                <c:if test="${not empty errorMessage}">
                    <div class="alert alert-danger alert-dismissible fade show d-flex align-items-center"
                         id="login-error-alert" role="alert">
                        <i class="bi bi-exclamation-triangle-fill me-2"></i>
                        <span><c:out value="${errorMessage}"/></span>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:if>

                <!--
                    Success Message (e.g., after logout)
                -->
                <c:if test="${not empty successMessage}">
                    <div class="alert alert-success alert-dismissible fade show d-flex align-items-center"
                         id="login-success-alert" role="alert">
                        <i class="bi bi-check-circle-fill me-2"></i>
                        <span><c:out value="${successMessage}"/></span>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:if>

                <c:if test="${param.logout eq 'true'}">
                    <div class="alert alert-success alert-dismissible fade show d-flex align-items-center"
                         id="logout-success-alert" role="alert">
                        <i class="bi bi-check-circle-fill me-2"></i>
                        <span>You have been logged out successfully.</span>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:if>

                <!--
                    LOGIN FORM
                    action="/ExamPro/login" → handled by LoginServlet
                    method="post"           → credentials sent in body, not URL
                -->
                <form id="loginForm"
                      action="${pageContext.request.contextPath}/login"
                      method="post"
                      novalidate>

                    <!-- CSRF Protection Token -->
                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">

                    <!-- Email Field -->
                    <div class="mb-4">
                        <label for="email" class="form-label ep-form-label">
                            <i class="bi bi-envelope me-1"></i> Email Address
                        </label>
                        <div class="input-group ep-input-group">
                            <span class="input-group-text">
                                <i class="bi bi-person-fill"></i>
                            </span>
                            <input type="email"
                                   class="form-control ep-form-control"
                                   id="email"
                                   name="email"
                                   value="<c:out value='${param.email}'/>"
                                   placeholder="you@example.com"
                                   autocomplete="email"
                                   required>
                            <div class="invalid-feedback">
                                Please enter a valid email address.
                            </div>
                        </div>
                    </div>

                    <!-- Password Field -->
                    <div class="mb-4">
                        <label for="password" class="form-label ep-form-label">
                            <i class="bi bi-lock me-1"></i> Password
                        </label>
                        <div class="input-group ep-input-group">
                            <span class="input-group-text">
                                <i class="bi bi-lock-fill"></i>
                            </span>
                            <input type="password"
                                   class="form-control ep-form-control"
                                   id="password"
                                   name="password"
                                   placeholder="Enter your password"
                                   autocomplete="current-password"
                                   required
                                   minlength="6">
                            <!-- Toggle password visibility button -->
                            <button class="btn btn-outline-secondary ep-toggle-pwd"
                                    type="button"
                                    id="togglePassword"
                                    aria-label="Toggle password visibility">
                                <i class="bi bi-eye" id="toggleIcon"></i>
                            </button>
                            <div class="invalid-feedback">
                                Password must be at least 6 characters.
                            </div>
                        </div>
                    </div>

                    <!-- Role Selection (Optional with Auto-Detection) -->
                    <div class="mb-4">
                        <label for="role" class="form-label ep-form-label">
                            <i class="bi bi-person-badge me-1"></i> Login As
                        </label>
                        <select class="form-select ep-form-control" id="role" name="role">
                            <option value="" selected>Auto-detect role (Recommended)</option>
                            <option value="ADMIN">Administrator</option>
                            <option value="TEACHER">Teacher</option>
                            <option value="STUDENT">Student</option>
                        </select>
                    </div>

                    <!-- Submit Button -->
                    <div class="d-grid mt-4">
                        <button type="submit"
                                id="btn-login"
                                class="btn btn-accent btn-lg ep-login-btn">
                            <span class="btn-text">
                                <i class="bi bi-box-arrow-in-right me-2"></i> Sign In
                            </span>
                            <span class="btn-spinner d-none">
                                <span class="spinner-border spinner-border-sm me-2" role="status"></span>
                                Signing in...
                            </span>
                        </button>
                    </div>

                </form>

                <!-- Footer links -->
                <div class="text-center mt-4">
                    <a href="${pageContext.request.contextPath}/" class="ep-back-link">
                        <i class="bi bi-arrow-left me-1"></i> Back to Home
                    </a>
                </div>

            </div><!-- /ep-login-form-container -->
        </div><!-- /ep-login-form-panel -->

    </div><!-- /ep-login-wrapper -->

    <!-- Bootstrap 5 JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-YvpcrYf0tY3lHB60NNkmXc4s9bIOgUxi8T/jzmLe3zxj8NxO8yVMnN5Z2X5ZZzEB"
            crossorigin="anonymous"></script>
    <script src="${pageContext.request.contextPath}/js/main.js"></script>
</body>
</html>
