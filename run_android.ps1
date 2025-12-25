Write-Host "--- Ghost Command Deployment (Windows/Debug) ---" -ForegroundColor Cyan

# 0. Setup Local Build Env (Auto-Bootstrap)
$scriptDir = $PSScriptRoot
$gradleDistBin = "$scriptDir\android_app\gradle_dist\gradle-8.5\bin\gradle.bat"
$gradleCmd = $gradleDistBin # Assuming it exists now

# 2. Build & Install (CLEAN + INFO) -> Log to file
Set-Location "$scriptDir\android_app"
Write-Host "Building (clean installDebug --info)... Logging to build_log.txt" -ForegroundColor Cyan

& $gradleCmd clean installDebug --info 2>&1 | Out-File -FilePath "build_log.txt" -Encoding UTF8

# 3. Check Log for errors
if (Select-String -Path "build_log.txt" -Pattern "BUILD FAILED") {
    Write-Error "Build Failed! Checking log tail..."
    Get-Content "build_log.txt" -Tail 20
    exit 1
}

# 3. Check for APK
$apkPath = "$scriptDir\android_app\app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apkPath) {
    Write-Host "APK Generated at: $apkPath" -ForegroundColor Green
    
    # Get list of devices (skipping first line which is 'List of devices attached')
    $devices = adb devices | Select-Object -Skip 1 | Where-Object { $_ -match "\w+\s+device" } | ForEach-Object { $_.Split("`t")[0] }

    if ($devices) {
        foreach ($device in $devices) {
            Write-Host "Installing on device: $device" -ForegroundColor Yellow
            adb -s $device install -r $apkPath
            Write-Host "Launching on device: $device" -ForegroundColor Yellow
            adb -s $device shell am start -n com.ghostcommand.app/.MainActivity
        }
    } else {
        Write-Warning "No active devices found to install."
    }
} else {
    Write-Error "APK NOT FOUND. See build_log.txt for details."
}

# 4. Launch
adb shell am start -n com.ghostcommand.app/.MainActivity
