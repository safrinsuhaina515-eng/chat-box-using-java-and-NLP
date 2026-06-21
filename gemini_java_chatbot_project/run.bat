@echo off
cd /d %~dp0src
javac *.java
if %errorlevel% neq 0 (
  echo Compilation failed.
  pause
  exit /b
)
java ChatBotGUI
pause
