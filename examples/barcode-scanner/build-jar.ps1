# PowerShell script to compile Java sources and create litecam.jar
param(
    [string]$BuildDir = "build",
    # Optional: additional directories containing prebuilt natives to merge (each should contain platform libs)
    [string[]]$ExtraNativeDirs = @()
)

$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

$javaSrc = Join-Path $projectRoot 'java-src'
$outDir = Join-Path $projectRoot 'java-build'
if (Test-Path $outDir) { Remove-Item -Recurse -Force $outDir }
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

Write-Host "Compiling Java sources..."

$javaFiles = Get-ChildItem -Recurse $javaSrc -Filter *.java | ForEach-Object { $_.FullName }
& javac -d $outDir $javaFiles
if ($LASTEXITCODE -ne 0) { throw "javac failed" }

$jarPath = Join-Path $projectRoot 'litecam.jar'
if (Test-Path $jarPath) { Remove-Item $jarPath }

# Collect native libraries by platform/arch
$nativeMap = @{}
$dllCandidates = @()
$dllCandidates += (Join-Path $projectRoot 'build/Release/litecam.dll')
$dllCandidates += (Join-Path $projectRoot 'build/litecam.dll')
$dllCandidates += (Join-Path $projectRoot 'build/Release/liblitecam.dylib')
$dllCandidates += (Join-Path $projectRoot 'build/liblitecam.dylib')
$dllCandidates += (Join-Path $projectRoot 'build/Release/liblitecam.so')
$dllCandidates += (Join-Path $projectRoot 'build/liblitecam.so')

function Get-OsToken {
    if ([System.Runtime.InteropServices.RuntimeInformation]::IsOSPlatform([System.Runtime.InteropServices.OSPlatform]::Windows)) { return 'windows' }
    elseif ([System.Runtime.InteropServices.RuntimeInformation]::IsOSPlatform([System.Runtime.InteropServices.OSPlatform]::OSX)) { return 'macos' }
    else { return 'linux' }
}
function Get-ArchToken { if ([System.Runtime.InteropServices.RuntimeInformation]::OSArchitecture -in @('Arm64')) { 'arm64' } else { 'x86_64' } }

$osToken = Get-OsToken
$archToken = Get-ArchToken

# Primary host build native bundling
$nativeRoot = Join-Path $outDir 'natives'
New-Item -ItemType Directory -Force -Path $nativeRoot | Out-Null
$nativeDir = Join-Path $nativeRoot "$osToken-$archToken"
New-Item -ItemType Directory -Force -Path $nativeDir | Out-Null

$picked = $false
foreach ($c in $dllCandidates) {
    if (Test-Path $c) {
        $destName = if ($osToken -eq 'windows') { 'litecam.dll' } elseif ($osToken -eq 'macos') { 'liblitecam.dylib' } else { 'liblitecam.so' }
        Copy-Item $c (Join-Path $nativeDir $destName) -Force
        Write-Host "Bundling native library: $c -> natives/$osToken-$archToken/$destName"
        $picked = $true
        break
    }
}
if (-not $picked) { Write-Warning "No native library found to bundle for host." }

# Merge additional prebuilt native sets
foreach ($dir in $ExtraNativeDirs) {
    if (-not (Test-Path $dir)) { Write-Warning "Extra native dir not found: $dir"; continue }
    # Expect structure like <dir>/windows-x86_64/litecam.dll etc. Copy recursively.
    Get-ChildItem -Directory $dir | ForEach-Object {
        $platformFolder = $_.Name
        $target = Join-Path $nativeRoot $platformFolder
        New-Item -ItemType Directory -Force -Path $target | Out-Null
        Get-ChildItem -File -Recurse $_.FullName | ForEach-Object {
            Copy-Item $_.FullName (Join-Path $target ($_.Name)) -Force
        }
        Write-Host "Merged extra natives: $platformFolder"
    }
}

Write-Host "Creating JAR with bundled natives..."
Push-Location $outDir
& jar cf $jarPath .
Pop-Location

Write-Host "Created $jarPath (includes natives/* for packaged platforms)"

# Convenience run script
$runScript = @'
@echo off
setlocal
set JAR=%~dp0litecam.jar
java -cp "%JAR%" com.example.litecam.LiteCamViewer %*
endlocal
'@
Set-Content -Path (Join-Path $projectRoot 'run-litecam.bat') -Value $runScript -Encoding ASCII
Write-Host "Created run-litecam.bat"
