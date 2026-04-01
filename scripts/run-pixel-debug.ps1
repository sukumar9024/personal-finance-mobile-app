param(
    [string]$Serial = ""
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
$apkPath = Join-Path $projectRoot "app\build\outputs\apk\debug\app-debug.apk"

$deviceArgs = @()
if ($Serial) {
    $deviceArgs = @("-s", $Serial)
}

Push-Location $projectRoot
try {
    .\gradlew.bat assembleDebug
    & $adb @deviceArgs devices
    & $adb @deviceArgs install -r $apkPath
    & $adb @deviceArgs shell monkey -p com.financetracker -c android.intent.category.LAUNCHER 1
}
finally {
    Pop-Location
}
