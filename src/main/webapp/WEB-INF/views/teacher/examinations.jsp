<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Assigned Examinations | ExamPro</title>

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
            <jsp:param name="active" value="examinations"/>
        </jsp:include>

        <!-- Main Content -->
        <main class="ep-admin-main">

            <!-- Topbar -->
            <header class="ep-admin-topbar">
                <div class="d-flex align-items-center gap-3">
                    <button class="btn btn-outline-secondary btn-sm d-lg-none" id="sidebarToggle" type="button" aria-label="Toggle Navigation">
                        <i class="bi bi-list fs-5"></i>
                    </button>
                    <nav aria-label="breadcrumb">
                        <ol class="breadcrumb m-0 ep-topbar-breadcrumb">
                            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/teacher/dashboard">Teacher Portal</a></li>
                            <li class="breadcrumb-item active" aria-current="page">Examinations</li>
                        </ol>
                    </nav>
                </div>
                <div class="ep-topbar-actions">
                    <span class="text-secondary small d-none d-md-inline">
                        <i class="bi bi-shield-lock me-1"></i> Role: Teacher
                    </span>
                </div>
            </header>

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

                <!-- Page Header -->
                <div class="d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center mb-4 gap-3">
                    <div>
                        <h1 class="h4 fw-bold text-white mb-1">
                            <i class="bi bi-file-earmark-text-fill text-info me-2"></i> Assigned Examination Schedules
                        </h1>
                        <p class="text-secondary small mb-0">
                            Examinations for your assigned courses. Allocate questions from your Question Bank and verify readiness for assessment.
                        </p>
                    </div>
                    <div>
                        <span class="badge bg-secondary px-3 py-2 fs-6">
                            Total: <strong class="text-white"><c:out value="${totalCount}"/></strong> examinations
                        </span>
                    </div>
                </div>

                <!-- Search & Status Filter Toolbar -->
                <div class="card bg-dark border-secondary p-3 mb-4">
                    <form method="get" action="${pageContext.request.contextPath}/teacher/examinations" class="row g-2 align-items-center">
                        <div class="col-md-5">
                            <div class="input-group input-group-sm">
                                <span class="input-group-text bg-secondary border-secondary text-secondary">
                                    <i class="bi bi-search"></i>
                                </span>
                                <input type="text" name="search" class="form-control bg-dark text-white border-secondary"
                                       placeholder="Search exam title or subject..." value="<c:out value='${search}'/>">
                            </div>
                        </div>

                        <div class="col-md-4">
                            <select name="status" class="form-select form-select-sm bg-dark text-white border-secondary">
                                <option value="ALL" ${selectedStatus == 'ALL' ? 'selected' : ''}>All Statuses</option>
                                <option value="DRAFT" ${selectedStatus == 'DRAFT' ? 'selected' : ''}>DRAFT</option>
                                <option value="SCHEDULED" ${selectedStatus == 'SCHEDULED' ? 'selected' : ''}>SCHEDULED</option>
                                <option value="ONGOING" ${selectedStatus == 'ONGOING' ? 'selected' : ''}>ONGOING</option>
                                <option value="COMPLETED" ${selectedStatus == 'COMPLETED' ? 'selected' : ''}>COMPLETED</option>
                                <option value="CANCELLED" ${selectedStatus == 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
                            </select>
                        </div>

                        <div class="col-md-3 d-flex gap-2 justify-content-md-end">
                            <button type="submit" class="btn btn-outline-info btn-sm">
                                <i class="bi bi-funnel me-1"></i> Filter
                            </button>
                            <a href="${pageContext.request.contextPath}/teacher/examinations" class="btn btn-outline-secondary btn-sm">
                                Reset
                            </a>
                        </div>
                    </form>
                </div>

                <!-- Examinations Table -->
                <div class="card bg-dark border-secondary mb-4">
                    <div class="table-responsive">
                        <table class="table table-dark table-hover align-middle mb-0">
                            <thead>
                                <tr class="text-secondary small border-secondary">
                                    <th style="width: 5%;">#</th>
                                    <th style="width: 25%;">Examination Details</th>
                                    <th style="width: 15%;">Subject</th>
                                    <th style="width: 15%;">Schedule</th>
                                    <th style="width: 10%;">Duration</th>
                                    <th style="width: 15%;">Questions &amp; Marks</th>
                                    <th style="width: 15%;" class="text-end">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty examinations}">
                                        <c:forEach var="exam" items="${examinations}" varStatus="status">
                                            <tr>
                                                <td class="text-muted small"><c:out value="${status.index + 1}"/></td>
                                                <td>
                                                    <div class="fw-semibold text-white mb-1"><c:out value="${exam.title}"/></div>
                                                    <div class="d-flex align-items-center gap-2">
                                                        <span class="badge ${exam.statusBadgeClass}"><c:out value="${exam.status}"/></span>
                                                        <c:choose>
                                                            <c:when test="${exam.ready}">
                                                                <span class="badge bg-success" title="Questions match total marks">
                                                                    <i class="bi bi-check2-circle me-1"></i>Ready
                                                                </span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge bg-warning text-dark" title="Incomplete question assignment">
                                                                    <i class="bi bi-clock me-1"></i>Pending
                                                                </span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </div>
                                                </td>
                                                <td>
                                                    <span class="badge bg-primary font-monospace"><c:out value="${exam.subjectCode}"/></span>
                                                    <div class="small text-muted text-truncate" style="max-width: 140px;"><c:out value="${exam.subjectName}"/></div>
                                                </td>
                                                <td class="small text-secondary">
                                                    <div><i class="bi bi-play-circle me-1"></i><c:out value="${exam.formattedStartTime}"/></div>
                                                    <div><i class="bi bi-stop-circle me-1"></i><c:out value="${exam.formattedEndTime}"/></div>
                                                </td>
                                                <td class="small text-secondary">
                                                    <i class="bi bi-stopwatch me-1"></i><c:out value="${exam.durationMinutes}"/> mins
                                                </td>
                                                <td>
                                                    <div class="small">
                                                        <strong class="text-white"><c:out value="${exam.assignedQuestionsCount}"/></strong> questions
                                                    </div>
                                                    <div class="small text-secondary">
                                                        <c:out value="${exam.assignedMarksTotal}"/> / <c:out value="${exam.totalMarks}"/> Marks
                                                    </div>
                                                    <div class="progress bg-secondary mt-1" style="height: 4px; width: 100px;">
                                                        <div class="progress-bar ${exam.ready ? 'bg-success' : 'bg-warning'}"
                                                             style="width: ${exam.totalMarks > 0 ? (exam.assignedMarksTotal * 100 / exam.totalMarks) : 0}%;"></div>
                                                    </div>
                                                </td>
                                                <td class="text-end">
                                                    <a href="${pageContext.request.contextPath}/teacher/examinations?action=manage&id=${exam.id}"
                                                       class="btn btn-outline-info btn-sm">
                                                        <i class="bi bi-gear-fill me-1"></i> Manage Questions
                                                    </a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="7" class="text-center py-5 text-muted">
                                                <i class="bi bi-journal-x fs-1 opacity-50 d-block mb-2"></i>
                                                <p class="mb-0">No examinations found for your assigned subjects.</p>
                                                <small class="text-secondary">Administrators configure examination schedules.</small>
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Pagination -->
                <c:if test="${totalPages > 1}">
                    <nav aria-label="Exam Pagination" class="d-flex justify-content-between align-items-center">
                        <span class="small text-secondary">
                            Page <c:out value="${currentPage}"/> of <c:out value="${totalPages}"/> (<c:out value="${totalCount}"/> examinations)
                        </span>
                        <ul class="pagination pagination-sm m-0">
                            <li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
                                <a class="page-link bg-dark border-secondary text-secondary"
                                   href="${pageContext.request.contextPath}/teacher/examinations?page=${currentPage - 1}&search=${search}&status=${selectedStatus}">
                                    &laquo; Previous
                                </a>
                            </li>
                            <c:forEach begin="1" end="${totalPages}" var="p">
                                <li class="page-item ${p == currentPage ? 'active' : ''}">
                                    <a class="page-link ${p == currentPage ? 'bg-primary border-primary text-white' : 'bg-dark border-secondary text-secondary'}"
                                       href="${pageContext.request.contextPath}/teacher/examinations?page=${p}&search=${search}&status=${selectedStatus}">
                                        <c:out value="${p}"/>
                                    </a>
                                </li>
                            </c:forEach>
                            <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                                <a class="page-link bg-dark border-secondary text-secondary"
                                   href="${pageContext.request.contextPath}/teacher/examinations?page=${currentPage + 1}&search=${search}&status=${selectedStatus}">
                                    Next &raquo;
                                </a>
                            </li>
                        </ul>
                    </nav>
                </c:if>

            </div>
        </main>
    </div>

    <!-- Bootstrap 5 JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-YvpcrYf0tY3lHB60NNkmXc4s9bIOgUxi8T/jzmLe3zxj8NxO8yVMnN5Z2X5ZZzEB"
            crossorigin="anonymous"></script>

    <!-- Sidebar Toggle Script -->
    <script>
        const sidebarToggle = document.getElementById('sidebarToggle');
        const adminSidebar = document.getElementById('adminSidebar');
        const sidebarBackdrop = document.getElementById('sidebarBackdrop');

        if (sidebarToggle && adminSidebar && sidebarBackdrop) {
            sidebarToggle.addEventListener('click', () => {
                adminSidebar.classList.toggle('show');
                sidebarBackdrop.classList.toggle('show');
            });
            sidebarBackdrop.addEventListener('click', () => {
                adminSidebar.classList.remove('show');
                sidebarBackdrop.classList.remove('show');
            });
        }
    </script>
</body>
</html>

