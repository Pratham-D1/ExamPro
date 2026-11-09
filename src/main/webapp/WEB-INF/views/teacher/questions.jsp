<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Question Bank Management | ExamPro</title>

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
            <jsp:param name="active" value="questions"/>
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
                            <li class="breadcrumb-item active" aria-current="page">Question Bank</li>
                        </ol>
                    </nav>
                </div>
                <div class="ep-topbar-actions">
                    <button type="button" class="btn btn-primary btn-sm px-3" data-bs-toggle="modal" data-bs-target="#addQuestionModal"
                            style="background: #a371f7; border-color: #a371f7;">
                        <i class="bi bi-plus-circle me-1"></i> Add Question
                    </button>
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

                <!-- Page Header & Summary -->
                <div class="d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center mb-4 gap-3">
                    <div>
                        <h1 class="h4 fw-bold text-white mb-1">
                            <i class="bi bi-patch-question-fill text-accent me-2"></i> Question Bank &amp; MCQ Management
                        </h1>
                        <p class="text-secondary small mb-0">
                            Create, organize, and maintain your library of multiple-choice questions for examination delivery.
                        </p>
                    </div>
                    <div>
                        <span class="badge bg-secondary px-3 py-2 fs-6">
                            Total: <strong class="text-white"><c:out value="${totalCount}"/></strong> questions
                        </span>
                    </div>
                </div>

                <!-- Search & Filters Toolbar -->
                <div class="card bg-dark border-secondary p-3 mb-4">
                    <form method="get" action="${pageContext.request.contextPath}/teacher/questions" class="row g-2 align-items-center">
                        <!-- Search Query -->
                        <div class="col-md-4">
                            <div class="input-group input-group-sm">
                                <span class="input-group-text bg-secondary border-secondary text-secondary">
                                    <i class="bi bi-search"></i>
                                </span>
                                <input type="text" name="search" class="form-control bg-dark text-white border-secondary"
                                       placeholder="Search statement or topic..." value="<c:out value='${search}'/>">
                            </div>
                        </div>

                        <!-- Subject Filter -->
                        <div class="col-md-3">
                            <select name="subjectId" class="form-select form-select-sm bg-dark text-white border-secondary">
                                <option value="">-- All Assigned Subjects --</option>
                                <c:forEach var="sub" items="${assignedSubjects}">
                                    <option value="${sub.id}" ${selectedSubjectId == sub.id ? 'selected' : ''}>
                                        <c:out value="${sub.code}"/> — <c:out value="${sub.name}"/>
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <!-- Difficulty Filter -->
                        <div class="col-md-2">
                            <select name="difficulty" class="form-select form-select-sm bg-dark text-white border-secondary">
                                <option value="ALL" ${selectedDifficulty == 'ALL' ? 'selected' : ''}>All Difficulties</option>
                                <option value="EASY" ${selectedDifficulty == 'EASY' ? 'selected' : ''}>Easy</option>
                                <option value="MEDIUM" ${selectedDifficulty == 'MEDIUM' ? 'selected' : ''}>Medium</option>
                                <option value="HARD" ${selectedDifficulty == 'HARD' ? 'selected' : ''}>Hard</option>
                            </select>
                        </div>

                        <!-- Actions -->
                        <div class="col-md-3 d-flex gap-2 justify-content-md-end">
                            <button type="submit" class="btn btn-outline-info btn-sm">
                                <i class="bi bi-funnel me-1"></i> Filter
                            </button>
                            <a href="${pageContext.request.contextPath}/teacher/questions" class="btn btn-outline-secondary btn-sm">
                                Reset
                            </a>
                        </div>
                    </form>
                </div>

                <!-- Questions Table -->
                <div class="card bg-dark border-secondary mb-4">
                    <div class="table-responsive">
                        <table class="table table-dark table-hover align-middle mb-0">
                            <thead>
                                <tr class="text-secondary small border-secondary">
                                    <th style="width: 5%;">#</th>
                                    <th style="width: 40%;">Question Statement</th>
                                    <th style="width: 15%;">Subject</th>
                                    <th style="width: 10%;">Difficulty</th>
                                    <th style="width: 8%;">Marks</th>
                                    <th style="width: 10%;">Exams Linked</th>
                                    <th style="width: 12%;" class="text-end">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty questions}">
                                        <c:forEach var="q" items="${questions}" varStatus="status">
                                            <tr>
                                                <td class="text-muted small"><c:out value="${status.index + 1}"/></td>
                                                <td>
                                                    <div class="fw-semibold text-white mb-1">
                                                        <c:choose>
                                                            <c:when test="${fn:length(q.questionText) > 100}">
                                                                <c:out value="${fn:substring(q.questionText, 0, 100)}"/>...
                                                            </c:when>
                                                            <c:otherwise>
                                                                <c:out value="${q.questionText}"/>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </div>
                                                    <div class="d-flex align-items-center gap-2">
                                                        <span class="badge bg-secondary small" style="font-size: 0.7rem;">
                                                            <i class="bi bi-bookmark me-1"></i><c:out value="${q.topic}"/>
                                                        </span>
                                                        <span class="text-muted small">Correct: <code>Option <c:out value="${q.correctOptionKey}"/></code></span>
                                                    </div>
                                                </td>
                                                <td>
                                                    <span class="badge bg-primary text-uppercase font-monospace"><c:out value="${q.subjectCode}"/></span>
                                                    <div class="small text-muted text-truncate" style="max-width: 130px;"><c:out value="${q.subjectName}"/></div>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${q.difficulty == 'EASY'}">
                                                            <span class="badge mcq-badge-difficulty-easy">EASY</span>
                                                        </c:when>
                                                        <c:when test="${q.difficulty == 'MEDIUM'}">
                                                            <span class="badge mcq-badge-difficulty-medium">MEDIUM</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge mcq-badge-difficulty-hard">HARD</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="text-white fw-medium">
                                                    <c:out value="${q.marks}"/>
                                                </td>
                                                <td>
                                                    <span class="badge ${q.assignedExamCount > 0 ? 'bg-info text-dark' : 'bg-secondary'}">
                                                        <c:out value="${q.assignedExamCount}"/> exams
                                                    </span>
                                                </td>
                                                <td class="text-end">
                                                    <div class="btn-group btn-group-sm">
                                                        <!-- View Details Button -->
                                                        <button type="button" class="btn btn-outline-info" data-bs-toggle="modal"
                                                                data-bs-target="#viewModal_${q.id}" title="Preview Question">
                                                            <i class="bi bi-eye"></i>
                                                        </button>

                                                        <!-- Edit Button -->
                                                        <button type="button" class="btn btn-outline-warning" data-bs-toggle="modal"
                                                                data-bs-target="#editModal_${q.id}" title="Edit Question">
                                                            <i class="bi bi-pencil-square"></i>
                                                        </button>

                                                        <!-- Delete Button -->
                                                        <button type="button" class="btn btn-outline-danger" data-bs-toggle="modal"
                                                                data-bs-target="#deleteModal_${q.id}" title="Delete Question"
                                                                ${q.assignedExamCount > 0 ? 'disabled' : ''}>
                                                            <i class="bi bi-trash"></i>
                                                        </button>
                                                    </div>
                                                </td>
                                            </tr>

                                            <!-- ═══════════════════════════════════════
                                                 MODAL 1: VIEW DETAILS
                                            ═══════════════════════════════════════ -->
                                            <div class="modal fade" id="viewModal_${q.id}" tabindex="-1" aria-labelledby="viewModalLabel_${q.id}" aria-hidden="true">
                                                <div class="modal-dialog modal-dialog-centered modal-lg">
                                                    <div class="modal-content bg-dark border-secondary text-white">
                                                        <div class="modal-header border-secondary">
                                                            <h5 class="modal-title" id="viewModalLabel_${q.id}">
                                                                <i class="bi bi-patch-question text-accent me-2"></i> Question Details
                                                            </h5>
                                                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                                                        </div>
                                                        <div class="modal-body">
                                                            <div class="d-flex flex-wrap gap-2 mb-3">
                                                                <span class="badge bg-primary font-monospace"><c:out value="${q.subjectCode}"/> - <c:out value="${q.subjectName}"/></span>
                                                                <span class="badge bg-secondary"><i class="bi bi-bookmark me-1"></i><c:out value="${q.topic}"/></span>
                                                                <span class="badge ${q.difficulty == 'EASY' ? 'mcq-badge-difficulty-easy' : (q.difficulty == 'MEDIUM' ? 'mcq-badge-difficulty-medium' : 'mcq-badge-difficulty-hard')}">
                                                                    <c:out value="${q.difficulty}"/>
                                                                </span>
                                                                <span class="badge bg-info text-dark"><c:out value="${q.marks}"/> Marks</span>
                                                            </div>

                                                            <div class="p-3 rounded bg-black border border-secondary mb-3">
                                                                <div class="text-secondary small fw-medium mb-1">Question Statement:</div>
                                                                <div class="fs-6 fw-semibold text-white"><c:out value="${q.questionText}"/></div>
                                                            </div>

                                                            <div class="text-secondary small fw-medium mb-2">Multiple Choice Options:</div>
                                                            <div class="mb-3">
                                                                <c:forEach var="opt" items="${q.options}">
                                                                    <div class="mcq-option-box ${opt.correct ? 'correct' : ''}">
                                                                        <div class="mcq-option-key"><c:out value="${opt.optionKey}"/></div>
                                                                        <div class="flex-grow-1 text-white"><c:out value="${opt.optionText}"/></div>
                                                                        <c:if test="${opt.correct}">
                                                                            <span class="badge bg-success ms-auto"><i class="bi bi-check2 me-1"></i>Correct Answer</span>
                                                                        </c:if>
                                                                    </div>
                                                                </c:forEach>
                                                            </div>

                                                            <c:if test="${not empty q.explanation}">
                                                                <div class="p-3 rounded border border-secondary" style="background: rgba(79, 142, 247, 0.08);">
                                                                    <div class="text-accent small fw-bold mb-1">
                                                                        <i class="bi bi-info-circle me-1"></i> Solution Explanation:
                                                                    </div>
                                                                    <div class="small text-secondary"><c:out value="${q.explanation}"/></div>
                                                                </div>
                                                            </c:if>
                                                        </div>
                                                        <div class="modal-footer border-secondary">
                                                            <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Close</button>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>

                                            <!-- ═══════════════════════════════════════
                                                 MODAL 2: EDIT QUESTION
                                            ═══════════════════════════════════════ -->
                                            <div class="modal fade" id="editModal_${q.id}" tabindex="-1" aria-labelledby="editModalLabel_${q.id}" aria-hidden="true">
                                                <div class="modal-dialog modal-dialog-centered modal-lg">
                                                    <div class="modal-content bg-dark border-secondary text-white">
                                                        <form method="post" action="${pageContext.request.contextPath}/teacher/questions">
                                                            <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                                                            <input type="hidden" name="action" value="update">
                                                            <input type="hidden" name="questionId" value="${q.id}">

                                                            <div class="modal-header border-secondary">
                                                                <h5 class="modal-title" id="editModalLabel_${q.id}">
                                                                    <i class="bi bi-pencil-square text-warning me-2"></i> Edit Question
                                                                </h5>
                                                                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                                                            </div>

                                                            <div class="modal-body">
                                                                <div class="row g-3 mb-3">
                                                                    <!-- Subject -->
                                                                    <div class="col-md-6">
                                                                        <label class="form-label small text-secondary">Subject <span class="text-danger">*</span></label>
                                                                        <select name="subjectId" class="form-select form-select-sm bg-dark text-white border-secondary" required>
                                                                            <c:forEach var="sub" items="${assignedSubjects}">
                                                                                <option value="${sub.id}" ${q.subjectId == sub.id ? 'selected' : ''}>
                                                                                    <c:out value="${sub.code}"/> — <c:out value="${sub.name}"/>
                                                                                </option>
                                                                            </c:forEach>
                                                                        </select>
                                                                    </div>
                                                                    <!-- Topic -->
                                                                    <div class="col-md-6">
                                                                        <label class="form-label small text-secondary">Topic / Unit</label>
                                                                        <input type="text" name="topic" class="form-control form-control-sm bg-dark text-white border-secondary"
                                                                               value="<c:out value='${q.topic}'/>" required>
                                                                    </div>
                                                                </div>

                                                                <div class="row g-3 mb-3">
                                                                    <!-- Difficulty -->
                                                                    <div class="col-md-6">
                                                                        <label class="form-label small text-secondary">Difficulty Level <span class="text-danger">*</span></label>
                                                                        <select name="difficulty" class="form-select form-select-sm bg-dark text-white border-secondary" required>
                                                                            <option value="EASY" ${q.difficulty == 'EASY' ? 'selected' : ''}>EASY</option>
                                                                            <option value="MEDIUM" ${q.difficulty == 'MEDIUM' ? 'selected' : ''}>MEDIUM</option>
                                                                            <option value="HARD" ${q.difficulty == 'HARD' ? 'selected' : ''}>HARD</option>
                                                                        </select>
                                                                    </div>
                                                                    <!-- Marks -->
                                                                    <div class="col-md-6">
                                                                        <label class="form-label small text-secondary">Marks <span class="text-danger">*</span></label>
                                                                        <input type="number" step="0.5" min="0.5" name="marks" class="form-control form-control-sm bg-dark text-white border-secondary"
                                                                               value="<c:out value='${q.marks}'/>" required>
                                                                    </div>
                                                                </div>

                                                                <!-- Statement -->
                                                                <div class="mb-3">
                                                                    <label class="form-label small text-secondary">Question Statement <span class="text-danger">*</span></label>
                                                                    <textarea name="questionText" class="form-control bg-dark text-white border-secondary" rows="3" required><c:out value="${q.questionText}"/></textarea>
                                                                </div>

                                                                <!-- Options & Correct Selector -->
                                                                <div class="text-secondary small fw-medium mb-2">Options &amp; Correct Answer:</div>
                                                                <div class="p-3 rounded border border-secondary mb-3" style="background: var(--ep-bg-primary);">
                                                                    <!-- Option A -->
                                                                    <div class="input-group input-group-sm mb-2">
                                                                        <span class="input-group-text bg-secondary border-secondary text-white font-monospace">A</span>
                                                                        <input type="text" name="optionA" class="form-control bg-dark text-white border-secondary"
                                                                               value="<c:out value='${q.getOptionByKey("A") != null ? q.getOptionByKey("A").optionText : ""}'/>" required>
                                                                        <div class="input-group-text bg-secondary border-secondary">
                                                                            <input class="form-check-input mt-0 me-1" type="radio" name="correctOption" value="A"
                                                                                   ${q.correctOptionKey == 'A' ? 'checked' : ''} required>
                                                                            <label class="small text-white mb-0">Correct</label>
                                                                        </div>
                                                                    </div>

                                                                    <!-- Option B -->
                                                                    <div class="input-group input-group-sm mb-2">
                                                                        <span class="input-group-text bg-secondary border-secondary text-white font-monospace">B</span>
                                                                        <input type="text" name="optionB" class="form-control bg-dark text-white border-secondary"
                                                                               value="<c:out value='${q.getOptionByKey("B") != null ? q.getOptionByKey("B").optionText : ""}'/>" required>
                                                                        <div class="input-group-text bg-secondary border-secondary">
                                                                            <input class="form-check-input mt-0 me-1" type="radio" name="correctOption" value="B"
                                                                                   ${q.correctOptionKey == 'B' ? 'checked' : ''} required>
                                                                            <label class="small text-white mb-0">Correct</label>
                                                                        </div>
                                                                    </div>

                                                                    <!-- Option C -->
                                                                    <div class="input-group input-group-sm mb-2">
                                                                        <span class="input-group-text bg-secondary border-secondary text-white font-monospace">C</span>
                                                                        <input type="text" name="optionC" class="form-control bg-dark text-white border-secondary"
                                                                               value="<c:out value='${q.getOptionByKey("C") != null ? q.getOptionByKey("C").optionText : ""}'/>" required>
                                                                        <div class="input-group-text bg-secondary border-secondary">
                                                                            <input class="form-check-input mt-0 me-1" type="radio" name="correctOption" value="C"
                                                                                   ${q.correctOptionKey == 'C' ? 'checked' : ''} required>
                                                                            <label class="small text-white mb-0">Correct</label>
                                                                        </div>
                                                                    </div>

                                                                    <!-- Option D -->
                                                                    <div class="input-group input-group-sm">
                                                                        <span class="input-group-text bg-secondary border-secondary text-white font-monospace">D</span>
                                                                        <input type="text" name="optionD" class="form-control bg-dark text-white border-secondary"
                                                                               value="<c:out value='${q.getOptionByKey("D") != null ? q.getOptionByKey("D").optionText : ""}'/>" required>
                                                                        <div class="input-group-text bg-secondary border-secondary">
                                                                            <input class="form-check-input mt-0 me-1" type="radio" name="correctOption" value="D"
                                                                                   ${q.correctOptionKey == 'D' ? 'checked' : ''} required>
                                                                            <label class="small text-white mb-0">Correct</label>
                                                                        </div>
                                                                    </div>
                                                                </div>

                                                                <!-- Explanation -->
                                                                <div class="mb-2">
                                                                    <label class="form-label small text-secondary">Explanation (Optional)</label>
                                                                    <textarea name="explanation" class="form-control form-control-sm bg-dark text-white border-secondary" rows="2"><c:out value="${q.explanation}"/></textarea>
                                                                </div>
                                                            </div>

                                                            <div class="modal-footer border-secondary">
                                                                <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                                                                <button type="submit" class="btn btn-warning btn-sm px-3">
                                                                    <i class="bi bi-save me-1"></i> Update Question
                                                                </button>
                                                            </div>
                                                        </form>
                                                    </div>
                                                </div>
                                            </div>

                                            <!-- ═══════════════════════════════════════
                                                 MODAL 3: DELETE CONFIRMATION
                                            ═══════════════════════════════════════ -->
                                            <div class="modal fade" id="deleteModal_${q.id}" tabindex="-1" aria-labelledby="deleteModalLabel_${q.id}" aria-hidden="true">
                                                <div class="modal-dialog modal-dialog-centered">
                                                    <div class="modal-content bg-dark border-secondary text-white">
                                                        <form method="post" action="${pageContext.request.contextPath}/teacher/questions">
                                                            <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                                                            <input type="hidden" name="action" value="delete">
                                                            <input type="hidden" name="questionId" value="${q.id}">

                                                            <div class="modal-header border-secondary">
                                                                <h5 class="modal-title text-danger" id="deleteModalLabel_${q.id}">
                                                                    <i class="bi bi-exclamation-triangle-fill me-2"></i> Confirm Question Deletion
                                                                </h5>
                                                                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                                                            </div>
                                                            <div class="modal-body">
                                                                <p class="text-secondary mb-2">Are you sure you want to permanently delete this question?</p>
                                                                <div class="p-3 bg-black rounded border border-secondary small text-white">
                                                                    <c:out value="${q.questionText}"/>
                                                                </div>
                                                                <small class="text-muted d-block mt-2">
                                                                    This action cannot be undone. Questions linked to examinations cannot be deleted.
                                                                </small>
                                                            </div>
                                                            <div class="modal-footer border-secondary">
                                                                <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                                                                <button type="submit" class="btn btn-danger btn-sm px-3">
                                                                    <i class="bi bi-trash me-1"></i> Delete Permanently
                                                                </button>
                                                            </div>
                                                        </form>
                                                    </div>
                                                </div>
                                            </div>

                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="7" class="text-center py-5 text-muted">
                                                <i class="bi bi-patch-question fs-1 opacity-50 d-block mb-2"></i>
                                                <p class="mb-1">No questions found matching your criteria.</p>
                                                <small class="text-secondary">Click the "+ Add Question" button to start populating your question bank.</small>
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
                    <nav aria-label="Question Pagination" class="d-flex justify-content-between align-items-center">
                        <span class="small text-secondary">
                            Page <c:out value="${currentPage}"/> of <c:out value="${totalPages}"/> (<c:out value="${totalCount}"/> questions)
                        </span>
                        <ul class="pagination pagination-sm m-0">
                            <li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
                                <a class="page-link bg-dark border-secondary text-secondary"
                                   href="${pageContext.request.contextPath}/teacher/questions?page=${currentPage - 1}&search=${search}&subjectId=${selectedSubjectId}&difficulty=${selectedDifficulty}">
                                    &laquo; Previous
                                </a>
                            </li>
                            <c:forEach begin="1" end="${totalPages}" var="p">
                                <li class="page-item ${p == currentPage ? 'active' : ''}">
                                    <a class="page-link ${p == currentPage ? 'bg-primary border-primary text-white' : 'bg-dark border-secondary text-secondary'}"
                                       href="${pageContext.request.contextPath}/teacher/questions?page=${p}&search=${search}&subjectId=${selectedSubjectId}&difficulty=${selectedDifficulty}">
                                        <c:out value="${p}"/>
                                    </a>
                                </li>
                            </c:forEach>
                            <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                                <a class="page-link bg-dark border-secondary text-secondary"
                                   href="${pageContext.request.contextPath}/teacher/questions?page=${currentPage + 1}&search=${search}&subjectId=${selectedSubjectId}&difficulty=${selectedDifficulty}">
                                    Next &raquo;
                                </a>
                            </li>
                        </ul>
                    </nav>
                </c:if>

            </div>
        </main>
    </div>

    <!-- ═══════════════════════════════════════
         MODAL: CREATE NEW QUESTION
    ═══════════════════════════════════════ -->
    <div class="modal fade" id="addQuestionModal" tabindex="-1" aria-labelledby="addQuestionModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered modal-lg">
            <div class="modal-content bg-dark border-secondary text-white">
                <form method="post" action="${pageContext.request.contextPath}/teacher/questions">
                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>">
                    <input type="hidden" name="action" value="create">

                    <div class="modal-header border-secondary">
                        <h5 class="modal-title" id="addQuestionModalLabel">
                            <i class="bi bi-plus-circle text-accent me-2"></i> Add New MCQ Question
                        </h5>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>

                    <div class="modal-body">
                        <c:choose>
                            <c:when test="${empty assignedSubjects}">
                                <div class="alert alert-warning mb-0" role="alert">
                                    <i class="bi bi-exclamation-triangle-fill me-2"></i>
                                    <strong>No assigned subjects:</strong> You have not been assigned to any subjects by an administrator yet. Questions can only be created for assigned subjects.
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="row g-3 mb-3">
                                    <!-- Subject -->
                                    <div class="col-md-6">
                                        <label class="form-label small text-secondary">Assigned Subject <span class="text-danger">*</span></label>
                                        <select name="subjectId" class="form-select form-select-sm bg-dark text-white border-secondary" required>
                                            <option value="">-- Choose Subject --</option>
                                            <c:forEach var="sub" items="${assignedSubjects}">
                                                <option value="${sub.id}">
                                                    <c:out value="${sub.code}"/> — <c:out value="${sub.name}"/>
                                                </option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                    <!-- Topic -->
                                    <div class="col-md-6">
                                        <label class="form-label small text-secondary">Topic / Unit <span class="text-danger">*</span></label>
                                        <input type="text" name="topic" class="form-control form-control-sm bg-dark text-white border-secondary"
                                               placeholder="e.g. Object-Oriented Programming" required>
                                    </div>
                                </div>

                                <div class="row g-3 mb-3">
                                    <!-- Difficulty -->
                                    <div class="col-md-6">
                                        <label class="form-label small text-secondary">Difficulty Level <span class="text-danger">*</span></label>
                                        <select name="difficulty" class="form-select form-select-sm bg-dark text-white border-secondary" required>
                                            <option value="EASY">EASY</option>
                                            <option value="MEDIUM" selected>MEDIUM</option>
                                            <option value="HARD">HARD</option>
                                        </select>
                                    </div>
                                    <!-- Marks -->
                                    <div class="col-md-6">
                                        <label class="form-label small text-secondary">Marks <span class="text-danger">*</span></label>
                                        <input type="number" step="0.5" min="0.5" name="marks" class="form-control form-control-sm bg-dark text-white border-secondary"
                                               value="1.0" required>
                                    </div>
                                </div>

                                <!-- Statement -->
                                <div class="mb-3">
                                    <label class="form-label small text-secondary">Question Statement <span class="text-danger">*</span></label>
                                    <textarea name="questionText" class="form-control bg-dark text-white border-secondary" rows="3"
                                              placeholder="Enter the full question text here..." required></textarea>
                                </div>

                                <!-- MCQ Options -->
                                <div class="text-secondary small fw-medium mb-2">
                                    Multiple Choice Options &amp; Correct Answer Key <span class="text-danger">*</span>
                                </div>
                                <div class="p-3 rounded border border-secondary mb-3" style="background: var(--ep-bg-primary);">
                                    <!-- Option A -->
                                    <div class="input-group input-group-sm mb-2">
                                        <span class="input-group-text bg-secondary border-secondary text-white font-monospace">A</span>
                                        <input type="text" name="optionA" class="form-control bg-dark text-white border-secondary"
                                               placeholder="Enter Option A statement" required>
                                        <div class="input-group-text bg-secondary border-secondary">
                                            <input class="form-check-input mt-0 me-1" type="radio" name="correctOption" value="A" required>
                                            <label class="small text-white mb-0">Correct</label>
                                        </div>
                                    </div>

                                    <!-- Option B -->
                                    <div class="input-group input-group-sm mb-2">
                                        <span class="input-group-text bg-secondary border-secondary text-white font-monospace">B</span>
                                        <input type="text" name="optionB" class="form-control bg-dark text-white border-secondary"
                                               placeholder="Enter Option B statement" required>
                                        <div class="input-group-text bg-secondary border-secondary">
                                            <input class="form-check-input mt-0 me-1" type="radio" name="correctOption" value="B" required>
                                            <label class="small text-white mb-0">Correct</label>
                                        </div>
                                    </div>

                                    <!-- Option C -->
                                    <div class="input-group input-group-sm mb-2">
                                        <span class="input-group-text bg-secondary border-secondary text-white font-monospace">C</span>
                                        <input type="text" name="optionC" class="form-control bg-dark text-white border-secondary"
                                               placeholder="Enter Option C statement" required>
                                        <div class="input-group-text bg-secondary border-secondary">
                                            <input class="form-check-input mt-0 me-1" type="radio" name="correctOption" value="C" required>
                                            <label class="small text-white mb-0">Correct</label>
                                        </div>
                                    </div>

                                    <!-- Option D -->
                                    <div class="input-group input-group-sm">
                                        <span class="input-group-text bg-secondary border-secondary text-white font-monospace">D</span>
                                        <input type="text" name="optionD" class="form-control bg-dark text-white border-secondary"
                                               placeholder="Enter Option D statement" required>
                                        <div class="input-group-text bg-secondary border-secondary">
                                            <input class="form-check-input mt-0 me-1" type="radio" name="correctOption" value="D" required>
                                            <label class="small text-white mb-0">Correct</label>
                                        </div>
                                    </div>
                                </div>

                                <!-- Explanation -->
                                <div class="mb-2">
                                    <label class="form-label small text-secondary">Explanation (Optional)</label>
                                    <textarea name="explanation" class="form-control form-control-sm bg-dark text-white border-secondary" rows="2"
                                              placeholder="Provide context or explanation for why the selected answer is correct..."></textarea>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="modal-footer border-secondary">
                        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                        <c:if test="${not empty assignedSubjects}">
                            <button type="submit" class="btn btn-primary btn-sm px-3" style="background: #a371f7; border-color: #a371f7;">
                                <i class="bi bi-save me-1"></i> Save to Question Bank
                            </button>
                        </c:if>
                    </div>
                </form>
            </div>
        </div>
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

