Write-Host "--- Ghost Build System Bootstrap ---" -ForegroundColor Cyan

$gradleVersion = "8.5"
$zipUrl = "https://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip"
$destDir = "$PSScriptRoot\android_app\gradle_dist"
$gradleBin = "$destDir\gradle-$gradleVersion\bin\gradle.bat"

# Create directory
if (!(Test-Path $destDir)) {
    New-Item -ItemType Directory -Path $destDir | Out-Null
}

# Check if already installed
if (Test-Path $gradleBin) {
    Write-Host "Gradle $gradleVersion already found at: $gradleBin" -ForegroundColor Green
    exit 0
}

# Download
Write-Host "Downloading Gradle $gradleVersion from: $zipUrl" -ForegroundColor Yellow
$zipFile = "$destDir\gradle.zip"
Invoke-WebRequest -Uri $zipUrl -OutFile $zipFile

# Extract
Write-Host "Extracting Gradle..." -ForegroundColor Yellow
Expand-Archive -Path $zipFile -DestinationPath $destDir -Force

# Cleanup
Remove-Item $zipFile

Write-Host "Gradle setup complete." -ForegroundColor Green
Write-Host "Location: $gradleBin"
