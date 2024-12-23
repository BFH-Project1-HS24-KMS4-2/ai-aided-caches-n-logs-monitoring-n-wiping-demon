@echo off
setlocal

:: check if the first argument is empty
if "%~1"=="" (
    java -Dlogging.level.root=OFF -Dspring.profiles.active=dev -jar "%TRACE_SENTRY_DIR%\bin\cli.jar"
) else (
    java -Dlogging.level.root=OFF -jar "%TRACE_SENTRY_DIR%\bin\cli.jar" %*
)

endlocal
