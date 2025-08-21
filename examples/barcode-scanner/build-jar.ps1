# PowerShell script to build the complete LiteCam project with native libraries and Java wrapper
param(
    [string]$BuildDir = "build",
    [string]$Configuration = "Release",
    [switch]$CleanBuild,
    [switch]$SkipNativeBuild,
    [switch]$Verbose,
    # Optional: additional directories containing prebuilt natives to merge (each should contain platform libs)
    [string[]]$ExtraNativeDirs = @()
)

$ErrorActionPreference = 'Stop'

if ($Verbose) {
    $VerbosePreference = 'Continue'
}

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

Write-Host "Building LiteCam Java Wrapper with Native Libraries..." -ForegroundColor Green
Write-Host "Project Root: $projectRoot" -ForegroundColor Cyan
Write-Host "Build Directory: $BuildDir" -ForegroundColor Cyan
Write-Host "Configuration: $Configuration" -ForegroundColor Cyan

# Helper functions
function Test-Command {
    param([string]$Command)
    try {
        Get-Command $Command -ErrorAction Stop | Out-Null
        return $true
    }
    catch {
        return $false
    }
}

function Invoke-CommandWithLogging {
    param(
        [string]$Command,
        [string]$Description,
        [switch]$AllowFailure
    )
    
    Write-Host "$Description..." -ForegroundColor Yellow
    Write-Verbose "Executing: $Command"
    
    try {
        Invoke-Expression $Command
        if ($LASTEXITCODE -ne 0 -and -not $AllowFailure) {
            throw "$Description failed with exit code $LASTEXITCODE"
        }
        Write-Host "[$Description] completed successfully" -ForegroundColor Green
    }
    catch {
        Write-Host "[$Description] failed: $($_.Exception.Message)" -ForegroundColor Red
        if (-not $AllowFailure) {
            throw
        }
    }
}

# Verify required tools
Write-Host "Checking required tools..." -ForegroundColor Yellow

$missingTools = @()
if (-not (Test-Command "cmake")) { $missingTools += "cmake" }
if (-not (Test-Command "javac")) { $missingTools += "javac (Java JDK)" }
if (-not (Test-Command "jar")) { $missingTools += "jar (Java JDK)" }

if ($missingTools.Count -gt 0) {
    Write-Host "Missing required tools: $($missingTools -join ', ')" -ForegroundColor Red
    Write-Host "Please install the missing tools and ensure they are in your PATH." -ForegroundColor Red
    exit 1
}
Write-Host "All required tools found" -ForegroundColor Green

# Step 1: Build native libraries if not skipped
if (-not $SkipNativeBuild) {
    Write-Host "Building native libraries..." -ForegroundColor Green
    
    $buildPath = Join-Path $projectRoot $BuildDir
    
    if ($CleanBuild -and (Test-Path $buildPath)) {
        Write-Host "Cleaning previous build..." -ForegroundColor Yellow
        Remove-Item -Recurse -Force $buildPath
    }
    
    if (-not (Test-Path $buildPath)) {
        New-Item -ItemType Directory -Force -Path $buildPath | Out-Null
    }
    
    Push-Location $buildPath
    try {
        # Configure CMake
        Invoke-CommandWithLogging "cmake .." "CMake configuration"
        
        # Build the project
        Invoke-CommandWithLogging "cmake --build . --config $Configuration" "Native library build"
        
        # Verify the build output
        $expectedLibs = @()
        if ($IsWindows -or $env:OS -eq "Windows_NT") {
            $expectedLibs += Join-Path $buildPath "$Configuration/litecam.dll"
        }
        elseif ($IsMacOS) {
            $expectedLibs += Join-Path $buildPath "liblitecam.dylib"
        }
        else {
            $expectedLibs += Join-Path $buildPath "liblitecam.so"
        }
        
        $foundLib = $false
        foreach ($lib in $expectedLibs) {
            if (Test-Path $lib) {
                Write-Host "Found native library: $lib" -ForegroundColor Green
                $foundLib = $true
                break
            }
        }
        
        if (-not $foundLib) {
            Write-Host "No native library found in expected locations" -ForegroundColor Red
            Write-Host "Expected locations: $($expectedLibs -join ', ')" -ForegroundColor Red
            throw "Native library build verification failed"
        }
        
    }
    finally {
        Pop-Location
    }
}
else {
    Write-Host "Skipping native build (SkipNativeBuild flag set)" -ForegroundColor Yellow
}

# Step 2: Build Java wrapper
Write-Host "Building Java wrapper..." -ForegroundColor Green

$javaSrc = Join-Path $projectRoot 'java-src'
$outDir = Join-Path $projectRoot 'java-build'

if (Test-Path $outDir) { 
    Write-Host "Cleaning previous Java build..." -ForegroundColor Yellow
    Remove-Item -Recurse -Force $outDir 
}
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

Write-Host "Compiling Java sources..." -ForegroundColor Yellow

$javaFiles = Get-ChildItem -Recurse $javaSrc -Filter *.java | ForEach-Object { $_.FullName }
Write-Verbose "Java files found: $($javaFiles -join ', ')"

Invoke-CommandWithLogging "javac -d `"$outDir`" $($javaFiles -join ' ')" "Java compilation"

$jarPath = Join-Path $projectRoot 'litecam.jar'
if (Test-Path $jarPath) { 
    Write-Host "Removing existing JAR..." -ForegroundColor Yellow
    Remove-Item $jarPath 
}

# Step 3: Package native libraries
Write-Host "Packaging native libraries..." -ForegroundColor Green

# Collect native libraries by platform/arch
$dllCandidates = @()
$dllCandidates += (Join-Path $projectRoot "build/$Configuration/litecam.dll")
$dllCandidates += (Join-Path $projectRoot 'build/litecam.dll')
$dllCandidates += (Join-Path $projectRoot "build/$Configuration/liblitecam.dylib")
$dllCandidates += (Join-Path $projectRoot 'build/liblitecam.dylib')
$dllCandidates += (Join-Path $projectRoot "build/$Configuration/liblitecam.so")
$dllCandidates += (Join-Path $projectRoot 'build/liblitecam.so')

function Get-OsToken {
    if ($IsWindows -or $env:OS -eq "Windows_NT") { return 'windows' }
    elseif ($IsMacOS) { return 'macos' }
    else { return 'linux' }
}

function Get-ArchToken { 
    $arch = [System.Runtime.InteropServices.RuntimeInformation]::OSArchitecture
    if ($arch -in @('Arm64')) { 'arm64' } else { 'x86_64' } 
}

$osToken = Get-OsToken
$archToken = Get-ArchToken

Write-Host "Detected platform: $osToken-$archToken" -ForegroundColor Cyan

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
        Write-Host "Bundling native library: $c -> natives/$osToken-$archToken/$destName" -ForegroundColor Green
        $picked = $true
        break
    }
}
if (-not $picked) { 
    Write-Warning "No native library found to bundle for host platform ($osToken-$archToken)"
    Write-Host "Searched locations:" -ForegroundColor Yellow
    foreach ($candidate in $dllCandidates) {
        Write-Host "  - $candidate" -ForegroundColor Gray
    }
}

# Merge additional prebuilt native sets
if ($ExtraNativeDirs.Count -gt 0) {
    Write-Host "Merging additional prebuilt natives..." -ForegroundColor Green
    foreach ($dir in $ExtraNativeDirs) {
        if (-not (Test-Path $dir)) { 
            Write-Warning "Extra native dir not found: $dir"
            continue 
        }
        # Expect structure like <dir>/windows-x86_64/litecam.dll etc. Copy recursively.
        Get-ChildItem -Directory $dir | ForEach-Object {
            $platformFolder = $_.Name
            $target = Join-Path $nativeRoot $platformFolder
            New-Item -ItemType Directory -Force -Path $target | Out-Null
            Get-ChildItem -File -Recurse $_.FullName | ForEach-Object {
                Copy-Item $_.FullName (Join-Path $target ($_.Name)) -Force
            }
            Write-Host "Merged extra natives: $platformFolder" -ForegroundColor Green
        }
    }
}

# Step 4: Create JAR with manifest
Write-Host "Creating JAR with bundled natives..." -ForegroundColor Green

# Create a proper manifest
$manifestDir = Join-Path $outDir 'META-INF'
New-Item -ItemType Directory -Force -Path $manifestDir | Out-Null
$manifestFile = Join-Path $manifestDir 'MANIFEST.MF'

$manifestContent = @"
Manifest-Version: 1.0
Main-Class: com.example.litecam.LiteCamViewer
Implementation-Title: LiteCam Java Wrapper
Implementation-Version: 1.0.0
Implementation-Vendor: LiteCam Project
Built-Date: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
Built-Platform: $osToken-$archToken

"@

Set-Content -Path $manifestFile -Value $manifestContent -Encoding ASCII

Push-Location $outDir
try {
    Invoke-CommandWithLogging "jar cfm `"$jarPath`" META-INF/MANIFEST.MF ." "JAR creation"
}
finally {
    Pop-Location
}

# Step 5: Verify JAR contents
Write-Host "Verifying JAR contents..." -ForegroundColor Green
$jarContents = & jar -tf $jarPath
$expectedFiles = @(
    'com/example/litecam/LiteCam.class',
    'com/example/litecam/LiteCamViewer.class',
    'META-INF/MANIFEST.MF'
)

$missingFiles = @()
foreach ($expected in $expectedFiles) {
    if ($jarContents -notcontains $expected) {
        $missingFiles += $expected
    }
}

if ($missingFiles.Count -gt 0) {
    Write-Warning "Missing expected files in JAR: $($missingFiles -join ', ')"
}
else {
    Write-Host "All expected files found in JAR" -ForegroundColor Green
}

# Count native libraries
$nativeFiles = $jarContents | Where-Object { $_ -like 'natives/*' -and $_ -notlike '*/' }
Write-Host "Native libraries included: $($nativeFiles.Count)" -ForegroundColor Cyan
foreach ($nativeFile in $nativeFiles) {
    Write-Host "  - $nativeFile" -ForegroundColor Gray
}

Write-Host "Created $jarPath" -ForegroundColor Green

# Step 6: Create convenience scripts
Write-Host "Creating convenience scripts..." -ForegroundColor Green

# Convenience run script
$runScript = @'
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
'@

Set-Content -Path (Join-Path $projectRoot 'run-litecam.bat') -Value $runScript -Encoding ASCII
Write-Host "Created run-litecam.bat" -ForegroundColor Green

# PowerShell run script
$runPsScript = @'
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
'@

Set-Content -Path (Join-Path $projectRoot 'run-litecam.ps1') -Value $runPsScript -Encoding UTF8
Write-Host "Created run-litecam.ps1" -ForegroundColor Green

# Step 7: Final summary
Write-Host ""
Write-Host "Build completed successfully!" -ForegroundColor Green
Write-Host "Summary:" -ForegroundColor Cyan
Write-Host "  JAR file: $jarPath" -ForegroundColor White
Write-Host "  File size: $([math]::Round((Get-Item $jarPath).Length / 1MB, 2)) MB" -ForegroundColor White
Write-Host "  Native libraries: $($nativeFiles.Count)" -ForegroundColor White
Write-Host "  Target platform: $osToken-$archToken" -ForegroundColor White
Write-Host ""
Write-Host "To run the application:" -ForegroundColor Yellow
Write-Host "  Windows: .\run-litecam.bat" -ForegroundColor White
Write-Host "  PowerShell: .\run-litecam.ps1" -ForegroundColor White
Write-Host "  Manual: java -cp litecam.jar com.example.litecam.LiteCamViewer" -ForegroundColor White
