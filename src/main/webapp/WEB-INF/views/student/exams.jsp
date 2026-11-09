<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${pageTitle}"/> | ExamPro</title>

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
            <jsp:param name="active" value="${activeTab == 'my-exams' ? 'my-exams' : 'exams'}"/>
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
                            <li class="breadcrumb-item active" aria-current="page"><c:out value="${pageTitle}"/></li>
                        </ol>
                    </nav>
                </div>

                <div class="ep-topbar-actions">
                    <div class="btn-group btn-group-sm" role="group">
                        <a href="${pageContext.request.contextPath}/student/exams"
                           class="btn ${activeTab != 'my-exams' ? 'btn-success' : 'btn-outline-secondary'}">
                            <i class="bi bi-journal-check me-1"></i> Available Now
                        </a>
                        <a href="${pageContext.request.contextPath}/student/my-exams"
                           class="btn ${activeTab == 'my-exams' ? 'btn-success' : 'btn-outline-secondary'}">
                            <i class="bi bi-card-checklist me-1"></i> All Candidate Exams
                        </a>
                    </div>
                </div>
            </header>

            <!-- Page Body -->
            <div class="p-3 p-md-4">

                <!-- Flash Notifications -->
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
                <c:if test="${not empty flashWarning}">
                    <div class="alert alert-warning alert-dismissible fade show d-flex align-items-center mb-4" role="alert">
                        <i class="bi bi-exclamation-circle-fill fs-5 me-2"></i>
                        <div><c:out value="${flashWarning}"/></div>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:if>

                <!-- Page Header with Search -->
                <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
                    <div>
                        <h1 class="h4 fw-bold text-white mb-1"><c:out value="${pageTitle}"/></h1>
                        <p class="text-secondary small mb-0">
                            Showing <span class="text-white fw-semibold"><c:out value="${totalCount}"/></span> examinations matching your authorization criteria.
                        </p>
                    </div>

                    <form action="${pageContext.request.contextPath}/student/${activeTab == 'my-exams' ? 'my-exams' : 'exams'}"
                          method="get" class="d-flex gap-2">
                        <div class="input-group input-group-sm" style="min-width: 260px;">
                            <input type="text" name="search" class="form-control bg-dark text-white border-secondary"
                                   placeholder="Search by title or subject..." value="${search}">
                            <button type="submit" class="btn btn-outline-secondary">
                                <i class="bi bi-search"></i>
                            </button>
                        </div>
                        <c:if test="${not empty search}">
                            <a href="${pageContext.request.contextPath}/student/${activeTab == 'my-exams' ? 'my-exams' : 'exams'}"
                               class="btn btn-outline-danger btn-sm">
                                <i class="bi bi-x-circle"></i>
                            </a>
                        </c:if>
                    </form>
                </div>

                <!-- Examinations Grid / List -->
                <c:choose>
                    <c:when test="${not empty exams}">
                        <div class="row g-4">
                            <c:forEach var="exam" items="${exams}">
                                <div class="col-md-6 col-xl-4">
                                    <div class="card bg-dark border-secondary h-100 shadow-sm d-flex flex-column justify-content-between">
                                        <div class="card-body">
                                            <!-- Subject badge & status badge -->
                                            <div class="d-flex justify-content-between align-items-start mb-3">
                                                <span class="badge bg-secondary px-2 py-1">
                                                    <i class="bi bi-book me-1"></i> <c:out value="${exam.subjectCode}"/>
                                                </span>
                                                <c:choose>
                                                    <c:when test="${exam.studentExamStatus == 'IN_PROGRESS'}">
                                                        <span class="badge bg-warning text-dark px-2 py-1">
                                                            <i class="bi bi-arrow-repeat me-1"></i> IN PROGRESS
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${exam.studentExamStatus == 'AVAILABLE'}">
                                                        <span class="badge bg-success-subtle text-success border border-success-subtle px-2 py-1">
                                                            <i class="bi bi-circle-fill me-1 small"></i> AVAILABLE
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${exam.studentExamStatus == 'SUBMITTED'}">
                                                        <span class="badge bg-purple px-2 py-1" style="background: rgba(163, 113, 247, 0.2); color: #a371f7; border: 1px solid rgba(163, 113, 247, 0.4);">
                                                            <i class="bi bi-check2-all me-1"></i> COMPLETED
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${exam.studentExamStatus == 'TIMEOUT'}">
                                                        <span class="badge bg-danger-subtle text-danger px-2 py-1">
                                                            <i class="bi bi-alarm me-1"></i> TIME EXPIRED
                                                        </span>
                                                    </c:when>
                                                    <c:when test="${exam.studentExamStatus == 'UPCOMING'}">
                                                        <span class="badge bg-info-subtle text-info px-2 py-1">
                                                            <i class="bi bi-clock me-1"></i> UPCOMING
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge bg-secondary-subtle text-secondary px-2 py-1">
                                                            <c:out value="${exam.status}"/>
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>

                                            <h5 class="card-title text-white fw-bold mb-1">
                                                <c:out value="${exam.title}"/>
                                            </h5>
                                            <div class="small text-secondary mb-3">
                                                <c:out value="${exam.subjectName}"/>
                                            </div>

                                            <c:if test="${not empty exam.description}">
                                                <p class="card-text text-secondary small mb-3 text-truncate" style="max-height: 40px;">
                                                    <c:out value="${exam.description}"/>
                                                </p>
                                            </c:if>

                                            <!-- Metadata stats -->
                                            <div class="row g-2 text-center py-2 px-1 mb-3 rounded" style="background: var(--ep-bg-secondary); border: 1px solid var(--ep-bg-tertiary);">
                                                <div class="col-4 border-end border-secondary">
                                                    <div class="small text-muted">Duration</div>
                                                    <div class="fw-bold text-white small"><c:out value="${exam.durationMinutes}"/> min</div>
                                                </div>
                                                <div class="col-4 border-end border-secondary">
                                                    <div class="small text-muted">Questions</div>
                                                    <div class="fw-bold text-white small"><c:out value="${exam.totalQuestions}"/> MCQs</div>
                                                </div>
                                                <div class="col-4">
                                                    <div class="small text-muted">Max Marks</div>
                                                    <div class="fw-bold text-white small"><c:out value="${exam.totalMarks}"/></div>
                                                </div>
                                            </div>

                                            <!-- Timing information -->
                                            <div class="small text-secondary mb-1">
                                                <i class="bi bi-calendar-event me-1"></i> Starts: <c:out value="${exam.formattedStartTime}"/>
                                            </div>
                                            <div class="small text-secondary mb-3">
                                                <i class="bi bi-calendar-x me-1"></i> Closes: <c:out value="${exam.formattedEndTime}"/>
                                            </div>
                                        </div>

                                        <div class="card-footer bg-transparent border-secondary p-3">
                                            <c:choose>
                                                <c:when test="${exam.studentExamStatus == 'IN_PROGRESS'}">
                                                    <a href="${pageContext.request.contextPath}/student/exam/take?attemptId=${exam.activeAttemptId}"
                                                       class="btn btn-warning w-100 fw-bold d-flex align-items-center justify-content-center gap-2">
                                                        <i class="bi bi-arrow-repeat"></i>
                                                        <span>Resume Ongoing Exam</span>
                                                    </a>
                                                </c:when>
                                                <c:when test="${exam.studentExamStatus == 'AVAILABLE'}">
                                                    <a href="${pageContext.request.contextPath}/student/exam/instructions?examId=${exam.id}"
                                                       class="btn btn-success w-100 fw-bold d-flex align-items-center justify-content-center gap-2"
                                                       style="background: #2ea043; border-color: #2ea043;">
                                                        <i class="bi bi-play-circle-fill"></i>
                                                        <span>View Instructions &amp; Start</span>
                                                    </a>
                                                </c:when>
                                                <c:when test="${exam.studentExamStatus == 'SUBMITTED' || exam.studentExamStatus == 'TIMEOUT'}">
                                                    <a href="${pageContext.request.contextPath}/student/result-detail?examId=${exam.id}"
                                                       class="btn btn-outline-info w-100 d-flex align-items-center justify-content-center gap-2">
                                                        <i class="bi bi-award-fill"></i>
                                                        <span>View Evaluated Scorecard</span>
                                                    </a>
                                                </c:when>
                                                <c:when test="${exam.studentExamStatus == 'UPCOMING'}">
                                                    <button class="btn btn-outline-secondary w-100" disabled>
                                                        <i class="bi bi-lock me-1"></i> Scheduled for Future
                                                    </button>
                                                </c:when>
                                                <c:otherwise>
                                                    <button class="btn btn-outline-secondary w-100" disabled>
                                                        <i class="bi bi-slash-circle me-1"></i> Exam Concluded
                                                    </button>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>

                        <!-- Pagination -->
                        <c:if test="${totalPages > 1}">
                            <nav aria-label="Page navigation" class="mt-4">
                                <ul class="pagination pagination-sm justify-content-center m-0">
                                    <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                        <a class="page-link bg-dark text-white border-secondary"
                                           href="${pageContext.request.contextPath}/student/${activeTab == 'my-exams' ? 'my-exams' : 'exams'}?page=${currentPage - 1}&search=${search}">
                                            Previous
                                        </a>
                                    </li>
                                    <c:forEach var="p" begin="1" end="${totalPages}">
                                        <li class="page-item ${currentPage == p ? 'active' : ''}">
                                            <a class="page-link ${currentPage == p ? 'bg-success border-success text-white' : 'bg-dark text-white border-secondary'}"
                                               href="${pageContext.request.contextPath}/student/${activeTab == 'my-exams' ? 'my-exams' : 'exams'}?page=${p}&search=${search}">
                                                ${p}
                                            </a>
                                        </li>
                                    </c:forEach>
                                    <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                        <a class="page-link bg-dark text-white border-secondary"
                                           href="${pageContext.request.contextPath}/student/${activeTab == 'my-exams' ? 'my-exams' : 'exams'}?page=${currentPage + 1}&search=${search}">
                                            Next
                                        </a>
                                    </li>
                                </ul>
                            </nav>
                        </c:if>
                    </c:when>
                    <c:otherwise>
                        <div class="card bg-dark border-secondary p-5 text-center">
                            <i class="bi bi-journal-x fs-1 text-secondary opacity-50 mb-3"></i>
                            <h5 class="text-white fw-bold">No Examinations Found</h5>
                            <p class="text-secondary small mb-0">
                                There are currently no examinations matching your query or assigned to your profile.
                            </p>
                            <c:if test="${not empty search}">
                                <div class="mt-3">
                                    <a href="${pageContext.request.contextPath}/student/${activeTab == 'my-exams' ? 'my-exams' : 'exams'}" class="btn btn-outline-secondary btn-sm">
                                        Clear Search Filters
                                    </a>
                                </div>
                            </c:if>
                        </div>
                    </c:otherwise>
                </c:choose>

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

