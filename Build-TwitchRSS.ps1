param(
    [switch]$Clean,
    [switch]$Release,
    [switch]$Install,
    [switch]$Launch
)

$ErrorActionPreference = "Stop"

# Explicitly ensure Java 17 and Android SDK environment
$env:JAVA_HOME = "C:\Users\matth\scoop\apps\openjdk17\current"
$env:ANDROID_HOME = "C:\Users\matth\scoop\apps\android-clt\current"
$env:PATH = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:PATH"

$RepoRoot = $PSScriptRoot
Set-Location $RepoRoot

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  TwitchRSS Android Build Routine" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Repository:   $RepoRoot"
Write-Host "JAVA_HOME:    $env:JAVA_HOME"
Write-Host "ANDROID_HOME: $env:ANDROID_HOME"

if ($Clean) {
    Write-Host "`n[1/3] Cleaning build cache..." -ForegroundColor Yellow
    cmd.exe /c "gradlew.bat clean"
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle clean failed with exit code $LASTEXITCODE"
    }
}

$Task = if ($Release) { "assembleGithubRelease" } else { "assembleGithubDebug" }
Write-Host "`nExecuting Gradle Task: $Task..." -ForegroundColor Yellow

$Stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
cmd.exe /c "gradlew.bat $Task --stacktrace"
$Stopwatch.Stop()

if ($LASTEXITCODE -ne 0) {
    throw "Gradle build failed with exit code $LASTEXITCODE"
}

$BuildType = if ($Release) { "release" } else { "debug" }
$ApkDir = Join-Path $RepoRoot "app\build\outputs\apk\github\$BuildType"
$Apk = Get-ChildItem -Path $ApkDir -Filter "TwitchRSS-*.apk" -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1

if (-not $Apk) {
    # Fallback search if naming differed
    $Apk = Get-ChildItem -Path $ApkDir -Filter "*.apk" -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1
}

if (-not $Apk) {
    throw "Build completed, but output APK was not found in $ApkDir"
}

$SizeMB = [math]::Round($Apk.Length / 1MB, 2)
Write-Host "`nBuild Succeeded in $($Stopwatch.Elapsed.ToString('mm\:ss'))!" -ForegroundColor Green
Write-Host "Output APK: $($Apk.FullName) ($SizeMB MB)" -ForegroundColor Green

if ($Install) {
    Write-Host "`nInstalling to connected Android device via ADB..." -ForegroundColor Yellow
    & adb install -r "$($Apk.FullName)"
    if ($LASTEXITCODE -ne 0) {
        Write-Warning "ADB install failed. Make sure your device is connected and authorized."
    } else {
        Write-Host "Successfully installed TwitchRSS on device!" -ForegroundColor Green
        if ($Launch) {
            Write-Host "Launching TwitchRSS..." -ForegroundColor Yellow
            & adb shell am start -n "me.ash.twitchrss/me.ash.reader.infrastructure.android.MainActivity"
        }
    }
}
