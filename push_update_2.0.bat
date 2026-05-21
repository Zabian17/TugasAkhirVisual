@echo off
REM ============================================================
REM GIT PUSH SCRIPT - Push to "update 2.0" branch
REM ============================================================

setlocal enabledelayedexpansion

echo.
echo ============================================================
echo  GudangKu - Push to Branch "update 2.0"
echo ============================================================
echo.

REM Check if git is installed
git --version > nul 2>&1
if errorlevel 1 (
    echo ERROR: Git is not installed or not in PATH
    pause
    exit /b 1
)

REM Navigate to project directory
cd /d c:\laragon\www\tugas_akhir.worktrees\agents-file-check-and-validation

echo [1/7] Checking git status...
echo.
git status --short
echo.

echo [2/7] Creating/Switching to branch 'update 2.0'...
git checkout -b "update 2.0" 2>nul
if errorlevel 1 (
    echo Branch already exists, switching...
    git checkout "update 2.0"
)
echo.

echo [3/7] Staging all changes...
git add -A
echo.

echo [4/7] Verifying staged changes...
git status --short
echo.

echo [5/7] Committing changes...
git commit -m "feat: Setup database and authentication system v2.0" -m "- Added comprehensive documentation (README, SETUP, QUICKSTART)
- Updated database configuration (gudang_akhir)
- Fixed authentication system (plaintext mode for testing)
- Updated pom.xml with BCrypt dependency
- Modified PasswordHelper for password verification
- Updated schema.sql with test user account
- Added debugging and troubleshooting guides

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"

if errorlevel 1 (
    echo WARNING: Commit failed or nothing to commit
    echo Continuing...
)
echo.

echo [6/7] Checking commit log...
git log --oneline -1
echo.

echo [7/7] Pushing to remote...
git push -u origin "update 2.0"

if errorlevel 1 (
    echo.
    echo ERROR: Push failed!
    echo Please check your internet connection and GitHub credentials.
    echo.
    echo Manual push command:
    echo   git push -u origin "update 2.0"
    echo.
    pause
    exit /b 1
)

echo.
echo ============================================================
echo  SUCCESS! Changes pushed to "update 2.0" branch
echo ============================================================
echo.
echo Next steps:
echo   1. Go to GitHub repository
echo   2. Create Pull Request from "update 2.0" to "main"
echo   3. Review and merge when ready
echo.
echo Current branch info:
git branch -a
echo.
git log --oneline -3
echo.

pause
