param(
    [switch]$Clean,
    [switch]$Release,
    [switch]$Install,
    [switch]$InstallOnly,
    [switch]$Launch,
    [string]$DeviceId
)

$ErrorActionPreference = "Stop"

# Explicitly ensure Java 17 and Android SDK environment
$env:JAVA_HOME = "C:\Users\matth\scoop\apps\openjdk17\current"
$env:ANDROID_HOME = "C:\Users\matth\scoop\apps\android-clt\current"
$env:PATH = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:PATH"

$RepoRoot = $PSScriptRoot
Set-Location $RepoRoot

function Resolve-TargetPhoneDeviceId {
    param([string]$ExplicitId)
    if ($ExplicitId) { return $ExplicitId }

    $devicesRaw = & adb devices -l
    $deviceLines = $devicesRaw | Where-Object { $_ -match '\s+device\s+' -and $_ -notmatch '^List of devices' }
    if (-not $deviceLines) { return $null }

    # 1. Prioritize Pixel / Phone identifiers
    $phoneLine = $deviceLines | Where-Object {
        $_ -match 'model:Pixel' -or
        $_ -match 'product:blazer' -or
        $_ -match 'adb-57141FDCH0013P' -or
        $_ -match '192\.168\.0\.162'
    } | Select-Object -First 1

    if ($phoneLine) {
        return ($phoneLine -split '\s+')[0]
    }

    # 2. Exclude known Shield or TV devices
    $nonTvLine = $deviceLines | Where-Object {
        $_ -notmatch 'SHIELD' -and
        $_ -notmatch 'mdarcy' -and
        $_ -notmatch '192\.168\.0\.224'
    } | Select-Object -First 1

    if ($nonTvLine) {
        return ($nonTvLine -split '\s+')[0]
    }

    # 3. Fallback to first available device
    return ($deviceLines[0] -split '\s+')[0]
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  TwitchRSS Android Build Routine" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Repository:   $RepoRoot"
Write-Host "JAVA_HOME:    $env:JAVA_HOME"
Write-Host "ANDROID_HOME: $env:ANDROID_HOME"

if ($Clean -and -not $InstallOnly) {
    Write-Host "`n[1/3] Cleaning build cache..." -ForegroundColor Yellow
    cmd.exe /c "gradlew.bat clean"
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle clean failed with exit code $LASTEXITCODE"
    }
}

$BuildType = if ($Release) { "release" } else { "debug" }
$ApkDir = Join-Path $RepoRoot "app\build\outputs\apk\github\$BuildType"

if (-not $InstallOnly) {
    $Task = if ($Release) { "assembleGithubRelease" } else { "assembleGithubDebug" }
    Write-Host "`nExecuting Gradle Task: $Task..." -ForegroundColor Yellow

    $Stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    cmd.exe /c "gradlew.bat $Task --stacktrace"
    $Stopwatch.Stop()

    if ($LASTEXITCODE -ne 0) {
        throw "Gradle build failed with exit code $LASTEXITCODE"
    }
    Write-Host "`nBuild Succeeded in $($Stopwatch.Elapsed.ToString('mm\:ss'))!" -ForegroundColor Green
}

$Apk = Get-ChildItem -Path $ApkDir -Filter "TwitchRSS-*.apk" -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1
if (-not $Apk) {
    $Apk = Get-ChildItem -Path $ApkDir -Filter "*.apk" -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1
}

if (-not $Apk) {
    throw "Output APK was not found in $ApkDir. Please run a build first."
}

$SizeMB = [math]::Round($Apk.Length / 1MB, 2)
Write-Host "Output APK: $($Apk.FullName) ($SizeMB MB)" -ForegroundColor Green

if ($Install -or $InstallOnly) {
    $TargetId = Resolve-TargetPhoneDeviceId -ExplicitId $DeviceId
    if (-not $TargetId) {
        throw "No authorized Android phone detected. Run 'adb devices -l' to check connection."
    }

    $DeviceModel = (& adb -s $TargetId shell getprop ro.product.model 2>$null).Trim()
    Write-Host "`nTarget Phone: $TargetId ($DeviceModel)" -ForegroundColor Cyan
    Write-Host "Installing APK to $DeviceModel via ADB..." -ForegroundColor Yellow

    & adb -s $TargetId install -r "$($Apk.FullName)"
    if ($LASTEXITCODE -ne 0) {
        Write-Warning "ADB install failed with exit code $LASTEXITCODE."
    } else {
        Write-Host "Successfully installed TwitchRSS on $DeviceModel!" -ForegroundColor Green
        if ($Launch) {
            Write-Host "Launching TwitchRSS..." -ForegroundColor Yellow
            & adb -s $TargetId shell am start -n "me.ash.twitchrss/me.ash.reader.infrastructure.android.MainActivity"
        }
    }
}
