<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Instructions: <c:out value="${exam.title}"/> | ExamPro</title>

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

    <div class="ep-admin-wrapper">

        <!-- Sidebar Navigation -->
        <jsp:include page="sidebar.jsp">
            <jsp:param name="active" value="exams"/>
        </jsp:include>

        <!-- Main Content Wrapper -->
        <main class="ep-admin-main">

            <!-- Top Navigation Bar -->
            <header class="ep-admin-topbar">
                <div class="d-flex align-items-center gap-3">
                    <button class="btn btn-outline-secondary btn-sm d-lg-none" id="sidebarToggle" type="button" aria-label="Toggle Navigation">
                        <i class="bi bi-list fs-5"></i>
                    </button>
                    <nav aria-label="breadcrumb">
                        <ol class="breadcrumb m-0 ep-topbar-breadcrumb">
                            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/student/dashboard">Student Portal</a></li>
                            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/student/exams">Examinations</a></li>
                            <li class="breadcrumb-item active" aria-current="page">Instructions</li>
                        </ol>
                    </nav>
                </div>
            </header>

            <!-- Instructions Body -->
            <div class="p-3 p-md-4">

                <!-- Flash Notifications -->
                <c:if test="${not empty flashError}">
                    <div class="alert alert-danger alert-dismissible fade show d-flex align-items-center mb-4" role="alert">
                        <i class="bi bi-exclamation-triangle-fill fs-5 me-2"></i>
                        <div><c:out value="${flashError}"/></div>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:if>

                <div class="row justify-content-center">
                    <div class="col-lg-10 col-xl-9">

                        <!-- Main Instructions Card -->
                        <div class="card bg-dark border-secondary shadow-lg">
                            <div class="card-header bg-transparent border-secondary p-4">
                                <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-2">
                                    <span class="badge bg-secondary px-3 py-1">
                                        <i class="bi bi-book me-1"></i> <c:out value="${exam.subjectCode}"/> - <c:out value="${exam.subjectName}"/>
                                    </span>
                                    <span class="badge bg-success-subtle text-success border border-success-subtle px-3 py-1">
                                        <i class="bi bi-shield-check me-1"></i> Verified Candidate: <c:out value="${currentUser.fullName}"/>
                                    </span>
                                </div>
                                <h2 class="h3 fw-bold text-white mb-2"><c:out value="${exam.title}"/></h2>
                                <c:if test="${not empty exam.description}">
                                    <p class="text-secondary mb-0"><c:out value="${exam.description}"/></p>
                                </c:if>
                            </div>

                            <div class="card-body p-4">

                                <!-- Exam Key Metrics Grid -->
                                <div class="row g-3 mb-4 text-center">
                                    <div class="col-6 col-md-3">
                                        <div class="p-3 rounded" style="background: var(--ep-bg-secondary); border: 1px solid var(--ep-bg-tertiary);">
                                            <div class="text-secondary small">Total Duration</div>
                                            <div class="h4 fw-bold text-warning mb-0 mt-1">
                                                <i class="bi bi-stopwatch me-1"></i> <c:out value="${exam.durationMinutes}"/> Min
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-6 col-md-3">
                                        <div class="p-3 rounded" style="background: var(--ep-bg-secondary); border: 1px solid var(--ep-bg-tertiary);">
                                            <div class="text-secondary small">Total Questions</div>
                                            <div class="h4 fw-bold text-info mb-0 mt-1">
                                                <i class="bi bi-patch-question me-1"></i> <c:out value="${questionCount}"/> MCQs
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-6 col-md-3">
                                        <div class="p-3 rounded" style="background: var(--ep-bg-secondary); border: 1px solid var(--ep-bg-tertiary);">
                                            <div class="text-secondary small">Maximum Marks</div>
                                            <div class="h4 fw-bold text-white mb-0 mt-1">
                                                <i class="bi bi-award me-1"></i> <c:out value="${exam.totalMarks}"/>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-6 col-md-3">
                                        <div class="p-3 rounded" style="background: var(--ep-bg-secondary); border: 1px solid var(--ep-bg-tertiary);">
                                            <div class="text-secondary small">Passing Threshold</div>
                                            <div class="h4 fw-bold text-success mb-0 mt-1">
                                                <i class="bi bi-check-circle me-1"></i> <c:out value="${exam.passingMarks}"/>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <!-- Examination Rules & Guidelines -->
                                <h5 class="fw-bold text-white mb-3">
                                    <i class="bi bi-card-checklist text-success me-2"></i> Important Examination Guidelines:
                                </h5>

                                <div class="d-flex flex-column gap-3 mb-4 text-secondary">
                                    <div class="d-flex align-items-start gap-3">
                                        <span class="badge rounded-circle bg-secondary d-flex align-items-center justify-content-center" style="width: 24px; height: 24px;">1</span>
                                        <div>
                                            <strong class="text-white">Strict Time Window:</strong>
                                            Once started, your countdown clock begins immediately. The countdown is continuously validated on the secure server.
                                        </div>
                                    </div>

                                    <div class="d-flex align-items-start gap-3">
                                        <span class="badge rounded-circle bg-secondary d-flex align-items-center justify-content-center" style="width: 24px; height: 24px;">2</span>
                                        <div>
                                            <strong class="text-white">Navigation &amp; Palette:</strong>
                                            You can move between questions freely using the Next/Previous buttons or by clicking any question number on the right-hand Question Palette.
                                        </div>
                                    </div>

                                    <div class="d-flex align-items-start gap-3">
                                        <span class="badge rounded-circle bg-secondary d-flex align-items-center justify-content-center" style="width: 24px; height: 24px;">3</span>
                                        <div>
                                            <strong class="text-white">Instant Auto-Save:</strong>
                                            Every option you select or modify is automatically and securely saved into the database without requiring page reloads.
                                        </div>
                                    </div>

                                    <div class="d-flex align-items-start gap-3">
                                        <span class="badge rounded-circle bg-secondary d-flex align-items-center justify-content-center" style="width: 24px; height: 24px;">4</span>
                                        <div>
                                            <strong class="text-white">Mark for Review:</strong>
                                            Unsure about an answer? Click "Mark for Review" to flag the question in purple so you can easily return before submission.
                                        </div>
                                    </div>

                                    <div class="d-flex align-items-start gap-3">
                                        <span class="badge rounded-circle bg-secondary d-flex align-items-center justify-content-center" style="width: 24px; height: 24px;">5</span>
                                        <div>
                                            <strong class="text-white">Automatic Submission:</strong>
                                            When the remaining time reaches 00:00:00, your test will be automatically locked and evaluated by the server. You may also submit earlier using the "Submit Examination" button.
                                        </div>
                                    </div>

                                    <div class="d-flex align-items-start gap-3">
                                        <span class="badge rounded-circle bg-secondary d-flex align-items-center justify-content-center" style="width: 24px; height: 24px;">6</span>
                                        <div>
                                            <strong class="text-white">No Malpractice:</strong>
                                            All evaluations occur server-side against protected answer keys. Do not close your browser tab or disconnect during an active session.
                                        </div>
                                    </div>
                                </div>

                                <!-- Student Confirmation Form -->
                                <form action="${pageContext.request.contextPath}/student/exam/start" method="post" id="startExamForm">
                                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                                    <input type="hidden" name="examId" value="${exam.id}">

                                    <div class="p-3 rounded mb-4" style="background: rgba(46, 160, 67, 0.08); border: 1px solid rgba(46, 160, 67, 0.3);">
                                        <div class="form-check">
                                            <input class="form-check-input" type="checkbox" id="agreeCheck" required>
                                            <label class="form-check-label text-white fw-semibold" for="agreeCheck">
                                                I confirm that I have read and agree to all examination rules. I am ready to begin my test.
                                            </label>
                                        </div>
                                    </div>

                                    <div class="d-flex justify-content-between align-items-center">
                                        <a href="${pageContext.request.contextPath}/student/exams" class="btn btn-outline-secondary">
                                            <i class="bi bi-arrow-left me-1"></i> Back to Examinations
                                        </a>

                                        <button type="submit" id="startBtn" class="btn btn-success btn-lg px-4 fw-bold"
                                                style="background: #2ea043; border-color: #2ea043;" disabled>
                                            <i class="bi bi-play-circle-fill me-2"></i> Start Examination Now
                                        </button>
                                    </div>
                                </form>

                            </div>
                        </div>

                    </div>
                </div>

            </div>
        </main>
    </div>

    <!-- Bootstrap 5 JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
            crossorigin="anonymous"></script>

    <!-- Checkbox Enabler & Toggle Script -->
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            const agreeCheck = document.getElementById('agreeCheck');
            const startBtn = document.getElementById('startBtn');

            if (agreeCheck && startBtn) {
                agreeCheck.addEventListener('change', function () {
                    startBtn.disabled = !this.checked;
                });
            }

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

