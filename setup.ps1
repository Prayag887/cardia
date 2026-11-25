$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$expected = '2db75c40782f5e8ba1fc278a5574bab070adccb2d21ca5a6e5ed840888448046'
$target = Join-Path $PSScriptRoot 'gradle/wrapper/gradle-wrapper.jar'
if ((Test-Path $target) -and ((Get-FileHash $target -Algorithm SHA256).Hash.ToLower() -eq $expected)) {
    Write-Host 'Official Gradle wrapper verified. Open this folder in Android Studio.'
    exit 0
}
$temporary = Join-Path (Split-Path $target) ('download-' + [guid]::NewGuid().ToString() + '.tmp')
try {
    Invoke-WebRequest 'https://raw.githubusercontent.com/gradle/gradle/v8.11.1/gradle/wrapper/gradle-wrapper.jar' -OutFile $temporary -UseBasicParsing
    if ((Get-FileHash $temporary -Algorithm SHA256).Hash.ToLower() -ne $expected) { throw 'Wrapper checksum mismatch; refusing to execute.' }
    Move-Item $temporary $target -Force
    Write-Host 'Ready. Open this folder in Android Studio, or run .\gradlew.bat :androidApp:assembleDebug'
} finally { if (Test-Path $temporary) { Remove-Item $temporary } }
