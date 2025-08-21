# PowerShell script to run LiteCam Java Viewer
param([string[]]$Args = @())

$ErrorActionPreference = 'Stop'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$jarPath = Join-Path $scriptDir 'litecam.jar'

if (-not (Test-Path $jarPath)) {
    Write-Host "Error: litecam.jar not found in $scriptDir" -ForegroundColor Red
    Write-Host "Please run build-jar.ps1 first to build the JAR file." -ForegroundColor Yellow
    exit 1
}

try {
    Write-Host "Starting LiteCam Java Viewer..." -ForegroundColor Green
    & java -cp $jarPath com.example.litecam.LiteCamViewer @Args
} catch {
    Write-Host "Error: Failed to run LiteCam Java Viewer: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Make sure Java is installed and accessible from PATH." -ForegroundColor Yellow
    exit 1
}
