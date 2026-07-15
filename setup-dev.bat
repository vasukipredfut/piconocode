@echo off
REM =====================================================================
REM EARMS Backend - One-command setup for Windows (Docker Desktop required)
REM
REM Steps (idempotent):
REM   1. Verify Docker Desktop is installed + running
REM   2. Start mariadb:11 on port 3306 if not running
REM   3. Bootstrap earms schema + earms_app user
REM   4. Load V1__baseline.sql if schema empty
REM   5. Build Spring Boot jar (host mvn or dockerized fallback)
REM
REM Usage: setup-dev.bat [--yes] [--no-mariadb] [--skip-build] [--help]
REM =====================================================================

setlocal enableextensions enabledelayedexpansion

set "ASSUME_YES=false"
set "SKIP_MARIA=false"
set "SKIP_BUILD=false"
:parse_args
if "%~1"=="" goto :after_args
if /i "%~1"=="--yes"        set "ASSUME_YES=true"
if /i "%~1"=="-y"           set "ASSUME_YES=true"
if /i "%~1"=="--no-mariadb" set "SKIP_MARIA=true"
if /i "%~1"=="--skip-build" set "SKIP_BUILD=true"
if /i "%~1"=="--help"       goto :help
shift
goto :parse_args
:after_args

set "BACKEND_DIR=%~dp0"
set "BACKEND_DIR=%BACKEND_DIR:~0,-1%"
set "BOOTSTRAP_SQL=%BACKEND_DIR%\src\main\resources\db\bootstrap\00_create_schema_and_user.sql"
set "BASELINE_SQL=%BACKEND_DIR%\src\main\resources\db\migration\V1__baseline.sql"

echo.
echo === Step 1: Prerequisites ===
where docker >nul 2>&1 || (echo ERROR: docker not found. Install Docker Desktop. & exit /b 1)
docker info >nul 2>&1 || (echo ERROR: docker daemon not running. Start Docker Desktop. & exit /b 1)
echo OK: docker
set "HAS_MVN=false"
where mvn >nul 2>&1 && set "HAS_MVN=true"
if "!HAS_MVN!"=="true" (echo OK: maven on host) else (echo WARN: maven not on host - dockerized build)

echo.
echo === Step 2: MariaDB ===
if "!SKIP_MARIA!"=="true" (
  echo OK: Skipped --no-mariadb - using host MariaDB at 127.0.0.1:3306
) else (
  set "RUNNING="
  for /f "delims=" %%n in ('docker ps --format "{{.Names}}" 2^>nul ^| findstr /b /c:"earms-mariadb"') do set "RUNNING=%%n"
  if defined RUNNING (
    echo OK: earms-mariadb already running
  ) else (
    set "EXISTS="
    for /f "delims=" %%n in ('docker ps -a --format "{{.Names}}" 2^>nul ^| findstr /b /c:"earms-mariadb"') do set "EXISTS=%%n"
    if defined EXISTS (
      docker start earms-mariadb >nul
      echo OK: earms-mariadb started
    ) else (
      echo Pulling + starting mariadb:11...
      docker run -d --name earms-mariadb --restart unless-stopped -p 3306:3306 -e MARIADB_ROOT_PASSWORD=root_local_dev -v earms-mariadb-data:/var/lib/mysql mariadb:11 >nul
      if errorlevel 1 (echo ERROR: docker run failed. Port 3306 may be busy. Try --no-mariadb. & exit /b 1)
      <nul set /p ="Waiting for MariaDB"
      for /l %%i in (1,1,40) do (
        docker exec earms-mariadb mysqladmin -uroot -proot_local_dev ping >nul 2>&1 && goto :maria_ready
        <nul set /p =.
        timeout /t 2 /nobreak >nul
      )
      echo.& echo ERROR: MariaDB did not become ready in 80s & exit /b 1
      :maria_ready
      echo.& echo OK: MariaDB ready
    )
  )
)

echo.
echo === Step 3: Bootstrap schema + user ===
if not exist "!BOOTSTRAP_SQL!" (echo ERROR: missing !BOOTSTRAP_SQL! & exit /b 1)
if "!SKIP_MARIA!"=="true" (
  set /p MARIADB_ROOT_PASSWORD=Host MariaDB root password:
  mysql -h 127.0.0.1 -uroot -p!MARIADB_ROOT_PASSWORD! < "!BOOTSTRAP_SQL!"
) else (
  docker exec -i earms-mariadb mysql -uroot -proot_local_dev < "!BOOTSTRAP_SQL!"
)
if errorlevel 1 (echo ERROR: bootstrap SQL failed & exit /b 1)
echo OK: earms schema + earms_app user ready

if "!SKIP_MARIA!"=="false" if exist "!BASELINE_SQL!" (
  for /f %%t in ('docker exec earms-mariadb mysql -uearms_app -pearms_local_dev earms -Nse "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='earms'"') do set "TABLES=%%t"
  if "!TABLES!"=="0" (
    echo Loading V1__baseline.sql...
    docker exec -i earms-mariadb mysql -uearms_app -pearms_local_dev earms < "!BASELINE_SQL!"
    echo OK: Baseline loaded
  ) else (
    echo OK: !TABLES! tables already in earms ^(skipping baseline^)
  )
)

echo.
echo === Step 4: Build backend ===
if "!SKIP_BUILD!"=="true" (
  echo OK: Skipped --skip-build
) else (
  if "!HAS_MVN!"=="true" (
    call mvn -o package -DskipTests -q
    if errorlevel 1 (echo ERROR: mvn build failed & exit /b 1)
  ) else (
    docker run --rm -v "!BACKEND_DIR!:/work" -v earms-m2:/root/.m2 -w /work maven:3.9-eclipse-temurin-21 mvn -o package -DskipTests -q
    if errorlevel 1 (echo ERROR: dockerized maven build failed & exit /b 1)
  )
  echo OK: target\idayam-0.0.1-SNAPSHOT.jar built
)

echo.
echo ===============================================================
echo  Setup complete
echo ===============================================================
echo.
echo  Start backend ^(port 8080^):
echo    java -jar target\idayam-0.0.1-SNAPSHOT.jar
echo.
echo  Default login: 1001 / Test@1234
echo.
echo  Frontend:
echo    cd ..\earmsfrontend ^&^& setup-dev.bat ^&^& npm start
echo.
echo  Stop MariaDB:    docker stop earms-mariadb
echo  Wipe + restart:  docker rm -f earms-mariadb ^&^& docker volume rm earms-mariadb-data
echo.
endlocal
exit /b 0

:help
echo Usage: setup-dev.bat [--yes] [--no-mariadb] [--skip-build] [--help]
exit /b 0
