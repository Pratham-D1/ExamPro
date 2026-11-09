<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Faculty Grade Sheet | ExamPro</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
    <style>
        body { background: #ffffff; color: #000000; }
        .print-toolbar {
            background: #f8f9fa;
            border-bottom: 1px solid #dee2e6;
            padding: 10px 20px;
        }
        @media print {
            .print-toolbar { display: none !important; }
        }
    </style>
</head>
<body>

    <!-- On-screen print toolbar -->
    <div class="print-toolbar d-flex justify-content-between align-items-center no-print">
        <div class="fw-semibold">
            <i class="bi bi-file-earmark-bar-graph text-purple me-2"></i> Faculty Grade Sheet &amp; Item Analysis Report
        </div>
        <div>
            <button onclick="window.print()" class="btn btn-primary btn-sm me-2" style="background: #6f42c1; border-color: #6f42c1;">
                <i class="bi bi-printer me-1"></i> Print / Save as PDF
            </button>
            <button onclick="window.close()" class="btn btn-outline-secondary btn-sm">
                Close Window
            </button>
        </div>
    </div>

    <div class="container-fluid p-4">

        <!-- Header -->
        <div class="d-flex justify-content-between align-items-center ep-report-header">
            <div class="d-flex align-items-center gap-3">
                <div style="width: 50px; height: 50px; background: #6f42c1; color: #fff; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 1.75rem;">
                    <i class="bi bi-mortarboard-fill"></i>
                </div>
                <div>
                    <h2 class="ep-report-title mb-0">ExamPro Academic Faculty</h2>
                    <div class="ep-report-subtitle">Examination Evaluation &amp; Question Item Performance Roster</div>
                </div>
            </div>
            <div class="text-end small text-muted">
                <div>Faculty Instructor: <strong><c:out value="${currentUser.fullName}"/></strong></div>
                <div>Generated: <strong><c:out value="${generatedDate}"/></strong></div>
            </div>
        </div>

        <!-- Examination Header Card if Selected -->
        <c:if test="${not empty selectedExam}">
            <div class="card mb-3 border">
                <div class="card-body p-3">
                    <div class="row g-2">
                        <div class="col-4">
                            <span class="text-muted small">Examination:</span>
                            <div class="fw-bold"><c:out value="${selectedExam.title}"/></div>
                        </div>
                        <div class="col-4">
                            <span class="text-muted small">Course / Subject:</span>
                            <div class="fw-bold"><c:out value="${selectedExam.subjectCode}"/> - <c:out value="${selectedExam.subjectName}"/></div>
                        </div>
                        <div class="col-4">
                            <span class="text-muted small">Passing Threshold:</span>
                            <div class="fw-bold"><c:out value="${selectedExam.passingMarks}"/> / <c:out value="${selectedExam.totalMarks}"/> Marks</div>
                        </div>
                    </div>
                </div>
            </div>
        </c:if>

        <!-- KPI Metrics -->
        <div class="row g-2 text-center mb-4">
            <div class="col-3">
                <div class="p-2 border rounded">
                    <div class="small text-muted">Total Candidates</div>
                    <div class="h5 fw-bold mb-0"><c:out value="${summary.totalAttempts}"/></div>
                </div>
            </div>
            <div class="col-3">
                <div class="p-2 border rounded">
                    <div class="small text-muted">Passed</div>
                    <div class="h5 fw-bold text-success mb-0"><c:out value="${summary.passedCount}"/></div>
                </div>
            </div>
            <div class="col-3">
                <div class="p-2 border rounded">
                    <div class="small text-muted">Failed</div>
                    <div class="h5 fw-bold text-danger mb-0"><c:out value="${summary.failedCount}"/></div>
                </div>
            </div>
            <div class="col-3">
                <div class="p-2 border rounded">
                    <div class="small text-muted">Class Average</div>
                    <div class="h5 fw-bold text-primary mb-0"><fmt:formatNumber value="${summary.avgPercentage}" maxFractionDigits="1"/>%</div>
                </div>
            </div>
        </div>

        <!-- Question Accuracy Table if available -->
        <c:if test="${not empty questionPerformances}">
            <h6 class="fw-bold mb-2">Question Item Analysis</h6>
            <table class="table table-bordered mb-4 small">
                <thead class="table-light">
                    <tr>
                        <th style="width: 40px;">#</th>
                        <th>Topic</th>
                        <th>Difficulty</th>
                        <th class="text-center">Points</th>
                        <th class="text-center">Attempts</th>
                        <th class="text-center">Correct</th>
                        <th class="text-center">Accuracy (%)</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="qp" items="${questionPerformances}">
                        <tr>
                            <td>${qp.questionNumber}</td>
                            <td><c:out value="${qp.topic}"/></td>
                            <td><c:out value="${qp.difficulty}"/></td>
                            <td class="text-center"><c:out value="${qp.questionMarks}"/></td>
                            <td class="text-center"><c:out value="${qp.totalAttempts}"/></td>
                            <td class="text-center"><c:out value="${qp.correctCount}"/></td>
                            <td class="text-center fw-bold"><fmt:formatNumber value="${qp.accuracyPercentage}" maxFractionDigits="1"/>%</td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:if>

        <!-- Candidate Results Roster -->
        <h6 class="fw-bold mb-2">Student Performance Roster</h6>
        <table class="table table-bordered align-middle mb-4">
            <thead class="table-light">
                <tr>
                    <th style="width: 40px;">#</th>
                    <th>Candidate Name</th>
                    <th>Registration No</th>
                    <th>Examination</th>
                    <th class="text-center">Score</th>
                    <th class="text-center">Percentage</th>
                    <th class="text-center">Outcome</th>
                    <th>Submitted At</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty results}">
                        <c:forEach var="res" items="${results}" varStatus="status">
                            <tr>
                                <td>${status.count}</td>
                                <td class="fw-bold"><c:out value="${res.studentName}"/></td>
                                <td><code><c:out value="${res.registrationNo != null ? res.registrationNo : 'N/A'}"/></code></td>
                                <td><c:out value="${res.examTitle}"/></td>
                                <td class="text-center fw-bold"><fmt:formatNumber value="${res.marksObtained}" maxFractionDigits="1"/> / <c:out value="${res.totalMarks}"/></td>
                                <td class="text-center"><fmt:formatNumber value="${res.percentage}" maxFractionDigits="1"/>%</td>
                                <td class="text-center fw-bold ${res.status == 'PASS' ? 'text-success' : 'text-danger'}"><c:out value="${res.status}"/></td>
                                <td class="small"><c:out value="${res.formattedSubmittedAt}"/></td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="8" class="text-center py-3 text-muted">No student evaluations found.</td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>

        <!-- Signatures -->
        <div class="ep-signature-section mt-5 pt-4">
            <div class="ep-signature-box">
                <strong>Course Faculty Instructor</strong>
                <div class="small text-muted">Signature &amp; Date</div>
            </div>
            <div class="ep-signature-box">
                <strong>Department Head (HOD)</strong>
                <div class="small text-muted">Approval &amp; Endorsement</div>
            </div>
        </div>

        <div class="ep-print-watermark">
            ExamPro Faculty Portal &bull; Generated <c:out value="${generatedDate}"/>
        </div>
    </div>

</body>
</html>

