@echo off
echo ========================================
echo Stopping any running Java processes...
echo ========================================
taskkill /F /IM java.exe 2>nul
timeout /t 2 >nul

echo.
echo ========================================
echo Building the backend...
echo ========================================
call mvnw.cmd clean package -DskipTests

echo.
echo ========================================
echo Starting the backend...
echo ========================================
echo Look for this message:
echo    ✅ Razorpay client initialized successfully!
echo    ✅ Using Key ID: rzp_test_RcKaD36uYcqBWS
echo.
call mvnw.cmd spring-boot:run
