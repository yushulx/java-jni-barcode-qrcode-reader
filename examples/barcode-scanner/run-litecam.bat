@echo off
setlocal
set JAR=%~dp0litecam.jar
if not exist "%JAR%" (
    echo Error: litecam.jar not found in the same directory as this script.
    echo Please run build-jar.ps1 first to build the JAR file.
    pause
    exit /b 1
)
echo Starting LiteCam Java Viewer...
java -cp "%JAR%" com.example.litecam.LiteCamViewer %*
if errorlevel 1 (
    echo Error: Failed to run LiteCam Java Viewer.
    echo Make sure Java is installed and accessible from PATH.
    pause
)
endlocal
