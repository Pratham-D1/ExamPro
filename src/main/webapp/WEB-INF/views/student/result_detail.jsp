<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Detailed Scorecard: <c:out value="${result.examTitle}"/> | ExamPro</title>

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
            <jsp:param name="active" value="results"/>
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
                            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/student/results">My Results</a></li>
                            <li class="breadcrumb-item active" aria-current="page">Scorecard Review</li>
                        </ol>
                    </nav>
                </div>

                <div class="ep-topbar-actions">
                    <a href="${pageContext.request.contextPath}/student/results" class="btn btn-outline-secondary btn-sm">
                        <i class="bi bi-arrow-left me-1"></i> Back to Results
                    </a>
                </div>
            </header>

            <!-- Scorecard Body -->
            <div class="p-3 p-md-4">

                <!-- ═══════════════════════════════════════════════════════════
                     HERO SCORECARD BANNER
                     ═══════════════════════════════════════════════════════════ -->
                <section class="card bg-dark border-secondary shadow-lg mb-4">
                    <div class="card-body p-4">
                        <div class="row align-items-center g-4">
                            <!-- Left: Details -->
                            <div class="col-lg-8">
                                <div class="d-flex align-items-center gap-2 mb-2">
                                    <span class="badge bg-secondary px-3 py-1">
                                        <c:out value="${result.subjectCode}"/> - <c:out value="${result.subjectName}"/>
                                    </span>
                                    <span class="text-secondary small">
                                        <i class="bi bi-calendar-check me-1"></i> Submitted: <c:out value="${result.formattedSubmittedAt}"/>
                                    </span>
                                </div>

                                <h2 class="h3 fw-bold text-white mb-2"><c:out value="${result.examTitle}"/></h2>
                                <p class="text-secondary mb-3">
                                    Candidate: <strong class="text-white"><c:out value="${currentUser.fullName}"/></strong> &bull;
                                    Evaluation Mode: <span class="badge bg-dark border border-secondary text-info">Server Automated</span>
                                </p>

                                <!-- Metric pills row -->
                                <div class="row g-2 text-center">
                                    <div class="col-3">
                                        <div class="p-2 rounded" style="background: var(--ep-bg-secondary); border: 1px solid var(--ep-bg-tertiary);">
                                            <div class="small text-muted">Total Questions</div>
                                            <div class="fw-bold text-white fs-5">${fn:length(reviewAnswers)}</div>
                                        </div>
                                    </div>
                                    <div class="col-3">
                                        <div class="p-2 rounded" style="background: rgba(46, 160, 67, 0.1); border: 1px solid rgba(46, 160, 67, 0.3);">
                                            <div class="small text-success">Correct</div>
                                            <div class="fw-bold text-success fs-5">${correctCount}</div>
                                        </div>
                                    </div>
                                    <div class="col-3">
                                        <div class="p-2 rounded" style="background: rgba(248, 81, 73, 0.1); border: 1px solid rgba(248, 81, 73, 0.3);">
                                            <div class="small text-danger">Incorrect</div>
                                            <div class="fw-bold text-danger fs-5">${incorrectCount}</div>
                                        </div>
                                    </div>
                                    <div class="col-3">
                                        <div class="p-2 rounded" style="background: var(--ep-bg-secondary); border: 1px solid var(--ep-bg-tertiary);">
                                            <div class="small text-secondary">Unattempted</div>
                                            <div class="fw-bold text-secondary fs-5">${unattemptedCount}</div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Right: Score & Status Card -->
                            <div class="col-lg-4 text-center">
                                <div class="p-4 rounded" style="background: var(--ep-bg-secondary); border: 1px solid var(--ep-bg-tertiary);">
                                    <div class="small text-secondary text-uppercase fw-semibold mb-1">Final Result</div>
                                    <c:choose>
                                        <c:when test="${result.status == 'PASS'}">
                                            <div class="display-6 fw-bold text-success mb-1">
                                                <i class="bi bi-check-circle-fill me-2"></i>PASSED
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="display-6 fw-bold text-danger mb-1">
                                                <i class="bi bi-x-circle-fill me-2"></i>FAILED
                                            </div>
                                        </c:otherwise>
                                    </c:choose>

                                    <div class="h4 fw-bold text-white mb-2">
                                        <fmt:formatNumber value="${result.marksObtained}" maxFractionDigits="1"/>
                                        <span class="text-secondary fs-5">/ <c:out value="${result.totalMarks}"/></span>
                                    </div>

                                    <div class="progress mb-2" style="height: 8px; background: rgba(255,255,255,0.1);">
                                        <div class="progress-bar ${result.status == 'PASS' ? 'bg-success' : 'bg-danger'}"
                                             role="progressbar"
                                             style="width: ${result.percentage}%;"
                                             aria-valuenow="${result.percentage}"
                                             aria-valuemin="0"
                                             aria-valuemax="100"></div>
                                    </div>

                                    <div class="small text-secondary">
                                        Overall Score: <strong class="text-white"><fmt:formatNumber value="${result.percentage}" maxFractionDigits="1"/>%</strong>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </section>

                <!-- ═══════════════════════════════════════════════════════════
                     QUESTION-BY-QUESTION COMPREHENSIVE REVIEW
                     ═══════════════════════════════════════════════════════════ -->
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h5 class="fw-bold text-white mb-0">
                        <i class="bi bi-card-checklist text-success me-2"></i> Question-by-Question Solution Breakdown
                    </h5>
                    <span class="text-secondary small">Revealed with official answer keys</span>
                </div>

                <c:choose>
                    <c:when test="${not empty reviewAnswers}">
                        <div class="d-flex flex-column gap-3">
                            <c:forEach var="a" items="${reviewAnswers}">
                                <div class="review-question-card">
                                    <!-- Question meta header -->
                                    <div class="d-flex justify-content-between align-items-center mb-3 pb-2 border-bottom border-secondary">
                                        <div class="d-flex align-items-center gap-2">
                                            <span class="badge bg-primary px-3 py-1 fw-bold">
                                                Question ${a.questionNumber}
                                            </span>
                                            <c:if test="${not empty a.topic}">
                                                <span class="badge bg-secondary px-2 py-1 small">
                                                    <i class="bi bi-tag me-1"></i> <c:out value="${a.topic}"/>
                                                </span>
                                            </c:if>
                                            <span class="badge ${a.difficulty == 'EASY' ? 'mcq-badge-difficulty-easy' : (a.difficulty == 'HARD' ? 'mcq-badge-difficulty-hard' : 'mcq-badge-difficulty-medium')} px-2 py-1 small">
                                                <c:out value="${a.difficulty}"/>
                                            </span>
                                        </div>

                                        <div class="d-flex align-items-center gap-2">
                                            <c:choose>
                                                <c:when test="${empty a.selectedOptionKey}">
                                                    <span class="badge bg-secondary-subtle text-secondary border border-secondary-subtle px-2 py-1">
                                                        <i class="bi bi-dash-circle me-1"></i> Unattempted (0.0 / <c:out value="${a.questionMarks}"/>)
                                                    </span>
                                                </c:when>
                                                <c:when test="${a.correct}">
                                                    <span class="badge bg-success-subtle text-success border border-success-subtle px-2 py-1">
                                                        <i class="bi bi-check-circle-fill me-1"></i> Correct (+<fmt:formatNumber value="${a.marksAwarded}" maxFractionDigits="1"/> / <c:out value="${a.questionMarks}"/>)
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-danger-subtle text-danger border border-danger-subtle px-2 py-1">
                                                        <i class="bi bi-x-circle-fill me-1"></i> Incorrect (0.0 / <c:out value="${a.questionMarks}"/>)
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>

                                    <!-- Question statement -->
                                    <div class="text-white fw-semibold mb-3 fs-6">
                                        <c:out value="${a.questionText}"/>
                                    </div>

                                    <!-- MCQ Options List with Correct / Student choice highlights -->
                                    <div class="d-flex flex-column gap-2 mb-2">
                                        <c:forEach var="opt" items="${a.options}">
                                            <c:set var="isChosen" value="${a.selectedOptionKey == opt.optionKey}"/>
                                            <c:set var="isCorrectOpt" value="${opt.correct}"/>

                                            <div class="review-option-card ${isCorrectOpt ? 'correct-answer' : (isChosen && !isCorrectOpt ? 'wrong-answer' : '')}">
                                                <span class="take-option-badge" style="${isCorrectOpt ? 'background: #3fb950; color: #fff;' : (isChosen ? 'background: #f85149; color: #fff;' : '')}">
                                                    <c:out value="${opt.optionKey}"/>
                                                </span>

                                                <span class="flex-grow-1 ${isCorrectOpt ? 'text-white fw-semibold' : (isChosen ? 'text-white-50' : 'text-secondary')}">
                                                    <c:out value="${opt.optionText}"/>
                                                </span>

                                                <div class="d-flex align-items-center gap-2">
                                                    <c:if test="${isChosen}">
                                                        <span class="badge ${isCorrectOpt ? 'bg-success' : 'bg-danger'} px-2 py-1 small">
                                                            <i class="bi ${isCorrectOpt ? 'bi-check' : 'bi-x'} me-1"></i> Your Choice
                                                        </span>
                                                    </c:if>
                                                    <c:if test="${isCorrectOpt}">
                                                        <span class="badge bg-success-subtle text-success border border-success-subtle px-2 py-1 small">
                                                            <i class="bi bi-check-all me-1"></i> Correct Answer
                                                        </span>
                                                    </c:if>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </div>

                                    <!-- Official Explanation if available -->
                                    <c:if test="${not empty a.explanation}">
                                        <div class="review-explanation-box">
                                            <div class="fw-bold text-info small mb-1">
                                                <i class="bi bi-info-circle-fill me-1"></i> Solution Explanation:
                                            </div>
                                            <div><c:out value="${a.explanation}"/></div>
                                        </div>
                                    </c:if>

                                </div>
                            </c:forEach>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="card bg-dark border-secondary p-4 text-center">
                            <p class="text-secondary mb-0">Detailed question-level review is not available for this record.</p>
                        </div>
                    </c:otherwise>
                </c:choose>

                <!-- Back Navigation Action -->
                <div class="mt-4 text-center">
                    <a href="${pageContext.request.contextPath}/student/results" class="btn btn-outline-secondary px-4 me-2">
                        <i class="bi bi-arrow-left me-1"></i> Back to My Results
                    </a>
                    <a href="${pageContext.request.contextPath}/student/dashboard" class="btn btn-primary px-4" style="background: #2ea043; border-color: #2ea043;">
                        <i class="bi bi-grid-1x2-fill me-1"></i> Return to Dashboard
                    </a>
                </div>

            </div>
        </main>
    </div>

    <!-- Bootstrap 5 JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
            crossorigin="anonymous"></script>

    <!-- Sidebar Toggle Script -->
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

