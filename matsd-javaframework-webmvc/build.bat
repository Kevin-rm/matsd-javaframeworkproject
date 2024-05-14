@echo off
chcp 65001 > nul

set "CURRENT_DIR=%~dp0"
set "SRC=%CURRENT_DIR%src"
set "TEMP=%CURRENT_DIR%temp"
set "BIN=%CURRENT_DIR%bin"
set "CLASSPATH=C:\apache-tomcat-10.1.9\lib\servlet-api.jar"

:: Compilation
mkdir "%TEMP%"
dir /s /B "%SRC%\*.java" > "%TEMP%\sources.txt"
for /F "tokens=*" %%f in (%TEMP%\sources.txt) do (
    copy "%%f" "%TEMP%"
)

if not exist "%BIN%" mkdir "%BIN%"
javac -cp "%CLASSPATH%" -d "%BIN%" "%TEMP%\*.java"
rd /s /q "%TEMP%"

:: Exportation en jar
jar cvf matsd-javaframework-webmvc.jar -C "%BIN%" .

pause
