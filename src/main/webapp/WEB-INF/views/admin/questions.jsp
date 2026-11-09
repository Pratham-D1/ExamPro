<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Question Bank Management | ExamPro Admin</title>

    <!-- Bootstrap 5 CSS & Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
</head>
<body class="ep-admin-body">

    <!-- Mobile Sidebar Backdrop -->
    <div class="ep-sidebar-backdrop" id="sidebarBackdrop" aria-hidden="true"></div>

    <div class="ep-admin-wrapper">

        <!-- Unified Sidebar -->
        <jsp:include page="sidebar.jsp">
            <jsp:param name="active" value="questions"/>
        </jsp:include>

        <!-- Main Content Area -->
        <div class="ep-admin-main">

            <!-- Topbar -->
            <header class="ep-admin-topbar">
                <div class="d-flex align-items-center gap-3">
                    <button class="btn btn-outline-secondary btn-sm d-lg-none" id="sidebarToggle" type="button" aria-label="Toggle sidebar navigation">
                        <i class="bi bi-list fs-5"></i>
                    </button>
                    <nav aria-label="breadcrumb">
                        <ol class="ep-topbar-breadcrumb list-unstyled m-0">
                            <li><a href="${pageContext.request.contextPath}/admin/dashboard"><i class="bi bi-house-door me-1"></i>Home</a></li>
                            <li class="text-muted">/</li>
                            <li><a href="${pageContext.request.contextPath}/admin/dashboard">Admin</a></li>
                            <li class="text-muted">/</li>
                            <li class="active" aria-current="page">Question Bank</li>
                        </ol>
                    </nav>
                </div>
                <div class="ep-topbar-actions d-flex align-items-center">
                    <jsp:include page="/WEB-INF/views/common/notif_bell.jsp"/>
                    <div class="d-flex align-items-center gap-2 ps-2 border-start border-secondary">
                        <span class="badge badge-role-admin d-none d-sm-inline-block">ADMIN</span>
                        <span class="text-white small fw-semibold text-truncate" style="max-width: 140px;">
                            <c:out value="${not empty sessionScope.userName ? sessionScope.userName : currentUser.fullName}"/>
                        </span>
                    </div>
                </div>
            </header>

            <!-- Dashboard Content Container -->
            <div class="ep-admin-content container-fluid p-4">

                <!-- Page Header -->
                <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
                    <div>
                        <h1 class="h3 fw-bold text-white mb-1">
                            <i class="bi bi-patch-question-fill text-accent me-2"></i>Institutional Question Bank
                        </h1>
                        <p class="text-muted mb-0 small">
                            Central repository of assessment questions authored across all academic subjects and faculty members.
                        </p>
                    </div>
                    <div>
                        <span class="badge bg-dark border border-secondary text-light px-3 py-2 fs-6">
                            <i class="bi bi-collection-fill me-1 text-accent"></i>
                            Total Questions: <strong class="text-white"><c:out value="${totalCount}"/></strong>
                        </span>
                    </div>
                </div>

                <!-- Flash Messages -->
                <c:if test="${not empty flashSuccess}">
                    <div class="alert alert-success alert-dismissible fade show d-flex align-items-center mb-4" role="alert">
                        <i class="bi bi-check-circle-fill me-2 fs-5"></i>
                        <div><c:out value="${flashSuccess}"/></div>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:if>
                <c:if test="${not empty flashError}">
                    <div class="alert alert-danger alert-dismissible fade show d-flex align-items-center mb-4" role="alert">
                        <i class="bi bi-exclamation-triangle-fill me-2 fs-5"></i>
                        <div><c:out value="${flashError}"/></div>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:if>

                <!-- Filter & Search Toolbar Card -->
                <div class="card bg-dark border-secondary mb-4 shadow-sm">
                    <div class="card-body p-3">
                        <form method="get" action="${pageContext.request.contextPath}/admin/questions" class="row g-2 align-items-end">
                            <!-- Keyword Search -->
                            <div class="col-md-3 col-sm-6">
                                <label class="form-label text-muted small mb-1"><i class="bi bi-search me-1"></i>Search</label>
                                <input type="text" name="search" class="form-control form-control-sm bg-secondary text-white border-secondary"
                                       placeholder="Keywords, topic, or author..." value="<c:out value='${search}'/>">
                            </div>

                            <!-- Subject Filter -->
                            <div class="col-md-3 col-sm-6">
                                <label class="form-label text-muted small mb-1"><i class="bi bi-book me-1"></i>Subject</label>
                                <select name="subjectId" class="form-select form-select-sm bg-secondary text-white border-secondary">
                                    <option value="">All Academic Subjects</option>
                                    <c:forEach var="sub" items="${subjects}">
                                        <option value="${sub.id}" ${selectedSubjectId == sub.id ? 'selected' : ''}>
                                            <c:out value="${sub.code}"/> – <c:out value="${sub.name}"/>
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <!-- Faculty Filter -->
                            <div class="col-md-3 col-sm-6">
                                <label class="form-label text-muted small mb-1"><i class="bi bi-person-video3 me-1"></i>Faculty Author</label>
                                <select name="teacherId" class="form-select form-select-sm bg-secondary text-white border-secondary">
                                    <option value="">All Faculty Authors</option>
                                    <c:forEach var="t" items="${teachers}">
                                        <option value="${t.id}" ${selectedTeacherId == t.id ? 'selected' : ''}>
                                            <c:out value="${t.fullName}"/>
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <!-- Difficulty Filter -->
                            <div class="col-md-2 col-sm-4">
                                <label class="form-label text-muted small mb-1"><i class="bi bi-speedometer2 me-1"></i>Difficulty</label>
                                <select name="difficulty" class="form-select form-select-sm bg-secondary text-white border-secondary">
                                    <option value="">All Difficulties</option>
                                    <option value="EASY" ${selectedDifficulty == 'EASY' ? 'selected' : ''}>Easy</option>
                                    <option value="MEDIUM" ${selectedDifficulty == 'MEDIUM' ? 'selected' : ''}>Medium</option>
                                    <option value="HARD" ${selectedDifficulty == 'HARD' ? 'selected' : ''}>Hard</option>
                                </select>
                            </div>

                            <!-- Filter & Reset Buttons -->
                            <div class="col-md-1 col-sm-2 d-flex gap-1">
                                <button type="submit" class="btn btn-accent btn-sm w-100" title="Apply Filters">
                                    <i class="bi bi-funnel-fill"></i>
                                </button>
                                <a href="${pageContext.request.contextPath}/admin/questions" class="btn btn-outline-secondary btn-sm" title="Reset Filters">
                                    <i class="bi bi-x-circle"></i>
                                </a>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- Questions Table Card -->
                <div class="card bg-dark border-secondary shadow-sm">
                    <div class="table-responsive">
                        <table class="table table-dark table-hover table-striped align-middle mb-0">
                            <thead>
                                <tr class="text-muted small text-uppercase" style="border-bottom: 2px solid var(--ep-bg-tertiary);">
                                    <th style="width: 50px;">#</th>
                                    <th>Subject</th>
                                    <th style="width: 38%;">Question Statement</th>
                                    <th>Author</th>
                                    <th>Topic</th>
                                    <th>Difficulty</th>
                                    <th>Marks</th>
                                    <th>Linked Exams</th>
                                    <th class="text-end" style="width: 110px;">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty questions}">
                                        <c:forEach var="q" items="${questions}">
                                            <tr>
                                                <td class="text-muted small">#<c:out value="${q.id}"/></td>
                                                <td>
                                                    <span class="badge bg-secondary text-light">
                                                        <c:out value="${q.subjectCode}"/>
                                                    </span>
                                                    <div class="small text-muted text-truncate" style="max-width: 140px;">
                                                        <c:out value="${q.subjectName}"/>
                                                    </div>
                                                </td>
                                                <td>
                                                    <div class="fw-medium text-white text-truncate" style="max-width: 380px;" title="<c:out value='${q.questionText}'/>">
                                                        <c:out value="${q.questionText}"/>
                                                    </div>
                                                    <c:if test="${not empty q.explanation}">
                                                        <span class="text-muted small fst-italic text-truncate d-block" style="max-width: 380px;">
                                                            <i class="bi bi-info-circle me-1"></i><c:out value="${q.explanation}"/>
                                                        </span>
                                                    </c:if>
                                                </td>
                                                <td>
                                                    <div class="text-light small fw-semibold">
                                                        <i class="bi bi-person me-1 text-muted"></i><c:out value="${q.teacherName}"/>
                                                    </div>
                                                </td>
                                                <td>
                                                    <span class="badge bg-dark border border-secondary text-light small">
                                                        <c:out value="${q.topic}"/>
                                                    </span>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${q.difficulty == 'EASY'}">
                                                            <span class="badge bg-success-subtle text-success border border-success-subtle">Easy</span>
                                                        </c:when>
                                                        <c:when test="${q.difficulty == 'HARD'}">
                                                            <span class="badge bg-danger-subtle text-danger border border-danger-subtle">Hard</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge bg-warning-subtle text-warning border border-warning-subtle">Medium</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="fw-semibold text-accent"><c:out value="${q.marks}"/></td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${q.assignedExamCount > 0}">
                                                            <span class="badge bg-info-subtle text-info border border-info-subtle">
                                                                <i class="bi bi-link-45deg me-1"></i><c:out value="${q.assignedExamCount}"/> Exam(s)
                                                            </span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge bg-secondary-subtle text-muted">Unassigned</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="text-end">
                                                    <div class="btn-group btn-group-sm">
                                                        <!-- View Options Modal Trigger -->
                                                        <button type="button" class="btn btn-outline-info btn-sm view-question-btn"
                                                                title="Inspect Question Details"
                                                                data-id="${q.id}"
                                                                data-text="<c:out value='${q.questionText}'/>"
                                                                data-subject="<c:out value='${q.subjectCode} - ${q.subjectName}'/>"
                                                                data-author="<c:out value='${q.teacherName}'/>"
                                                                data-topic="<c:out value='${q.topic}'/>"
                                                                data-difficulty="<c:out value='${q.difficulty}'/>"
                                                                data-marks="<c:out value='${q.marks}'/>"
                                                                data-explanation="<c:out value='${q.explanation}'/>"
                                                                data-exams="<c:out value='${q.assignedExamCount}'/>">
                                                            <i class="bi bi-eye"></i>
                                                        </button>

                                                        <!-- Delete Button (Only active if not assigned to any exam) -->
                                                        <c:choose>
                                                            <c:when test="${q.assignedExamCount == 0}">
                                                                <button type="button" class="btn btn-outline-danger btn-sm delete-question-btn"
                                                                        title="Permanently Delete Question"
                                                                        data-id="${q.id}"
                                                                        data-text="<c:out value='${q.questionText}'/>">
                                                                    <i class="bi bi-trash3"></i>
                                                                </button>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <button type="button" class="btn btn-outline-secondary btn-sm"
                                                                        disabled title="Cannot delete: Question is linked to active examinations">
                                                                    <i class="bi bi-lock-fill"></i>
                                                                </button>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="9" class="text-center py-5 text-muted">
                                                <i class="bi bi-patch-question fs-1 d-block mb-2 text-secondary"></i>
                                                <div class="fw-semibold">No questions found matching your filter criteria.</div>
                                                <small>Try selecting a different subject or clearing your search filter.</small>
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>

                    <!-- Pagination -->
                    <c:if test="${totalPages > 1}">
                        <div class="card-footer bg-dark border-secondary d-flex justify-content-between align-items-center py-2 px-3">
                            <span class="text-muted small">
                                Page <strong class="text-white">${currentPage}</strong> of <strong class="text-white">${totalPages}</strong> (${totalCount} Total)
                            </span>
                            <ul class="pagination pagination-sm m-0">
                                <li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
                                    <a class="page-link bg-secondary text-white border-secondary"
                                       href="${pageContext.request.contextPath}/admin/questions?page=${currentPage - 1}&search=${search}&subjectId=${selectedSubjectId}&teacherId=${selectedTeacherId}&difficulty=${selectedDifficulty}&topic=${selectedTopic}">
                                        &laquo; Prev
                                    </a>
                                </li>
                                <c:forEach var="p" begin="1" end="${totalPages}">
                                    <c:if test="${p >= currentPage - 2 && p <= currentPage + 2}">
                                        <li class="page-item ${currentPage == p ? 'active' : ''}">
                                            <a class="page-link ${currentPage == p ? 'bg-primary border-primary text-white' : 'bg-secondary text-white border-secondary'}"
                                               href="${pageContext.request.contextPath}/admin/questions?page=${p}&search=${search}&subjectId=${selectedSubjectId}&teacherId=${selectedTeacherId}&difficulty=${selectedDifficulty}&topic=${selectedTopic}">
                                                ${p}
                                            </a>
                                        </li>
                                    </c:if>
                                </c:forEach>
                                <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                                    <a class="page-link bg-secondary text-white border-secondary"
                                       href="${pageContext.request.contextPath}/admin/questions?page=${currentPage + 1}&search=${search}&subjectId=${selectedSubjectId}&teacherId=${selectedTeacherId}&difficulty=${selectedDifficulty}&topic=${selectedTopic}">
                                        Next &raquo;
                                    </a>
                                </li>
                            </ul>
                        </div>
                    </c:if>
                </div>

            </div><!-- /ep-admin-content -->
        </div><!-- /ep-admin-main -->
    </div><!-- /ep-admin-wrapper -->

    <!-- Modal: View Question Details -->
    <div class="modal fade" id="viewQuestionModal" tabindex="-1" aria-labelledby="viewQuestionModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered modal-lg">
            <div class="modal-content bg-dark text-white border-secondary">
                <div class="modal-header border-secondary">
                    <h5 class="modal-title" id="viewQuestionModalLabel">
                        <i class="bi bi-patch-question-fill text-accent me-2"></i>Question Details <span id="modalQuestionId" class="text-muted fs-6"></span>
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <span class="badge bg-secondary me-2" id="modalSubject"></span>
                        <span class="badge me-2" id="modalDifficulty"></span>
                        <span class="badge bg-dark border border-secondary text-light me-2" id="modalTopic"></span>
                        <span class="badge bg-primary text-white" id="modalMarks"></span>
                    </div>

                    <div class="card bg-secondary bg-opacity-25 border-secondary p-3 mb-3">
                        <label class="text-muted small mb-1">Question Statement:</label>
                        <div class="fs-6 fw-semibold text-white" id="modalQuestionText"></div>
                    </div>

                    <div class="card bg-secondary bg-opacity-25 border-secondary p-3 mb-3" id="modalExplanationCard">
                        <label class="text-muted small mb-1"><i class="bi bi-lightbulb-fill text-warning me-1"></i>Explanation / Solution:</label>
                        <div class="small text-light" id="modalExplanation"></div>
                    </div>

                    <div class="row g-2 text-muted small">
                        <div class="col-sm-6">
                            <i class="bi bi-person-fill me-1"></i>Author: <span class="text-white" id="modalAuthor"></span>
                        </div>
                        <div class="col-sm-6 text-sm-end">
                            <i class="bi bi-link-45deg me-1"></i>Linked Examinations: <span class="text-white" id="modalExams"></span>
                        </div>
                    </div>
                </div>
                <div class="modal-footer border-secondary">
                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Close</button>
                </div>
            </div>
        </div>
    </div>

    <!-- Modal: Delete Confirmation -->
    <div class="modal fade" id="deleteQuestionModal" tabindex="-1" aria-labelledby="deleteQuestionModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content bg-dark text-white border-danger">
                <div class="modal-header border-danger">
                    <h5 class="modal-title text-danger" id="deleteQuestionModalLabel">
                        <i class="bi bi-exclamation-triangle-fill me-2"></i>Confirm Question Deletion
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <form method="post" action="${pageContext.request.contextPath}/admin/questions">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="questionId" id="deleteQuestionId">

                    <div class="modal-body">
                        <p class="mb-2">Are you sure you want to permanently delete this question from the Question Bank?</p>
                        <div class="card bg-secondary bg-opacity-25 border-secondary p-2 mb-3">
                            <span class="small text-white fst-italic" id="deleteQuestionText"></span>
                        </div>
                        <p class="text-warning small mb-0">
                            <i class="bi bi-shield-exclamation me-1"></i> This action is irreversible and will be logged in the system audit trail.
                        </p>
                    </div>
                    <div class="modal-footer border-danger">
                        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-danger btn-sm">
                            <i class="bi bi-trash3 me-1"></i>Delete Permanently
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Bootstrap 5 JS Bundle & Event Script -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-YvpcrYf0tY3lHB60NNkmXc4s9bIOgUxi8T/jzmLe3zxj8NxO8yVMnN5Z2X5ZZzEB" crossorigin="anonymous"></script>
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            // Mobile sidebar toggle
            const toggle = document.getElementById('sidebarToggle');
            const sidebar = document.getElementById('adminSidebar');
            const backdrop = document.getElementById('sidebarBackdrop');
            if (toggle && sidebar) {
                toggle.addEventListener('click', function () {
                    sidebar.classList.toggle('show');
                    if (backdrop) backdrop.classList.toggle('show');
                });
            }
            if (backdrop) {
                backdrop.addEventListener('click', function () {
                    sidebar.classList.remove('show');
                    backdrop.classList.remove('show');
                });
            }

            // View Question Modal
            const viewModal = new bootstrap.Modal(document.getElementById('viewQuestionModal'));
            document.querySelectorAll('.view-question-btn').forEach(btn => {
                btn.addEventListener('click', function () {
                    document.getElementById('modalQuestionId').textContent = '#' + this.dataset.id;
                    document.getElementById('modalSubject').textContent = this.dataset.subject;
                    document.getElementById('modalQuestionText').textContent = this.dataset.text;
                    document.getElementById('modalAuthor').textContent = this.dataset.author;
                    document.getElementById('modalTopic').textContent = this.dataset.topic;
                    document.getElementById('modalMarks').textContent = this.dataset.marks + ' Mark(s)';
                    document.getElementById('modalExams').textContent = this.dataset.exams;

                    const diff = this.dataset.difficulty;
                    const diffBadge = document.getElementById('modalDifficulty');
                    diffBadge.textContent = diff;
                    if (diff === 'EASY') {
                        diffBadge.className = 'badge bg-success-subtle text-success border border-success-subtle me-2';
                    } else if (diff === 'HARD') {
                        diffBadge.className = 'badge bg-danger-subtle text-danger border border-danger-subtle me-2';
                    } else {
                        diffBadge.className = 'badge bg-warning-subtle text-warning border border-warning-subtle me-2';
                    }

                    const expCard = document.getElementById('modalExplanationCard');
                    const expText = document.getElementById('modalExplanation');
                    if (this.dataset.explanation && this.dataset.explanation.trim() !== '') {
                        expText.textContent = this.dataset.explanation;
                        expCard.style.display = 'block';
                    } else {
                        expCard.style.display = 'none';
                    }

                    viewModal.show();
                });
            });

            // Delete Question Modal
            const deleteModal = new bootstrap.Modal(document.getElementById('deleteQuestionModal'));
            document.querySelectorAll('.delete-question-btn').forEach(btn => {
                btn.addEventListener('click', function () {
                    document.getElementById('deleteQuestionId').value = this.dataset.id;
                    document.getElementById('deleteQuestionText').textContent = this.dataset.text;
                    deleteModal.show();
                });
            });
        });
    </script>
</body>
</html>

