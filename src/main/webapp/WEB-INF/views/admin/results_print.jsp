<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Official Results Report | ExamPro</title>
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
            <i class="bi bi-file-earmark-text text-primary me-2"></i> Official Examination Results Report
        </div>
        <div>
            <button onclick="window.print()" class="btn btn-primary btn-sm me-2">
                <i class="bi bi-printer me-1"></i> Print / Save as PDF
            </button>
            <button onclick="window.close()" class="btn btn-outline-secondary btn-sm">
                Close Window
            </button>
        </div>
    </div>

    <div class="container-fluid p-4">

        <!-- Institutional Header -->
        <div class="d-flex justify-content-between align-items-center ep-report-header">
            <div class="d-flex align-items-center gap-3">
                <div style="width: 50px; height: 50px; background: #0d6efd; color: #fff; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 1.75rem;">
                    <i class="bi bi-mortarboard-fill"></i>
                </div>
                <div>
                    <h2 class="ep-report-title mb-0">ExamPro Examination Board</h2>
                    <div class="ep-report-subtitle">Official Candidate Performance &amp; Evaluation Record</div>
                </div>
            </div>
            <div class="text-end small text-muted">
                <div>Generated: <strong><c:out value="${generatedDate}"/></strong></div>
                <div>Authorized By: <strong><c:out value="${currentUser.fullName}"/></strong> (Administrator)</div>
            </div>
        </div>

        <!-- Examination Summary Box -->
        <c:if test="${not empty selectedExam}">
            <div class="card mb-4 border">
                <div class="card-body p-3">
                    <div class="row g-3">
                        <div class="col-6 col-md-3">
                            <span class="text-muted small">Examination:</span>
                            <div class="fw-bold"><c:out value="${selectedExam.title}"/></div>
                        </div>
                        <div class="col-6 col-md-3">
                            <span class="text-muted small">Subject:</span>
                            <div class="fw-bold"><c:out value="${selectedExam.subjectCode}"/> - <c:out value="${selectedExam.subjectName}"/></div>
                        </div>
                        <div class="col-6 col-md-3">
                            <span class="text-muted small">Marks Criteria:</span>
                            <div class="fw-bold"><c:out value="${selectedExam.totalMarks}"/> Max (Pass: <c:out value="${selectedExam.passingMarks}"/>)</div>
                        </div>
                        <div class="col-6 col-md-3">
                            <span class="text-muted small">Publication State:</span>
                            <div class="fw-bold">${selectedExam.resultsPublished ? 'PUBLISHED' : 'UNPUBLISHED (CONFIDENTIAL)'}</div>
                        </div>
                    </div>
                </div>
            </div>
        </c:if>

        <!-- KPI Performance Grid -->
        <div class="row g-2 text-center mb-4">
            <div class="col-3">
                <div class="p-2 border rounded">
                    <div class="small text-muted">Total Submissions</div>
                    <div class="h5 fw-bold mb-0"><c:out value="${stats.totalEvaluatedAttempts}"/></div>
                </div>
            </div>
            <div class="col-3">
                <div class="p-2 border rounded">
                    <div class="small text-muted">Pass Count (Rate)</div>
                    <div class="h5 fw-bold text-success mb-0">
                        <c:out value="${stats.overallPassCount}"/> (<fmt:formatNumber value="${stats.overallPassPercentage}" maxFractionDigits="1"/>%)
                    </div>
                </div>
            </div>
            <div class="col-3">
                <div class="p-2 border rounded">
                    <div class="small text-muted">Fail Count (Rate)</div>
                    <div class="h5 fw-bold text-danger mb-0">
                        <c:out value="${stats.overallFailCount}"/> (<fmt:formatNumber value="${stats.overallFailPercentage}" maxFractionDigits="1"/>%)
                    </div>
                </div>
            </div>
            <div class="col-3">
                <div class="p-2 border rounded">
                    <div class="small text-muted">Average Score</div>
                    <div class="h5 fw-bold text-primary mb-0">
                        <fmt:formatNumber value="${stats.averageScore}" maxFractionDigits="1"/>%
                    </div>
                </div>
            </div>
        </div>

        <!-- Candidate Results Table -->
        <div class="table-responsive">
            <table class="table table-bordered align-middle mb-4">
                <thead class="table-light">
                    <tr>
                        <th style="width: 40px;">#</th>
                        <th>Candidate Name</th>
                        <th>Registration No</th>
                        <th>Examination</th>
                        <th>Subject</th>
                        <th class="text-center">Score</th>
                        <th class="text-center">Percentage</th>
                        <th class="text-center">Outcome</th>
                        <th>Submission Date</th>
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
                                    <td><c:out value="${res.subjectCode}"/></td>
                                    <td class="text-center fw-bold"><fmt:formatNumber value="${res.marksObtained}" maxFractionDigits="1"/> / <c:out value="${res.totalMarks}"/></td>
                                    <td class="text-center"><fmt:formatNumber value="${res.percentage}" maxFractionDigits="1"/>%</td>
                                    <td class="text-center fw-bold ${res.status == 'PASS' ? 'text-success' : 'text-danger'}">
                                        <c:out value="${res.status}"/>
                                    </td>
                                    <td class="small"><c:out value="${res.formattedSubmittedAt}"/></td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="9" class="text-center py-4 text-muted">No records found for this criteria.</td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>

        <!-- Institutional Signature Section -->
        <div class="ep-signature-section mt-5 pt-4">
            <div class="ep-signature-box">
                <strong>Controller of Examinations</strong>
                <div class="small text-muted">Signature &amp; Stamp</div>
            </div>
            <div class="ep-signature-box">
                <strong>Academic Registrar</strong>
                <div class="small text-muted">Verification Seal</div>
            </div>
        </div>

        <div class="ep-print-watermark">
            ExamPro Assessment System &bull; Confidential &bull; Generated <c:out value="${generatedDate}"/>
        </div>
    </div>

</body>
</html>

