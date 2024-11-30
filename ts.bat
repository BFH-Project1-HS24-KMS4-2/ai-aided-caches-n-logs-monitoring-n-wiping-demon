@echo off
setlocal

cd /d "%~dp0"

:: check if the first argument is empty
if "%~1"=="" (
    java -Dlogging.level.root=OFF -Dspring.profiles.active=dev -jar "cli\target\cli-0.0.1-SNAPSHOT.jar"
) else (
    java -Dlogging.level.root=OFF -jar "cli\target\cli-0.0.1-SNAPSHOT.jar" %*
)

endlocal
