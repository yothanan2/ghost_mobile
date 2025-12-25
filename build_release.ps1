Write-Host "--- Ghost Command RELEASE BUILD v2.00 (Signed) ---" -ForegroundColor Magenta

# 0. Setup
$scriptDir = $PSScriptRoot
# ... (lines 4-29 skipped in target content for brevity, assuming tool context handles it, but safer to just update title and then update bottom separate) 
# I'll update title first.
$gradleDistBin = "$scriptDir\android_app\gradle_dist\gradle-8.5\bin\gradle.bat"
$gradleCmd = $gradleDistBin

# 1. Build Release
Set-Location "$scriptDir\android_app"
Write-Host "Building RELEASE APK (clean assembleRelease)... Logging to build_release_log.txt" -ForegroundColor Cyan

& $gradleCmd clean assembleRelease --stacktrace 2>&1 | Out-File -FilePath "build_release_log.txt" -Encoding UTF8

# 2. Check Result
if (Select-String -Path "build_release_log.txt" -Pattern "BUILD FAILED") {
    Write-Error "Release Build Failed! Checking log..."
    Get-Content "build_release_log.txt" -Tail 50
    exit 1
}

# 3. Locate APK
$apkPath = "$scriptDir\android_app\app\build\outputs\apk\release\app-release.apk" 
# Note: Now signed, so it should be app-release.apk

if (Test-Path $apkPath) {
    Write-Host "SUCCESS: Release APK Generated at:" -ForegroundColor Green
    Write-Host $apkPath -ForegroundColor White
    
    Copy-Item $apkPath -Destination "$scriptDir\Ghost_v2.00_Release.apk"
    Write-Host "Copied to Desktop/Ghost_Mobile_App/Ghost_v2.00_Release.apk" -ForegroundColor Green
} else {
    Write-Error "APK NOT FOUND. See build_release_log.txt."
}
