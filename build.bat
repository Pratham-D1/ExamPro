@echo off
:: ════════════════════════════════════════════════════════════════════
:: build.bat  –  ExamPro Build Script for Windows
:: ════════════════════════════════════════════════════════════════════
:: This script builds ExamPro.war and optionally deploys it to Tomcat.
:: Run it from the project root: d:\Projects\Exam_Management_System\
::
:: Usage:
::   build.bat          -> Build WAR only
::   build.bat deploy   -> Build WAR and deploy to Tomcat
:: ════════════════════════════════════════════════════════════════════

setlocal

:: ── Configuration ────────────────────────────────────────────────
set TOMCAT_HOME=D:\Programming Software\apache-tomcat-10.1.26\apache-tomcat-10.1.26
set MAVEN_CMD=mvn

:: Check if mvn is on PATH, otherwise try common locations
where mvn >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [INFO] mvn not found on PATH. Looking in common locations...
    if exist "C:\Maven\apache-maven-3.9.16\bin\mvn.cmd" (
        set MAVEN_CMD=C:\Maven\apache-maven-3.9.16\bin\mvn.cmd
        echo [INFO] Found Maven at C:\Maven\apache-maven-3.9.16\bin\mvn.cmd
    ) else if exist "%USERPROFILE%\AppData\Local\Programs\Maven\bin\mvn.cmd" (
        set MAVEN_CMD=%USERPROFILE%\AppData\Local\Programs\Maven\bin\mvn.cmd
    ) else (
        echo.
        echo [ERROR] Maven not found!
        echo.
        echo Please install Maven:
        echo   1. Download from: https://maven.apache.org/download.cgi
        echo   2. Extract to a folder (e.g., D:\Programming Software\apache-maven-3.9.9)
        echo   3. Add its 'bin' directory to your PATH
        echo   4. Run this script again.
        echo.
        pause
        exit /b 1
    )
)

echo ════════════════════════════════════════════════════════
echo   ExamPro Build Script
echo ════════════════════════════════════════════════════════
echo.

:: ── Step 1: Clean and Build ───────────────────────────────────────
echo [STEP 1] Building ExamPro.war ...
%MAVEN_CMD% clean package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo.
    echo [FAILED] Build failed! Check the error messages above.
    pause
    exit /b 1
)

echo.
echo [SUCCESS] WAR file built: target\ExamPro.war

:: ── Step 2: Deploy (optional) ────────────────────────────────────
if "%1"=="deploy" (
    echo.
    echo [STEP 2] Deploying to Tomcat...

    if not exist "%TOMCAT_HOME%\webapps" (
        echo [ERROR] Tomcat webapps directory not found: %TOMCAT_HOME%\webapps
        echo         Update TOMCAT_HOME in this script.
        pause
        exit /b 1
    )

    :: Remove old deployment
    if exist "%TOMCAT_HOME%\webapps\ExamPro.war" (
        del "%TOMCAT_HOME%\webapps\ExamPro.war"
        echo [INFO] Removed old ExamPro.war
    )
    if exist "%TOMCAT_HOME%\webapps\ExamPro" (
        rmdir /s /q "%TOMCAT_HOME%\webapps\ExamPro"
        echo [INFO] Removed old ExamPro folder
    )

    :: Copy new WAR
    copy "target\ExamPro.war" "%TOMCAT_HOME%\webapps\"
    echo [SUCCESS] Deployed ExamPro.war to Tomcat webapps.
    echo.
    echo [INFO] Start Tomcat to deploy:
    echo        %TOMCAT_HOME%\bin\startup.bat
)

echo.
echo ════════════════════════════════════════════════════════
echo   Done! Next steps:
echo   1. Start Tomcat: startup.bat
echo   2. Visit: http://localhost:8080/ExamPro/health
echo ════════════════════════════════════════════════════════
pause
