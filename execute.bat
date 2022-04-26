java -jar CSGOCaseStatsViewer-1.0.6-jar-with-dependencies.jar
echo off
if "%ERRORLEVEL%" EQU "1" (
echo It looks like you cloned the repository instead of downloading the newest release.
echo Download it from https://github.com/cantryDev/CSGOCaseStatsViewer/releases/latest
echo You will be redirected to the download page in 20 seconds
timeout 20
start "" https://github.com/cantryDev/CSGOCaseStatsViewer/releases/latest
)
if "%ERRORLEVEL%" EQU "9009" (
java -version
if "%ERRORLEVEL%" EQU "9009" (
echo You need Java 1.8 or higher to use this program
)
)
pause
pause
pause