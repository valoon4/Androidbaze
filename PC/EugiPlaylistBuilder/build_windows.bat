@echo off
setlocal
cd /d "%~dp0"
py -3 -m pip install --upgrade pyinstaller
if errorlevel 1 goto fail
py -3 -m PyInstaller --noconfirm --clean --onefile --windowed --name EugiPlaylistBuilder app.py
if errorlevel 1 goto fail
echo Portable EXE: dist\EugiPlaylistBuilder.exe
pause
exit /b 0
:fail
echo Build fehlgeschlagen.
pause
exit /b 1
