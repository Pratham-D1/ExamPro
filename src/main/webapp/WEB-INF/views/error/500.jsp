<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>500 Server Error | ExamPro</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
</head>
<body class="d-flex align-items-center justify-content-center min-vh-100 text-center">
    <div>
        <div style="font-size:6rem; color:#f85149;">
            <i class="bi bi-exclamation-triangle-fill"></i>
        </div>
        <h1 class="display-1 fw-bold" style="color:#f85149;">500</h1>
        <h2 class="mb-3">Internal Server Error</h2>
        <p class="text-muted mb-4">Something went wrong on our end. Please try again or contact support.</p>
        <a href="${pageContext.request.contextPath}/" class="btn btn-accent px-4">
            <i class="bi bi-house me-2"></i> Go Home
        </a>
    </div>
</body>
</html>
