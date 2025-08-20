@echo off
setlocal
set JAR=%~dp0litecam.jar
java -cp "%JAR%" com.example.litecam.LiteCamViewer %*
endlocal
