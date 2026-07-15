@echo off
REM ============================================
REM Idayam ERP Database Migration Runner
REM Script: run-migrations.bat
REM Date: April 22, 2026
REM Description: Automated database migration execution for Windows
REM ============================================

setlocal enabledelayedexpansion

REM Configuration
set DB_HOST=localhost
set DB_PORT=5432
set DB_NAME=idayam_erp
set DB_USER=postgres
REM set DB_PASSWORD=your_password_here

REM Get script directory
set MIGRATIONS_DIR=%~dp0

echo.
echo ==========================================
echo Idayam ERP Database Migration Runner
echo ==========================================
echo Target Database: %DB_NAME%
echo Host: %DB_HOST%
echo Port: %DB_PORT%
echo User: %DB_USER%
echo Migrations Directory: %MIGRATIONS_DIR%
echo ==========================================
echo.

REM Check if psql is available
where psql >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: psql not found. Please install PostgreSQL client tools.
    exit /b 1
)

REM Prompt for password if not set
if "%DB_PASSWORD%"=="" (
    set /p DB_PASSWORD="Enter PostgreSQL password for user %DB_USER%: "
)

REM Confirm before running
set /p confirm="Do you want to proceed with migrations? (yes/no): "
if /i not "%confirm%"=="yes" (
    echo Migration cancelled by user.
    exit /b 0
)

echo.
echo Starting database migrations...
echo.

REM Array of migration files in order
setlocal enabledelayedexpansion
set migrations[0]=01-schema.sql
set migrations[1]=02-core-tables.sql
set migrations[2]=03-business-tables.sql
set migrations[3]=04-indexes.sql
set migrations[4]=05-seed-data.sql

REM Execute each migration
for /l %%i in (0,1,4) do (
    set migration=!migrations[%%i]!
    set migration_file=%MIGRATIONS_DIR%!migration!
    
    if not exist "!migration_file!" (
        echo ERROR: Migration file not found: !migration_file!
        exit /b 1
    )
    
    echo Executing: !migration!
    
    set PGPASSWORD=%DB_PASSWORD%
    psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -f "!migration_file!" >nul 2>nul
    
    if !errorlevel! equ 0 (
        echo [OK] Completed: !migration!
    ) else (
        echo ERROR: Failed to execute: !migration!
        echo Please check the migration file and database connection.
        exit /b 1
    )
    echo.
)

REM Verify installation
echo Verifying installation...
set PGPASSWORD=%DB_PASSWORD%
psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -c "SELECT 'Setup Complete - Database Tables:' as status; \dt"

echo.
echo ==========================================
echo All migrations completed successfully!
echo Database is ready for application use.
echo ==========================================
echo.
echo Next steps:
echo   1. Update application.properties with database connection details
echo   2. Test connection from Spring Boot application
echo   3. Verify all tables and data using PgAdmin or DBeaver
echo   4. Create backups before going to production
echo.

REM Cleanup
endlocal
