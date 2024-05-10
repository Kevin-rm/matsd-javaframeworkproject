@echo off

set "CURRENT_DIR=%~dp0"
set "BIN=%CURRENT_DIR%out"
set "SRC=%CURRENT_DIR%src"

echo "Début de la compilation"
dir /s /B "%SRC%\*.java" > sources.txt

mkdir "temp"
for /F "tokens=*" %%f in (sources.txt) do (
    copy "%%f" "temp"
)

if not exist "%BIN%" mkdir "%BIN%"
javac -cp "C:\apache-tomcat-10.1.9\lib\servlet-api.jar" -d "%BIN%" "temp\*.java"

del sources.txt
rd /s /q "temp"
echo "Fin de la compilation"

echo "Début de l'exportation en jar"
jar cvfm matsd-javaframework-webmvc.jar NUL -C "%BIN%" .
echo "Fin de l'exportation"

pause
