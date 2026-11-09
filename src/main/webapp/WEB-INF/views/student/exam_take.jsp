<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Live Examination: <c:out value="${attempt.examTitle}"/> | ExamPro</title>

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
    <style>
        body {
            user-select: none;
            -webkit-user-select: none;
            background-color: var(--ep-bg-primary);
        }
        .exam-save-indicator {
            font-size: 0.8rem;
            transition: opacity 0.3s ease;
        }
    </style>
</head>
<body class="p-2 p-md-3">

    <div class="container-fluid px-lg-4 py-2">

        <!-- ═══════════════════════════════════════════════════════════
             TOP HEADER: Exam Title, Subject, Live Timer, and Actions
             ═══════════════════════════════════════════════════════════ -->
        <header class="exam-room-header mb-3">
            <div class="d-flex align-items-center gap-3">
                <div class="ep-sidebar-brand-icon" style="background: rgba(46, 160, 67, 0.15); color: #3fb950; border: 1px solid rgba(46, 160, 67, 0.3); width: 42px; height: 42px; border-radius: 10px; display: flex; align-items: center; justify-content: center;">
                    <i class="bi bi-mortarboard-fill fs-5"></i>
                </div>
                <div>
                    <h1 class="h5 fw-bold text-white mb-0 text-truncate" style="max-width: 480px;">
                        <c:out value="${attempt.examTitle}"/>
                    </h1>
                    <div class="small text-secondary d-flex align-items-center gap-2">
                        <span class="badge bg-secondary"><c:out value="${attempt.subjectCode}"/></span>
                        <span><c:out value="${attempt.subjectName}"/></span>
                        <span class="text-muted">&bull;</span>
                        <span class="text-white-50">Candidate: <strong class="text-white"><c:out value="${currentUser.fullName}"/></strong></span>
                    </div>
                </div>
            </div>

            <div class="d-flex align-items-center gap-3">
                <!-- Auto-save notification pill -->
                <div id="saveStatus" class="exam-save-indicator text-muted d-flex align-items-center gap-1 opacity-0">
                    <i class="bi bi-cloud-check text-success"></i>
                    <span>Saved</span>
                </div>

                <!-- Live Countdown Timer -->
                <div class="exam-timer-card timer-normal" id="timerBadge" title="Remaining Examination Time">
                    <i class="bi bi-clock-history"></i>
                    <span id="timerDisplay">--:--:--</span>
                </div>

                <!-- Submit Button -->
                <button type="button" class="btn btn-danger btn-sm px-3 py-2 fw-bold d-flex align-items-center gap-2"
                        data-bs-toggle="modal" data-bs-target="#submitModal">
                    <i class="bi bi-send-check"></i>
                    <span class="d-none d-sm-inline">Submit Exam</span>
                </button>
            </div>
        </header>

        <!-- ═══════════════════════════════════════════════════════════
             MAIN TEST WORKSPACE: Question Box (Left) & Palette (Right)
             ═══════════════════════════════════════════════════════════ -->
        <div class="row g-3">

            <!-- Question Interaction Area (Left 8-9 cols) -->
            <div class="col-lg-8 col-xl-9">
                <div class="take-question-box">

                    <!-- Questions Iteration Container -->
                    <div id="questionsContainer">
                        <c:forEach var="q" items="${questions}" varStatus="status">
                            <div class="question-view" id="qView_${status.index}"
                                 data-index="${status.index}"
                                 data-qid="${q.questionId}"
                                 style="${status.index == 0 ? 'display: block;' : 'display: none;'}">

                                <!-- Question Header Bar -->
                                <div class="d-flex justify-content-between align-items-center mb-3 pb-2 border-bottom border-secondary">
                                    <div class="d-flex align-items-center gap-2">
                                        <span class="badge bg-primary px-3 py-2 fw-bold fs-6">
                                            Question ${status.index + 1} of ${fn:length(questions)}
                                        </span>
                                        <c:if test="${not empty q.topic}">
                                            <span class="badge bg-secondary px-2 py-1 small">
                                                <i class="bi bi-tag me-1"></i> <c:out value="${q.topic}"/>
                                            </span>
                                        </c:if>
                                        <span class="badge ${q.difficulty == 'EASY' ? 'mcq-badge-difficulty-easy' : (q.difficulty == 'HARD' ? 'mcq-badge-difficulty-hard' : 'mcq-badge-difficulty-medium')} px-2 py-1 small">
                                            <c:out value="${q.difficulty}"/>
                                        </span>
                                    </div>

                                    <div class="d-flex align-items-center gap-2">
                                        <span class="badge bg-dark border border-secondary text-white px-2 py-1">
                                            +<c:out value="${q.marks}"/> Mark<c:if test="${q.marks > 1}">s</c:if>
                                        </span>
                                    </div>
                                </div>

                                <!-- Question Statement -->
                                <div class="take-question-text">
                                    <c:out value="${q.questionText}"/>
                                </div>

                                <!-- MCQ Options List -->
                                <div class="take-options-list mb-4">
                                    <c:forEach var="opt" items="${q.options}">
                                        <div class="take-option-item">
                                            <label class="take-option-label ${q.selectedOptionKey == opt.optionKey ? 'active-selected' : ''}"
                                                   for="opt_${q.questionId}_${opt.optionKey}">
                                                <input type="radio"
                                                       name="question_${q.questionId}"
                                                       id="opt_${q.questionId}_${opt.optionKey}"
                                                       value="${opt.optionKey}"
                                                       class="form-check-input d-none"
                                                       ${q.selectedOptionKey == opt.optionKey ? 'checked' : ''}
                                                       onchange="selectOption(${status.index}, '${q.questionId}', '${opt.optionKey}')">

                                                <span class="take-option-badge">
                                                    <c:out value="${opt.optionKey}"/>
                                                </span>
                                                <span class="take-option-text">
                                                    <c:out value="${opt.optionText}"/>
                                                </span>
                                            </label>
                                        </div>
                                    </c:forEach>
                                </div>

                            </div>
                        </c:forEach>
                    </div>

                    <!-- Bottom Question Controls Bar -->
                    <div class="pt-3 border-top border-secondary d-flex flex-wrap justify-content-between align-items-center gap-2">
                        <div class="d-flex align-items-center gap-2">
                            <!-- Previous Button -->
                            <button type="button" class="btn btn-outline-secondary btn-sm px-3" id="prevBtn" onclick="navQuestion(-1)">
                                <i class="bi bi-chevron-left me-1"></i> Previous
                            </button>

                            <!-- Next Button -->
                            <button type="button" class="btn btn-primary btn-sm px-3" id="nextBtn" onclick="navQuestion(1)">
                                Next <i class="bi bi-chevron-right ms-1"></i>
                            </button>
                        </div>

                        <div class="d-flex align-items-center gap-2">
                            <!-- Clear Answer Button -->
                            <button type="button" class="btn btn-outline-warning btn-sm" onclick="clearCurrentAnswer()">
                                <i class="bi bi-eraser me-1"></i> Clear Selection
                            </button>

                            <!-- Mark For Review Button -->
                            <button type="button" class="btn btn-outline-purple btn-sm" id="reviewBtn" onclick="toggleCurrentReview()"
                                    style="border-color: #8957e5; color: #a371f7;">
                                <i class="bi bi-bookmark me-1" id="reviewIcon"></i>
                                <span id="reviewText">Mark for Review</span>
                            </button>
                        </div>
                    </div>

                </div>
            </div>

            <!-- Question Palette Sidebar (Right 3-4 cols) -->
            <div class="col-lg-4 col-xl-3">
                <div class="question-palette-card">

                    <!-- Palette Header -->
                    <div class="d-flex justify-content-between align-items-center mb-3 pb-2 border-bottom border-secondary">
                        <h6 class="text-white fw-bold mb-0">
                            <i class="bi bi-grid-3x3-gap-fill text-success me-1"></i> Question Palette
                        </h6>
                        <span class="badge bg-secondary small">${fn:length(questions)} Total</span>
                    </div>

                    <!-- Legend Items -->
                    <div class="row g-2 mb-3 px-1">
                        <div class="col-6">
                            <div class="palette-legend-item">
                                <span class="palette-legend-swatch" style="background: #238636;"></span>
                                <span>Answered (<span id="countAnswered">${answeredCount}</span>)</span>
                            </div>
                        </div>
                        <div class="col-6">
                            <div class="palette-legend-item">
                                <span class="palette-legend-swatch" style="background: var(--ep-bg-primary); border-color: var(--ep-bg-tertiary);"></span>
                                <span>Unanswered (<span id="countUnanswered">${unansweredCount}</span>)</span>
                            </div>
                        </div>
                        <div class="col-6">
                            <div class="palette-legend-item">
                                <span class="palette-legend-swatch" style="background: #8957e5;"></span>
                                <span>For Review (<span id="countReview">${reviewCount}</span>)</span>
                            </div>
                        </div>
                        <div class="col-6">
                            <div class="palette-legend-item">
                                <span class="palette-legend-swatch" style="background: linear-gradient(135deg, #238636 50%, #8957e5 50%);"></span>
                                <span>Both</span>
                            </div>
                        </div>
                    </div>

                    <!-- Numbered Navigation Buttons Grid -->
                    <div class="question-palette-grid mb-3" id="paletteGrid">
                        <c:forEach var="q" items="${questions}" varStatus="status">
                            <c:set var="statusClass" value=""/>
                            <c:choose>
                                <c:when test="${not empty q.selectedOptionKey && q.isMarkedForReview}">
                                    <c:set var="statusClass" value="status-answered-review"/>
                                </c:when>
                                <c:when test="${not empty q.selectedOptionKey}">
                                    <c:set var="statusClass" value="status-answered"/>
                                </c:when>
                                <c:when test="${q.isMarkedForReview}">
                                    <c:set var="statusClass" value="status-review"/>
                                </c:when>
                            </c:choose>

                            <button type="button"
                                    class="palette-btn ${statusClass} ${status.index == 0 ? 'status-current' : ''}"
                                    id="paletteBtn_${status.index}"
                                    onclick="jumpToQuestion(${status.index})">
                                ${status.index + 1}
                            </button>
                        </c:forEach>
                    </div>

                    <!-- Summary & Quick Submit Button -->
                    <div class="p-3 rounded text-center" style="background: var(--ep-bg-primary); border: 1px solid var(--ep-bg-tertiary);">
                        <button type="button" class="btn btn-success w-100 fw-bold py-2"
                                style="background: #2ea043; border-color: #2ea043;"
                                data-bs-toggle="modal" data-bs-target="#submitModal">
                            <i class="bi bi-check2-circle me-1"></i> Review &amp; Submit
                        </button>
                        <small class="text-secondary d-block mt-2">
                            Time automatically submits at 00:00:00
                        </small>
                    </div>

                </div>
            </div>

        </div>

    </div>

    <!-- ═══════════════════════════════════════════════════════════
         SUBMIT CONFIRMATION MODAL
         ═══════════════════════════════════════════════════════════ -->
    <div class="modal fade" id="submitModal" tabindex="-1" aria-labelledby="submitModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content bg-dark border-secondary text-white">
                <div class="modal-header border-secondary">
                    <h5 class="modal-title fw-bold" id="submitModalLabel">
                        <i class="bi bi-question-circle text-warning me-2"></i> Submit Examination?
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <p class="text-secondary mb-3">
                        Are you sure you want to finalize your examination? Once submitted, your answers will be automatically graded and cannot be changed.
                    </p>

                    <!-- Attempt Summary Matrix -->
                    <div class="row g-2 text-center p-3 rounded mb-3" style="background: var(--ep-bg-secondary); border: 1px solid var(--ep-bg-tertiary);">
                        <div class="col-4 border-end border-secondary">
                            <div class="small text-muted">Answered</div>
                            <div class="h5 fw-bold text-success mb-0" id="modalAnsweredCount">0</div>
                        </div>
                        <div class="col-4 border-end border-secondary">
                            <div class="small text-muted">Unanswered</div>
                            <div class="h5 fw-bold text-warning mb-0" id="modalUnansweredCount">0</div>
                        </div>
                        <div class="col-4">
                            <div class="small text-muted">Marked Review</div>
                            <div class="h5 fw-bold text-purple mb-0" style="color: #a371f7;" id="modalReviewCount">0</div>
                        </div>
                    </div>

                    <div class="alert alert-warning py-2 px-3 small d-flex align-items-center mb-0">
                        <i class="bi bi-exclamation-triangle-fill me-2 fs-5"></i>
                        <span>Immediate automatic evaluation will compute your final score upon submission.</span>
                    </div>
                </div>
                <div class="modal-footer border-secondary">
                    <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">
                        Continue Examination
                    </button>
                    <form action="${pageContext.request.contextPath}/student/exam/submit" method="post" id="finalSubmitForm" class="m-0">
                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                        <input type="hidden" name="attemptId" value="${attempt.id}">
                        <button type="submit" class="btn btn-danger fw-bold px-4" id="confirmSubmitBtn">
                            Yes, Submit Now
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <!-- Hidden Auto-Submit Form for Timer Expiry -->
    <form action="${pageContext.request.contextPath}/student/exam/submit" method="post" id="autoSubmitForm" style="display: none;">
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <input type="hidden" name="attemptId" value="${attempt.id}">
    </form>

    <!-- Bootstrap 5 JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
            crossorigin="anonymous"></script>

    <!-- ═══════════════════════════════════════════════════════════
         EXAMINATION ENGINE CLIENT-SIDE LOGIC
         ═══════════════════════════════════════════════════════════ -->
    <script>
        // State Variables
        let currentIndex = 0;
        const totalQuestions = ${fn:length(questions)};
        const attemptId = ${attempt.id};
        const contextPath = '${pageContext.request.contextPath}';
        const csrfToken = '${csrfToken}';
        let remainingSeconds = ${attempt.remainingSeconds};

        // Questions in-memory state
        const questionData = [
            <c:forEach var="q" items="${questions}" varStatus="status">
                {
                    index: ${status.index},
                    questionId: ${q.questionId},
                    selectedOptionKey: '${q.selectedOptionKey != null ? q.selectedOptionKey : ""}',
                    isMarkedForReview: ${q.isMarkedForReview}
                }<c:if test="${!status.last}">,</c:if>
            </c:forEach>
        ];

        // Countdown Timer Engine
        function startTimer() {
            const display = document.getElementById('timerDisplay');
            const badge = document.getElementById('timerBadge');

            function updateDisplay() {
                if (remainingSeconds <= 0) {
                    display.textContent = '00:00:00';
                    badge.className = 'exam-timer-card timer-urgent';
                    // Automatic submission triggers immediately
                    autoSubmitExam();
                    return;
                }

                const hours = Math.floor(remainingSeconds / 3600);
                const minutes = Math.floor((remainingSeconds % 3600) / 60);
                const seconds = remainingSeconds % 60;

                const hStr = hours < 10 ? '0' + hours : hours;
                const mStr = minutes < 10 ? '0' + minutes : minutes;
                const sStr = seconds < 10 ? '0' + seconds : seconds;

                display.textContent = hStr + ':' + mStr + ':' + sStr;

                if (remainingSeconds <= 60) {
                    badge.className = 'exam-timer-card timer-urgent';
                } else if (remainingSeconds <= 300) {
                    badge.className = 'exam-timer-card timer-warning';
                } else {
                    badge.className = 'exam-timer-card timer-normal';
                }

                remainingSeconds--;
            }

            updateDisplay();
            const timerInterval = setInterval(function() {
                if (remainingSeconds < 0) {
                    clearInterval(timerInterval);
                } else {
                    updateDisplay();
                }
            }, 1000);
        }

        // Automatic Submission on Timer Expiry
        let isSubmitted = false;
        function autoSubmitExam() {
            if (isSubmitted) return;
            isSubmitted = true;
            window.onbeforeunload = null;

            // Show submitting notice
            alert("Examination time has expired! Your responses will now be automatically submitted and evaluated.");
            document.getElementById('autoSubmitForm').submit();
        }

        // Navigation between questions
        function jumpToQuestion(targetIndex) {
            if (targetIndex < 0 || targetIndex >= totalQuestions) return;

            // Hide old question
            const oldView = document.getElementById('qView_' + currentIndex);
            if (oldView) oldView.style.display = 'none';

            // Remove current ring from old palette button
            const oldPalette = document.getElementById('paletteBtn_' + currentIndex);
            if (oldPalette) oldPalette.classList.remove('status-current');

            // Show new question
            currentIndex = targetIndex;
            const newView = document.getElementById('qView_' + currentIndex);
            if (newView) newView.style.display = 'block';

            // Highlight new palette button
            const newPalette = document.getElementById('paletteBtn_' + currentIndex);
            if (newPalette) newPalette.classList.add('status-current');

            // Update Previous & Next button states
            updateNavButtons();
            // Update Review button visual state for current question
            updateReviewButtonState();
        }

        function navQuestion(delta) {
            jumpToQuestion(currentIndex + delta);
        }

        function updateNavButtons() {
            const prevBtn = document.getElementById('prevBtn');
            const nextBtn = document.getElementById('nextBtn');

            if (prevBtn) prevBtn.disabled = (currentIndex === 0);
            if (nextBtn) {
                if (currentIndex === totalQuestions - 1) {
                    nextBtn.innerHTML = 'Review All <i class="bi bi-check2 ms-1"></i>';
                } else {
                    nextBtn.innerHTML = 'Next <i class="bi bi-chevron-right ms-1"></i>';
                }
            }
        }

        // Option Selection Handler
        function selectOption(index, questionId, optionKey) {
            questionData[index].selectedOptionKey = optionKey;

            // Update option labels highlight in current question view
            const view = document.getElementById('qView_' + index);
            if (view) {
                const labels = view.querySelectorAll('.take-option-label');
                labels.forEach(label => label.classList.remove('active-selected'));

                const input = view.querySelector('input[value="' + optionKey + '"]');
                if (input) {
                    input.checked = true;
                    const parentLabel = input.closest('.take-option-label');
                    if (parentLabel) parentLabel.classList.add('active-selected');
                }
            }

            updatePaletteItem(index);
            updateSummaryCounts();
            persistAnswer(questionId, optionKey, questionData[index].isMarkedForReview);
        }

        // Clear Answer Handler
        function clearCurrentAnswer() {
            const q = questionData[currentIndex];
            if (!q.selectedOptionKey) return; // already empty

            q.selectedOptionKey = '';

            const view = document.getElementById('qView_' + currentIndex);
            if (view) {
                const inputs = view.querySelectorAll('input[type="radio"]');
                inputs.forEach(input => input.checked = false);

                const labels = view.querySelectorAll('.take-option-label');
                labels.forEach(label => label.classList.remove('active-selected'));
            }

            updatePaletteItem(currentIndex);
            updateSummaryCounts();
            persistAnswer(q.questionId, '', q.isMarkedForReview);
        }

        // Toggle Review Handler
        function toggleCurrentReview() {
            const q = questionData[currentIndex];
            q.isMarkedForReview = !q.isMarkedForReview;

            updateReviewButtonState();
            updatePaletteItem(currentIndex);
            updateSummaryCounts();
            persistAnswer(q.questionId, q.selectedOptionKey, q.isMarkedForReview);
        }

        function updateReviewButtonState() {
            const q = questionData[currentIndex];
            const reviewBtn = document.getElementById('reviewBtn');
            const reviewIcon = document.getElementById('reviewIcon');
            const reviewText = document.getElementById('reviewText');

            if (!reviewBtn) return;

            if (q.isMarkedForReview) {
                reviewBtn.className = 'btn btn-purple btn-sm';
                reviewBtn.style.background = '#8957e5';
                reviewBtn.style.color = '#ffffff';
                reviewIcon.className = 'bi bi-bookmark-fill me-1';
                reviewText.textContent = 'Marked for Review';
            } else {
                reviewBtn.className = 'btn btn-outline-purple btn-sm';
                reviewBtn.style.background = 'transparent';
                reviewBtn.style.color = '#a371f7';
                reviewIcon.className = 'bi bi-bookmark me-1';
                reviewText.textContent = 'Mark for Review';
            }
        }

        // Update Palette item class
        function updatePaletteItem(index) {
            const btn = document.getElementById('paletteBtn_' + index);
            if (!btn) return;

            const q = questionData[index];
            const hasAnswer = q.selectedOptionKey && q.selectedOptionKey.trim() !== '';
            const isReview = q.isMarkedForReview;

            btn.classList.remove('status-answered', 'status-review', 'status-answered-review');

            if (hasAnswer && isReview) {
                btn.classList.add('status-answered-review');
            } else if (hasAnswer) {
                btn.classList.add('status-answered');
            } else if (isReview) {
                btn.classList.add('status-review');
            }
        }

        // Update counts in legend and modal
        function updateSummaryCounts() {
            let answered = 0;
            let review = 0;

            questionData.forEach(q => {
                if (q.selectedOptionKey && q.selectedOptionKey.trim() !== '') answered++;
                if (q.isMarkedForReview) review++;
            });

            const unanswered = totalQuestions - answered;

            const elAns = document.getElementById('countAnswered');
            const elUnans = document.getElementById('countUnanswered');
            const elRev = document.getElementById('countReview');

            if (elAns) elAns.textContent = answered;
            if (elUnans) elUnans.textContent = unanswered;
            if (elRev) elRev.textContent = review;

            const mAns = document.getElementById('modalAnsweredCount');
            const mUnans = document.getElementById('modalUnansweredCount');
            const mRev = document.getElementById('modalReviewCount');

            if (mAns) mAns.textContent = answered;
            if (mUnans) mUnans.textContent = unanswered;
            if (mRev) mRev.textContent = review;
        }

        // Auto-Save Asynchronous Persistence
        function persistAnswer(questionId, selectedOptionKey, isMarkedForReview) {
            const saveStatus = document.getElementById('saveStatus');

            const body = new URLSearchParams();
            body.append('attemptId', attemptId);
            body.append('questionId', questionId);
            body.append('selectedOptionKey', selectedOptionKey || '');
            body.append('isMarkedForReview', isMarkedForReview ? 'true' : 'false');
            body.append('csrfToken', csrfToken);

            fetch(contextPath + '/student/exam/save-answer', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: body.toString()
            })
            .then(res => res.json())
            .then(data => {
                if (data.autoSubmitted) {
                    autoSubmitExam();
                    return;
                }

                if (saveStatus) {
                    saveStatus.style.opacity = '1';
                    setTimeout(() => {
                        saveStatus.style.opacity = '0';
                    }, 1500);
                }
            })
            .catch(err => {
                console.error("Auto-save sync error:", err);
            });
        }

        // Initialize on load
        document.addEventListener('DOMContentLoaded', function () {
            startTimer();
            updateNavButtons();
            updateReviewButtonState();
            updateSummaryCounts();

            // Guard against accidental tab closing
            window.onbeforeunload = function () {
                if (!isSubmitted) {
                    return "You have an active examination in progress. Are you sure you want to leave?";
                }
            };

            // Remove guard when submitting through modal
            const submitForm = document.getElementById('finalSubmitForm');
            if (submitForm) {
                submitForm.addEventListener('submit', function () {
                    isSubmitted = true;
                    window.onbeforeunload = null;
                });
            }
        });
    </script>
</body>
</html>

