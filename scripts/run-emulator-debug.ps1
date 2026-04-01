param(
    [string]$AvdName = "Medium_Phone_API_36.1"
)

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$localProperties = Join-Path $projectRoot "local.properties"

if (-not (Test-Path $localProperties)) {
    throw "local.properties not found at $localProperties"
}

$sdkLine = Get-Content $localProperties | Select-String '^sdk.dir=' | Select-Object -First 1
if (-not $sdkLine) {
    throw "sdk.dir not found in local.properties"
}

$sdkDir = $sdkLine.ToString().Split('=')[1]
$adb = Join-Path $sdkDir "platform-tools\adb.exe"
$emulator = Join-Path $sdkDir "emulator\emulator.exe"
$apkPath = Join-Path $projectRoot "app\build\outputs\apk\debug\app-debug.apk"

Push-Location $projectRoot
try {
    .\gradlew.bat assembleDebug

    $runningDevices = & $adb devices
    if ($runningDevices -notmatch "emulator-\d+\s+device") {
        Start-Process -FilePath $emulator -ArgumentList "-avd", $AvdName

        for ($i = 0; $i -lt 60; $i++) {
            $boot = & $adb shell getprop sys.boot_completed 2>$null
            if ($boot -match "1") {
                break
            }
            Start-Sleep -Seconds 5
        }
    }

    & $adb install -r $apkPath
    & $adb shell monkey -p com.financetracker -c android.intent.category.LAUNCHER 1
}
finally {
    Pop-Location
}
