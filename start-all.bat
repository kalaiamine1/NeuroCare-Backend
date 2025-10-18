@echo off
REM 🚀 Script de démarrage complet pour NeuroCare avec IA Rappels
REM =============================================================

echo 🤖 Démarrage du système NeuroCare avec IA Rappels
echo ================================================

REM Configuration des couleurs
color 0A

REM Vérification des prérequis
echo 🔍 Vérification des prérequis...

REM Vérification Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java n'est pas installé ou pas dans le PATH
    pause
    exit /b 1
)
echo ✅ Java détecté

REM Vérification Python
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Python n'est pas installé ou pas dans le PATH
    pause
    exit /b 1
)
echo ✅ Python détecté

REM Vérification Maven
mvn --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven n'est pas installé ou pas dans le PATH
    pause
    exit /b 1
)
echo ✅ Maven détecté

REM Vérification Node.js
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Node.js n'est pas installé ou pas dans le PATH
    pause
    exit /b 1
)
echo ✅ Node.js détecté

echo.
echo 📦 Démarrage des services...
echo.

REM 1. Démarrage du microservice Python IA
echo 🤖 [1/3] Démarrage du microservice IA Python...
cd ai-reminder-service
start "Microservice IA" cmd /k "python app.py"
cd ..
timeout /t 3 >nul
echo ✅ Microservice IA démarré sur le port 5000

REM 2. Démarrage du backend Spring Boot
echo 🍃 [2/3] Démarrage du backend Spring Boot...
start "Backend Spring Boot" cmd /k "mvn spring-boot:run"
timeout /t 5 >nul
echo ✅ Backend Spring Boot démarré sur le port 8089

REM 3. Démarrage du frontend Angular
echo 🅰️ [3/3] Démarrage du frontend Angular...
cd ..\NeuroCare_Front
start "Frontend Angular" cmd /k "npm start"
cd ..\NeuroCare-Backend
timeout /t 3 >nul
echo ✅ Frontend Angular démarré sur le port 4200

echo.
echo 🎉 Tous les services sont démarrés !
echo ====================================
echo.
echo 📋 Services disponibles:
echo    🤖 Microservice IA:    http://localhost:5000
echo    🍃 Backend Spring Boot: http://localhost:8089
echo    🅰️ Frontend Angular:    http://localhost:4200
echo.
echo 📡 Endpoints IA Rappels:
echo    GET  http://localhost:5000/health
echo    POST http://localhost:5000/generate-reminder
echo    POST http://localhost:5000/batch-reminders
echo    GET  http://localhost:5000/templates
echo.
echo 📡 Endpoints Spring Boot:
echo    GET  http://localhost:8089/api/v1/ai-reminders/health
echo    POST http://localhost:8089/api/v1/appointments/{id}/generate-reminder
echo    GET  http://localhost:8089/api/v1/appointments/{id}/preview-reminder
echo    POST http://localhost:8089/api/v1/appointments/parent/{parentId}/generate-reminders
echo.
echo 🧪 Tests disponibles:
echo    python ai-reminder-service\test_service.py
echo.
echo 🛑 Pour arrêter tous les services, fermez les fenêtres de commande
echo.

REM Attendre que l'utilisateur appuie sur une touche
pause
