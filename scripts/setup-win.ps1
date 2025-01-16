$installDir = (Get-Location).Path
Write-Host "Installation directory: $installDir"

[System.Environment]::SetEnvironmentVariable("TRACE_SENTRY_DIR", $installDir, "User")
Write-Host "Installation directory added to environment variables."

$currentPath = [System.Environment]::GetEnvironmentVariable("Path", "User")
if ($currentPath -notlike "*$installDir*") {
    [System.Environment]::SetEnvironmentVariable("Path", "$currentPath;$installDir", "User")
    Write-Host "Installation directory added to PATH."
} else {
    Write-Host "Installation directory already in PATH."
}

# for the project submission we will provide the apikey.txt file.
$keyFilePath = Join-Path $installDir "apikey.txt"
if (Test-Path $keyFilePath) {
    $apiKey = Get-Content -Path $keyFilePath -Raw
    [System.Environment]::SetEnvironmentVariable("OPENAI_API_KEY", $apiKey, "User")
    Write-Host "OpenAI API key set."
} else {
    Write-Host "API key file not found. Set the environment variable OPENAI_API_KEY manually later."
}

$daemonFile = Join-Path $installDir "ts-daemon.bat"
$startupFolder = "$env:APPDATA\Microsoft\Windows\Start Menu\Programs\Startup"
if (Test-Path $daemonFile) {
    Copy-Item -Path $daemonFile -Destination $startupFolder -Force
    Write-Host "Daemon file copied to startup folder."
} else {
    Write-Host "Daemon file not found. Please make sure the file exists."
}

java -jar ".\bin\daemon.jar"
Write-Host "Setup complete please restart your terminal and type 'ts' to run TraceSentry."