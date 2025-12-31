param (
    [Parameter(Mandatory=$true)]
    [string]$Version
)

$VersionAttr = "v$Version"
$ApkName = "Ghost_v$($Version)_Release.apk"

Write-Host "--- Ghost Command RELEASE AUTOMATION ($VersionAttr) ---" -ForegroundColor Magenta

# 0. Setup
$scriptDir = $PSScriptRoot
$gradleDistBin = "$scriptDir\android_app\gradle_dist\gradle-8.5\bin\gradle.bat"
$ghCli = "C:\Program Files\GitHub CLI\gh.exe"

# 1. Build Release
Set-Location "$scriptDir\android_app"
Write-Host "[1/5] Building RELEASE APK (clean assembleRelease)..." -ForegroundColor Cyan

& $gradleDistBin clean assembleRelease --stacktrace 2>&1 | Out-File -FilePath "build_release_log.txt" -Encoding UTF8

if (Select-String -Path "build_release_log.txt" -Pattern "BUILD FAILED") {
    Write-Error "Release Build Failed! See build_release_log.txt."
    exit 1
}

# 2. Locate and Rename
$rawApk = "$scriptDir\android_app\app\build\outputs\apk\release\app-release.apk" 
if (!(Test-Path $rawApk)) {
    Write-Error "APK NOT FOUND at $rawApk"
    exit 1
}

Set-Location $scriptDir
Copy-Item $rawApk -Destination "$scriptDir\$ApkName" -Force
Write-Host "[2/5] APK Prepared: $ApkName" -ForegroundColor Green

# 3. Git Operations
Write-Host "[3/5] Git Tagging ($VersionAttr)..." -ForegroundColor Cyan
git add .
git commit -m "Release ${VersionAttr}: Automated Build"
git tag $VersionAttr
git push origin master
git push origin $VersionAttr

# 4. GitHub Release
Write-Host "[4/5] Creating GitHub Release..." -ForegroundColor Cyan
& $ghCli release create $VersionAttr $ApkName --title "Ghost Mobile $VersionAttr" --notes "Automated Security Release $VersionAttr"

# 5. Trigger OTA
Write-Host "[5/5] Triggering OTA Update..." -ForegroundColor Cyan
python trigger_ota.py

Write-Host "--- MISSION ACCOMPLISHED: $VersionAttr IS LIVE ---" -ForegroundColor Green
