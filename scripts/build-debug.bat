@echo off
setlocal
cd /d %~dp0\..
call gradlew.bat --no-daemon assembleDebug
if errorlevel 1 exit /b %errorlevel%
if not exist app\build\outputs\apk\debug\app-debug.apk (
  echo APK missing after build.
  exit /b 1
)
echo [Androidbaze] APK: %cd%\app\build\outputs\apk\debug\app-debug.apk
