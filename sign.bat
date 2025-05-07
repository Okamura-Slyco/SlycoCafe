@echo off
setlocal

REM Get version argument
set VERSION=%1

REM Replace dot with underscore
set VERSION_TAG=%VERSION:.=_%

REM Create output directory if it doesn't exist
mkdir output

REM Run apksigner and name output with version
apksigner sign ^
  --ks D:\Slyco\TopSecret\KeyStore.jks ^
  --v1-signing-enabled=true ^
  --v2-signing-enabled=false ^
  --v3-signing-enabled=false ^
  --out .\output\app-release-signed-%VERSION_TAG%.apk ^
  D:\AndroidStudioProjects\SlycoCafe\app\build\outputs\apk\release\app-release.apk

echo Signed APK saved as app-release-signed-%VERSION_TAG%.apk
endlocal
pause
