@echo off

call gradlew.bat jar

for /f %%i in ('git rev-parse --short HEAD') do set COMMIT=%%i
echo Commit: %COMMIT%

set REMOTE_PATH=/www/builds/%COMMIT%

ssh host "mkdir -p %REMOTE_PATH%"

scp build\libs\esco-plugin-v2.jar host:%REMOTE_PATH%/plugin.jar

ssh host "echo %COMMIT% > /www/builds/latest.txt"

echo Done: %REMOTE_PATH% (latest = %COMMIT%)