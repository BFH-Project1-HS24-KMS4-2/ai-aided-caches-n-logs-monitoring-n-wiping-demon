Set-Location -Path "./src"
mvn clean package -DskipTests
if (-not (Test-Path -Path "../bin")) {
    New-Item -ItemType Directory -Path "../bin" | Out-Null
}
Copy-Item -Path "./daemon/target/daemon.jar" -Destination "../bin" -Force
Copy-Item -Path "./cli/target/cli.jar" -Destination "../bin" -Force

Set-Location -Path "../"