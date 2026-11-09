<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Official Scorecard: <c:out value="${result.examTitle}"/> | ExamPro</title>
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
            <i class="bi bi-award-fill text-success me-2"></i> Official Candidate Examination Scorecard
        </div>
        <div>
            <button onclick="window.print()" class="btn btn-success btn-sm me-2" style="background: #2ea043; border-color: #2ea043;">
                <i class="bi bi-printer me-1"></i> Print / Save as PDF
            </button>
            <button onclick="window.close()" class="btn btn-outline-secondary btn-sm">
                Close Window
            </button>
        </div>
    </div>

    <div class="container p-4">

        <!-- Institutional Header -->
        <div class="d-flex justify-content-between align-items-center ep-report-header">
            <div class="d-flex align-items-center gap-3">
                <div style="width: 50px; height: 50px; background: #2ea043; color: #fff; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 1.75rem;">
                    <i class="bi bi-mortarboard-fill"></i>
                </div>
                <div>
                    <h2 class="ep-report-title mb-0">ExamPro Examination Board</h2>
                    <div class="ep-report-subtitle">Official Candidate Examination Transcript &amp; Solution Review</div>
                </div>
            </div>
            <div class="text-end small text-muted">
                <div>Candidate: <strong><c:out value="${currentUser.fullName}"/></strong></div>
                <div>Reg No: <code><c:out value="${result.registrationNo != null ? result.registrationNo : 'N/A'}"/></code></div>
                <div>Submission Date: <strong><c:out value="${result.formattedSubmittedAt}"/></strong></div>
            </div>
        </div>

        <!-- Examination Summary Box -->
        <div class="card mb-4 border">
            <div class="card-body p-3">
                <div class="row align-items-center g-3">
                    <div class="col-8">
                        <span class="text-muted small">Examination:</span>
                        <h4 class="fw-bold mb-1"><c:out value="${result.examTitle}"/></h4>
                        <div class="small text-muted">
                            Course: <strong><c:out value="${result.subjectCode}"/> - <c:out value="${result.subjectName}"/></strong>
                        </div>
                    </div>
                    <div class="col-4 text-end">
                        <div class="h2 fw-bold mb-0 ${result.status == 'PASS' ? 'text-success' : 'text-danger'}">
                            <c:out value="${result.status}"/>
                        </div>
                        <div class="h5 fw-bold mb-0">
                            <fmt:formatNumber value="${result.marksObtained}" maxFractionDigits="1"/> / <c:out value="${result.totalMarks}"/>
                            (<fmt:formatNumber value="${result.percentage}" maxFractionDigits="1"/>%)
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Metrics Grid -->
        <div class="row g-2 text-center mb-4">
            <div class="col-3">
                <div class="p-2 border rounded">
                    <div class="small text-muted">Total Questions</div>
                    <div class="h5 fw-bold mb-0">${fn:length(reviewAnswers)}</div>
                </div>
            </div>
            <div class="col-3">
                <div class="p-2 border rounded">
                    <div class="small text-muted">Correct Answers</div>
                    <div class="h5 fw-bold text-success mb-0">${correctCount}</div>
                </div>
            </div>
            <div class="col-3">
                <div class="p-2 border rounded">
                    <div class="small text-muted">Incorrect Answers</div>
                    <div class="h5 fw-bold text-danger mb-0">${incorrectCount}</div>
                </div>
            </div>
            <div class="col-3">
                <div class="p-2 border rounded">
                    <div class="small text-muted">Unattempted</div>
                    <div class="h5 fw-bold text-secondary mb-0">${unattemptedCount}</div>
                </div>
            </div>
        </div>

        <!-- Question Breakdown -->
        <h6 class="fw-bold mb-2">Detailed Question Response Audit</h6>
        <div class="d-flex flex-column gap-2 mb-4">
            <c:forEach var="a" items="${reviewAnswers}">
                <div class="border rounded p-3 mb-2" style="page-break-inside: avoid;">
                    <div class="d-flex justify-content-between align-items-center mb-2">
                        <span class="fw-bold">Question ${a.questionNumber} (${a.difficulty} &bull; ${a.topic})</span>
                        <span class="fw-bold ${a.correct ? 'text-success' : 'text-danger'}">
                            <c:choose>
                                <c:when test="${empty a.selectedOptionKey}">Unattempted (0.0 / ${a.questionMarks})</c:when>
                                <c:when test="${a.correct}">Correct (+<fmt:formatNumber value="${a.marksAwarded}" maxFractionDigits="1"/> / ${a.questionMarks})</c:when>
                                <c:otherwise>Incorrect (0.0 / ${a.questionMarks})</c:otherwise>
                            </c:choose>
                        </span>
                    </div>

                    <div class="mb-2"><c:out value="${a.questionText}"/></div>

                    <div class="small">
                        <div class="text-muted">
                            Selected Option: <strong><c:out value="${a.selectedOptionKey != null ? a.selectedOptionKey : 'None'}"/></strong> &bull;
                            Official Correct Option: <strong class="text-success"><c:out value="${a.correctOptionKey}"/></strong>
                        </div>
                        <c:if test="${not empty a.explanation}">
                            <div class="mt-1 text-secondary">
                                <em>Solution Note: <c:out value="${a.explanation}"/></em>
                            </div>
                        </c:if>
                    </div>
                </div>
            </c:forEach>
        </div>

        <!-- Seal & Signatures -->
        <div class="ep-signature-section mt-5 pt-4">
            <div class="ep-signature-box">
                <strong>Student Candidate Signature</strong>
                <div class="small text-muted">Acknowledgement</div>
            </div>
            <div class="ep-signature-box">
                <strong>Controller of Examinations</strong>
                <div class="small text-muted">Official Verification Seal</div>
            </div>
        </div>

        <div class="ep-print-watermark">
            ExamPro Assessment System &bull; Official Transcript &bull; Verification Hash Validated
        </div>
    </div>

</body>
</html>

