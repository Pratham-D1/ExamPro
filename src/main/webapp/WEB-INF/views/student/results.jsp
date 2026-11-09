<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Results &amp; Scorecards | ExamPro</title>

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
                            <li class="breadcrumb-item active" aria-current="page">My Results</li>
                        </ol>
                    </nav>
                </div>
            </header>

            <!-- Results Body -->
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
                        <h1 class="h4 fw-bold text-white mb-1">Examination Scorecards &amp; Evaluation History</h1>
                        <p class="text-secondary small mb-0">
                            Showing <span class="text-white fw-semibold"><c:out value="${totalCount}"/></span> completed examination submissions.
                        </p>
                    </div>

                    <div class="d-flex align-items-center gap-2">
                        <a href="${pageContext.request.contextPath}/student/analytics" class="btn btn-outline-success btn-sm">
                            <i class="bi bi-graph-up-arrow me-1"></i> Performance Analytics
                        </a>
                        <form action="${pageContext.request.contextPath}/student/results" method="get" class="d-flex gap-2">
                            <div class="input-group input-group-sm" style="min-width: 240px;">
                                <input type="text" name="search" class="form-control bg-dark text-white border-secondary"
                                       placeholder="Search by exam or subject..." value="${search}">
                                <button type="submit" class="btn btn-outline-secondary">
                                    <i class="bi bi-search"></i>
                                </button>
                            </div>
                            <c:if test="${not empty search}">
                                <a href="${pageContext.request.contextPath}/student/results" class="btn btn-outline-danger btn-sm">
                                    <i class="bi bi-x-circle"></i>
                                </a>
                            </c:if>
                        </form>
                    </div>
                </div>

                <!-- Results Table Card -->
                <div class="card bg-dark border-secondary shadow-sm">
                    <div class="card-body p-0">
                        <c:choose>
                            <c:when test="${not empty results}">
                                <div class="table-responsive">
                                    <table class="table table-dark table-hover align-middle mb-0">
                                        <thead>
                                            <tr class="text-secondary small">
                                                <th class="ps-3 py-3">Examination</th>
                                                <th>Subject</th>
                                                <th>Submitted At</th>
                                                <th>Marks Obtained</th>
                                                <th>Total Marks</th>
                                                <th>Percentage</th>
                                                <th>Result Status</th>
                                                <th class="text-end pe-3">Action</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="res" items="${results}">
                                                <tr>
                                                    <td class="ps-3">
                                                        <div class="fw-bold text-white"><c:out value="${res.examTitle}"/></div>
                                                    </td>
                                                    <td>
                                                        <span class="badge bg-secondary"><c:out value="${res.subjectCode}"/></span>
                                                        <span class="small text-secondary ms-1"><c:out value="${res.subjectName}"/></span>
                                                    </td>
                                                    <td class="small text-secondary">
                                                        <c:out value="${res.formattedSubmittedAt}"/>
                                                    </td>
                                                    <td class="fw-bold text-white fs-6">
                                                        <c:choose>
                                                            <c:when test="${res.published}">
                                                                <fmt:formatNumber value="${res.marksObtained}" maxFractionDigits="1"/>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="text-secondary small fst-italic">Pending</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td class="text-secondary">
                                                        <c:out value="${res.totalMarks}"/>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${res.published}">
                                                                <div class="d-flex align-items-center gap-2">
                                                                    <div class="progress flex-grow-1" style="height: 6px; width: 60px; background: rgba(255,255,255,0.1);">
                                                                        <div class="progress-bar ${res.status == 'PASS' ? 'bg-success' : 'bg-danger'}"
                                                                             role="progressbar"
                                                                             style="width: ${res.percentage}%;"
                                                                             aria-valuenow="${res.percentage}"
                                                                             aria-valuemin="0"
                                                                             aria-valuemax="100"></div>
                                                                    </div>
                                                                    <span class="small fw-semibold"><fmt:formatNumber value="${res.percentage}" maxFractionDigits="1"/>%</span>
                                                                </div>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="text-secondary small">--</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${not res.published}">
                                                                <span class="badge bg-warning-subtle text-warning border border-warning-subtle px-2 py-1">
                                                                    <i class="bi bi-clock-history me-1"></i> Pending Publication
                                                                </span>
                                                            </c:when>
                                                            <c:when test="${res.status == 'PASS'}">
                                                                <span class="badge bg-success-subtle text-success border border-success-subtle px-2 py-1">
                                                                    <i class="bi bi-check-circle me-1"></i> PASS
                                                                </span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge bg-danger-subtle text-danger border border-danger-subtle px-2 py-1">
                                                                    <i class="bi bi-x-circle me-1"></i> FAIL
                                                                </span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td class="text-end pe-3">
                                                        <c:choose>
                                                            <c:when test="${res.published}">
                                                                <div class="btn-group btn-group-sm">
                                                                    <a href="${pageContext.request.contextPath}/student/result-detail?examId=${res.examinationId}"
                                                                       class="btn btn-outline-info">
                                                                        <i class="bi bi-file-earmark-bar-graph me-1"></i> Review
                                                                    </a>
                                                                    <a href="${pageContext.request.contextPath}/student/result/print?examId=${res.examinationId}"
                                                                       target="_blank"
                                                                       class="btn btn-outline-secondary"
                                                                       title="Print Official Scorecard">
                                                                        <i class="bi bi-printer"></i>
                                                                    </a>
                                                                </div>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <button class="btn btn-outline-secondary btn-sm" disabled title="Awaiting institutional publication">
                                                                    <i class="bi bi-lock me-1"></i> Evaluation Underway
                                                                </button>
                                                            </c:otherwise>
                                                        </c:choose>
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
                                                   href="${pageContext.request.contextPath}/student/results?page=${currentPage - 1}&search=${search}">
                                                    Previous
                                                </a>
                                            </li>
                                            <c:forEach var="p" begin="1" end="${totalPages}">
                                                <li class="page-item ${currentPage == p ? 'active' : ''}">
                                                    <a class="page-link ${currentPage == p ? 'bg-success border-success text-white' : 'bg-dark text-white border-secondary'}"
                                                       href="${pageContext.request.contextPath}/student/results?page=${p}&search=${search}">
                                                        ${p}
                                                    </a>
                                                </li>
                                            </c:forEach>
                                            <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                                <a class="page-link bg-dark text-white border-secondary"
                                                   href="${pageContext.request.contextPath}/student/results?page=${currentPage + 1}&search=${search}">
                                                    Next
                                                </a>
                                            </li>
                                        </ul>
                                    </div>
                                </c:if>
                            </c:when>
                            <c:otherwise>
                                <div class="text-center py-5">
                                    <i class="bi bi-award fs-1 text-secondary opacity-50 mb-3"></i>
                                    <h5 class="text-white fw-bold">No Scorecards Available</h5>
                                    <p class="text-secondary small mb-0">
                                        You have not completed any examinations matching the search criteria yet.
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

