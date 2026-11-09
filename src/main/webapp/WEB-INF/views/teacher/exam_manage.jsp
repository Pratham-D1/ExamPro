<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Exam Questions | ExamPro</title>

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
                            <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/teacher/examinations">Examinations</a></li>
                            <li class="breadcrumb-item active" aria-current="page">Manage Questions</li>
                        </ol>
                    </nav>
                </div>
                <div class="ep-topbar-actions">
                    <a href="${pageContext.request.contextPath}/teacher/examinations" class="btn btn-outline-secondary btn-sm">
                        <i class="bi bi-arrow-left me-1"></i> Back to Examinations
                    </a>
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

                <!-- Examination Header & Specifications -->
                <div class="card bg-dark border-secondary p-4 mb-4">
                    <div class="d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-3 mb-3">
                        <div>
                            <span class="badge ${exam.statusBadgeClass} me-2"><c:out value="${exam.status}"/></span>
                            <span class="badge bg-primary font-monospace me-2"><c:out value="${exam.subjectCode}"/></span>
                            <span class="text-white-50 small"><c:out value="${exam.subjectName}"/></span>
                            <h1 class="h3 fw-bold text-white mt-2 mb-1"><c:out value="${exam.title}"/></h1>
                            <p class="text-secondary small mb-0"><c:out value="${exam.description}"/></p>
                        </div>
                        <div class="text-md-end">
                            <c:choose>
                                <c:when test="${exam.ready}">
                                    <span class="badge bg-success fs-6 px-3 py-2">
                                        <i class="bi bi-check2-circle me-1"></i> Examination Ready
                                    </span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-warning text-dark fs-6 px-3 py-2">
                                        <i class="bi bi-hourglass-split me-1"></i> Question Allocation Pending
                                    </span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <!-- Readiness Breakdown Stats -->
                    <div class="row g-3 pt-3 border-top border-secondary">
                        <div class="col-6 col-md-3">
                            <span class="text-secondary small d-block">Scheduled Window</span>
                            <span class="text-white small fw-medium">
                                <c:out value="${exam.formattedStartTime}"/> &ndash; <c:out value="${exam.formattedEndTime}"/>
                            </span>
                        </div>
                        <div class="col-6 col-md-3">
                            <span class="text-secondary small d-block">Duration &amp; Passing</span>
                            <span class="text-white small fw-medium">
                                <c:out value="${exam.durationMinutes}"/> mins &bull; Pass: <c:out value="${exam.passingMarks}"/> Marks
                            </span>
                        </div>
                        <div class="col-6 col-md-3">
                            <span class="text-secondary small d-block">Allocated Questions</span>
                            <span class="text-white small fw-bold">
                                <c:out value="${exam.assignedQuestionsCount}"/> Questions
                            </span>
                        </div>
                        <div class="col-6 col-md-3">
                            <span class="text-secondary small d-block">Allocated Marks</span>
                            <div class="d-flex align-items-center gap-2">
                                <span class="fs-6 fw-bold ${exam.ready ? 'text-success' : 'text-warning'}">
                                    <c:out value="${exam.assignedMarksTotal}"/> / <c:out value="${exam.totalMarks}"/>
                                </span>
                            </div>
                        </div>
                    </div>

                    <!-- Progress Bar -->
                    <div class="progress bg-secondary mt-3" style="height: 8px;">
                        <div class="progress-bar ${exam.ready ? 'bg-success' : 'bg-warning'}"
                             role="progressbar"
                             style="width: ${exam.totalMarks > 0 ? (exam.assignedMarksTotal * 100 / exam.totalMarks) : 0}%;"></div>
                    </div>
                    <c:if test="${not exam.ready}">
                        <small class="text-warning mt-2 d-block">
                            <i class="bi bi-info-circle me-1"></i> Need <strong><c:out value="${exam.remainingMarks}"/></strong> more marks allocated to match the required exam total (<c:out value="${exam.totalMarks}"/>).
                        </small>
                    </c:if>
                </div>

                <!-- ══════════════════════════════════════════════════════
                     SECTION 1: CURRENTLY ASSIGNED QUESTIONS
                ══════════════════════════════════════════════════════ -->
                <div class="card bg-dark border-secondary mb-4">
                    <div class="card-header bg-transparent border-secondary d-flex justify-content-between align-items-center py-3">
                        <h2 class="h5 fw-bold text-white mb-0">
                            <i class="bi bi-check2-square text-success me-2"></i> Currently Assigned Questions (<c:out value="${fn:length(assignedQuestions)}"/>)
                        </h2>
                    </div>
                    <div class="table-responsive">
                        <table class="table table-dark table-hover align-middle mb-0">
                            <thead>
                                <tr class="text-secondary small border-secondary">
                                    <th style="width: 5%;">#</th>
                                    <th style="width: 50%;">Question Statement</th>
                                    <th style="width: 15%;">Topic</th>
                                    <th style="width: 10%;">Difficulty</th>
                                    <th style="width: 10%;">Marks</th>
                                    <th style="width: 10%;" class="text-end">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty assignedQuestions}">
                                        <c:forEach var="eq" items="${assignedQuestions}" varStatus="status">
                                            <tr>
                                                <td class="text-muted small"><c:out value="${status.index + 1}"/></td>
                                                <td>
                                                    <div class="fw-semibold text-white mb-1">
                                                        <c:choose>
                                                            <c:when test="${fn:length(eq.questionText) > 120}">
                                                                <c:out value="${fn:substring(eq.questionText, 0, 120)}"/>...
                                                            </c:when>
                                                            <c:otherwise>
                                                                <c:out value="${eq.questionText}"/>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </div>
                                                    <span class="text-muted small">Correct: <code>Option <c:out value="${eq.correctOptionKey}"/></code></span>
                                                </td>
                                                <td><span class="badge bg-secondary"><c:out value="${eq.topic}"/></span></td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${eq.difficulty == 'EASY'}"><span class="badge mcq-badge-difficulty-easy">EASY</span></c:when>
                                                        <c:when test="${eq.difficulty == 'MEDIUM'}"><span class="badge mcq-badge-difficulty-medium">MEDIUM</span></c:when>
                                                        <c:otherwise><span class="badge mcq-badge-difficulty-hard">HARD</span></c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="text-white fw-bold"><c:out value="${eq.marks}"/></td>
                                                <td class="text-end">
                                                    <form method="post" action="${pageContext.request.contextPath}/teacher/examinations" class="d-inline">
                                                        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                                                        <input type="hidden" name="action" value="removeQuestion">
                                                        <input type="hidden" name="examId" value="${exam.id}">
                                                        <input type="hidden" name="questionId" value="${eq.questionId}">
                                                        <button type="submit" class="btn btn-outline-danger btn-sm"
                                                                ${exam.ongoing || exam.completed || exam.cancelled ? 'disabled' : ''}
                                                                title="Remove from examination">
                                                            <i class="bi bi-dash-circle me-1"></i> Remove
                                                        </button>
                                                    </form>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="6" class="text-center py-4 text-muted">
                                                No questions currently assigned to this examination. Pick questions from your question bank below.
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- ══════════════════════════════════════════════════════
                     SECTION 2: AVAILABLE QUESTIONS FROM QUESTION BANK
                ══════════════════════════════════════════════════════ -->
                <div class="card bg-dark border-secondary mb-4">
                    <div class="card-header bg-transparent border-secondary d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2 py-3">
                        <div>
                            <h2 class="h5 fw-bold text-white mb-0">
                                <i class="bi bi-collection-fill text-accent me-2"></i> Available Questions in Question Bank
                            </h2>
                            <small class="text-secondary">Questions in subject <strong class="text-white"><c:out value="${exam.subjectCode}"/></strong> authored by you</small>
                        </div>
                        <a href="${pageContext.request.contextPath}/teacher/questions" class="btn btn-outline-info btn-sm">
                            <i class="bi bi-plus-circle me-1"></i> Create More Questions
                        </a>
                    </div>

                    <!-- Search & Filter bar for available questions -->
                    <div class="p-3 border-bottom border-secondary bg-black">
                        <form method="get" action="${pageContext.request.contextPath}/teacher/examinations" class="row g-2 align-items-center">
                            <input type="hidden" name="action" value="manage">
                            <input type="hidden" name="id" value="${exam.id}">

                            <div class="col-md-6">
                                <input type="text" name="qSearch" class="form-control form-control-sm bg-dark text-white border-secondary"
                                       placeholder="Filter by question text or topic..." value="<c:out value='${qSearch}'/>">
                            </div>
                            <div class="col-md-3">
                                <select name="difficulty" class="form-select form-select-sm bg-dark text-white border-secondary">
                                    <option value="ALL" ${selectedDifficulty == 'ALL' ? 'selected' : ''}>All Difficulties</option>
                                    <option value="EASY" ${selectedDifficulty == 'EASY' ? 'selected' : ''}>EASY</option>
                                    <option value="MEDIUM" ${selectedDifficulty == 'MEDIUM' ? 'selected' : ''}>MEDIUM</option>
                                    <option value="HARD" ${selectedDifficulty == 'HARD' ? 'selected' : ''}>HARD</option>
                                </select>
                            </div>
                            <div class="col-md-3 d-flex gap-2">
                                <button type="submit" class="btn btn-outline-info btn-sm">Filter</button>
                                <a href="${pageContext.request.contextPath}/teacher/examinations?action=manage&id=${exam.id}" class="btn btn-outline-secondary btn-sm">Reset</a>
                            </div>
                        </form>
                    </div>

                    <div class="table-responsive">
                        <table class="table table-dark table-hover align-middle mb-0">
                            <thead>
                                <tr class="text-secondary small border-secondary">
                                    <th style="width: 5%;">#</th>
                                    <th style="width: 50%;">Question Statement</th>
                                    <th style="width: 15%;">Topic</th>
                                    <th style="width: 10%;">Difficulty</th>
                                    <th style="width: 10%;">Marks</th>
                                    <th style="width: 10%;" class="text-end">Assign</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty availableQuestions}">
                                        <c:forEach var="aq" items="${availableQuestions}" varStatus="status">
                                            <tr>
                                                <td class="text-muted small"><c:out value="${status.index + 1}"/></td>
                                                <td>
                                                    <div class="fw-semibold text-white mb-1">
                                                        <c:choose>
                                                            <c:when test="${fn:length(aq.questionText) > 120}">
                                                                <c:out value="${fn:substring(aq.questionText, 0, 120)}"/>...
                                                            </c:when>
                                                            <c:otherwise>
                                                                <c:out value="${aq.questionText}"/>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </div>
                                                    <span class="text-muted small">Correct: <code>Option <c:out value="${aq.correctOptionKey}"/></code></span>
                                                </td>
                                                <td><span class="badge bg-secondary"><c:out value="${aq.topic}"/></span></td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${aq.difficulty == 'EASY'}"><span class="badge mcq-badge-difficulty-easy">EASY</span></c:when>
                                                        <c:when test="${aq.difficulty == 'MEDIUM'}"><span class="badge mcq-badge-difficulty-medium">MEDIUM</span></c:when>
                                                        <c:otherwise><span class="badge mcq-badge-difficulty-hard">HARD</span></c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="text-white fw-bold"><c:out value="${aq.marks}"/></td>
                                                <td class="text-end">
                                                    <form method="post" action="${pageContext.request.contextPath}/teacher/examinations" class="d-inline">
                                                        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                                                        <input type="hidden" name="action" value="addQuestion">
                                                        <input type="hidden" name="examId" value="${exam.id}">
                                                        <input type="hidden" name="questionId" value="${aq.id}">
                                                        <button type="submit" class="btn btn-primary btn-sm"
                                                                style="background: #a371f7; border-color: #a371f7;"
                                                                ${exam.ongoing || exam.completed || exam.cancelled ? 'disabled' : ''}>
                                                            <i class="bi bi-plus-lg me-1"></i> Add to Exam
                                                        </button>
                                                    </form>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="6" class="text-center py-4 text-muted">
                                                No additional available questions for this subject in your question bank.
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>

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

