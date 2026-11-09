<%-- ════════════════════════════════════════════════════════
     index.jsp  –  ExamPro Landing / Splash Page
     ════════════════════════════════════════════════════════
     This is the first page a visitor sees.
     It is a PUBLIC page (lives directly in webapp/, not inside
     WEB-INF) so it is accessible at:
         http://localhost:8080/ExamPro/

     What it does:
     • If the user is already logged in (session exists) it
       redirects them straight to their role dashboard.
     • Otherwise it shows a marketing-style landing page with
       a "Get Started" button that links to login.jsp.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    // If a user is already logged in, redirect to dashboard.
    // We check for the "user" attribute that LoginServlet sets.
    if (session.getAttribute("user") != null) {
        String role = (String) session.getAttribute("userRole");
        if ("ADMIN".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        } else if ("TEACHER".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/teacher/dashboard");
        } else {
            response.sendRedirect(request.getContextPath() + "/student/dashboard");
        }
        return; // Stop processing this JSP
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="ExamPro - A modern online examination management system for students, teachers, and administrators.">
    <title>ExamPro | Modern Exam Management System</title>

    <!-- Bootstrap 5 CSS (CDN) -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet"
          integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH"
          crossorigin="anonymous">

    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">

    <!-- Google Fonts: Inter -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">

    <!-- Custom Styles -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
</head>
<body class="exampro-landing">

    <!-- ═══════════════════════════════════════
         NAVIGATION BAR
    ═══════════════════════════════════════ -->
    <nav class="navbar navbar-expand-lg navbar-dark ep-navbar fixed-top">
        <div class="container">
            <a class="navbar-brand fw-bold fs-4" href="${pageContext.request.contextPath}/">
                <i class="bi bi-mortarboard-fill me-2 text-accent"></i>ExamPro
            </a>
            <div class="ms-auto">
                <a href="${pageContext.request.contextPath}/login"
                   class="btn btn-outline-light btn-sm me-2">Log In</a>
            </div>
        </div>
    </nav>

    <!-- ═══════════════════════════════════════
         HERO SECTION
    ═══════════════════════════════════════ -->
    <section class="ep-hero d-flex align-items-center">
        <div class="container text-center text-white">
            <div class="ep-hero-badge mb-4">
                <i class="bi bi-shield-check-fill me-1"></i> Secure &bull; Reliable &bull; Modern
            </div>
            <h1 class="display-3 fw-bold mb-4 ep-hero-title">
                Exam Management<br>
                <span class="text-accent">Re-imagined</span>
            </h1>
            <p class="lead mb-5 ep-hero-subtitle">
                A complete platform for administering exams, managing questions,<br>
                and tracking student performance — all in one place.
            </p>
            <div class="d-flex gap-3 justify-content-center flex-wrap">
                <a href="${pageContext.request.contextPath}/login"
                   id="btn-get-started"
                   class="btn btn-accent btn-lg px-5 py-3">
                    Get Started <i class="bi bi-arrow-right ms-2"></i>
                </a>
                <a href="#features"
                   class="btn btn-outline-light btn-lg px-5 py-3">
                    Learn More
                </a>
            </div>
        </div>
        <!-- Decorative background shapes -->
        <div class="ep-hero-shape ep-shape-1"></div>
        <div class="ep-hero-shape ep-shape-2"></div>
    </section>

    <!-- ═══════════════════════════════════════
         ROLE CARDS
    ═══════════════════════════════════════ -->
    <section id="features" class="ep-section ep-features-section">
        <div class="container">
            <div class="text-center mb-5">
                <h2 class="fw-bold ep-section-title">Designed for Every Role</h2>
                <p class="text-muted ep-section-subtitle">
                    Whether you are an administrator, teacher, or student — ExamPro has you covered.
                </p>
            </div>
            <div class="row g-4">

                <!-- Admin Card -->
                <div class="col-md-4">
                    <div class="ep-card h-100">
                        <div class="ep-card-icon ep-icon-admin">
                            <i class="bi bi-person-gear"></i>
                        </div>
                        <h3 class="ep-card-title">Administrator</h3>
                        <p class="ep-card-text">
                            Manage users, schedule exams, publish results, and oversee
                            the entire examination lifecycle from one dashboard.
                        </p>
                        <ul class="ep-feature-list">
                            <li><i class="bi bi-check-circle-fill"></i> Manage Students &amp; Teachers</li>
                            <li><i class="bi bi-check-circle-fill"></i> Schedule Examinations</li>
                            <li><i class="bi bi-check-circle-fill"></i> Publish Results</li>
                            <li><i class="bi bi-check-circle-fill"></i> Subject Management</li>
                        </ul>
                    </div>
                </div>

                <!-- Teacher Card -->
                <div class="col-md-4">
                    <div class="ep-card h-100 ep-card-featured">
                        <div class="ep-featured-badge">Most Used</div>
                        <div class="ep-card-icon ep-icon-teacher">
                            <i class="bi bi-person-workspace"></i>
                        </div>
                        <h3 class="ep-card-title">Teacher</h3>
                        <p class="ep-card-text">
                            Create and manage a question bank, build examinations,
                            and review detailed student performance reports.
                        </p>
                        <ul class="ep-feature-list">
                            <li><i class="bi bi-check-circle-fill"></i> Question Bank</li>
                            <li><i class="bi bi-check-circle-fill"></i> Build Examinations</li>
                            <li><i class="bi bi-check-circle-fill"></i> Assign Questions</li>
                            <li><i class="bi bi-check-circle-fill"></i> View Student Results</li>
                        </ul>
                    </div>
                </div>

                <!-- Student Card -->
                <div class="col-md-4">
                    <div class="ep-card h-100">
                        <div class="ep-card-icon ep-icon-student">
                            <i class="bi bi-person-video2"></i>
                        </div>
                        <h3 class="ep-card-title">Student</h3>
                        <p class="ep-card-text">
                            Take online exams at your scheduled time, save progress,
                            submit answers, and view your results instantly when released.
                        </p>
                        <ul class="ep-feature-list">
                            <li><i class="bi bi-check-circle-fill"></i> Take Online Exams</li>
                            <li><i class="bi bi-check-circle-fill"></i> Auto-Save Answers</li>
                            <li><i class="bi bi-check-circle-fill"></i> View Result History</li>
                            <li><i class="bi bi-check-circle-fill"></i> Secure Login</li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- ═══════════════════════════════════════
         STATS BANNER
    ═══════════════════════════════════════ -->
    <section class="ep-stats-section">
        <div class="container">
            <div class="row g-4 text-center text-white">
                <div class="col-6 col-md-3">
                    <div class="ep-stat-value">MCQ</div>
                    <div class="ep-stat-label">Question Types</div>
                </div>
                <div class="col-6 col-md-3">
                    <div class="ep-stat-value">3</div>
                    <div class="ep-stat-label">User Roles</div>
                </div>
                <div class="col-6 col-md-3">
                    <div class="ep-stat-value">100%</div>
                    <div class="ep-stat-label">Browser-Based</div>
                </div>
                <div class="col-6 col-md-3">
                    <div class="ep-stat-value">BCrypt</div>
                    <div class="ep-stat-label">Password Security</div>
                </div>
            </div>
        </div>
    </section>

    <!-- ═══════════════════════════════════════
         FOOTER
    ═══════════════════════════════════════ -->
    <footer class="ep-footer text-center text-white py-4">
        <p class="mb-1">
            <i class="bi bi-mortarboard-fill me-2 text-accent"></i>
            <strong>ExamPro</strong> &copy; 2026 &mdash; Enterprise Online Examination Management Platform
        </p>
        <p class="small text-white-50 mb-0">Engineered with Jakarta Servlets 6.0, JSP 3.1, JDBC &amp; MySQL &bull; Demonstration Ready (Phases 1–10)</p>
    </footer>

    <!-- Bootstrap 5 JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-YvpcrYf0tY3lHB60NNkmXc4s9bIOgUxi8T/jzmLe3zxj8NxO8yVMnN5Z2X5ZZzEB"
            crossorigin="anonymous"></script>
    <script src="${pageContext.request.contextPath}/js/main.js"></script>
</body>
</html>
