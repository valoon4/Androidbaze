@echo off
setlocal
set ROOT=%~dp0
set GRADLE_VERSION=8.9
set CACHE_DIR=%ROOT%.gradle-dist
set DIST_DIR=%CACHE_DIR%\gradle-%GRADLE_VERSION%
set ZIP=%CACHE_DIR%\gradle-%GRADLE_VERSION%-bin.zip
if exist "%DIST_DIR%\bin\gradle.bat" goto run
if not exist "%CACHE_DIR%" mkdir "%CACHE_DIR%"
if not exist "%ZIP%" (
  echo [Androidbaze] Downloading Gradle %GRADLE_VERSION%...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%ZIP%'"
  if errorlevel 1 exit /b 1
)
echo [Androidbaze] Extracting Gradle...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Force '%ZIP%' '%CACHE_DIR%'"
if errorlevel 1 exit /b 1
:run
call "%DIST_DIR%\bin\gradle.bat" %*
