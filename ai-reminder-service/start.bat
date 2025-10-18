@echo off
REM 🚀 Script de démarrage Windows pour le Microservice IA Rappels
REM ==============================================================

echo 🤖 Démarrage du Microservice IA Rappels NeuroCare
echo =================================================

REM Vérification de Python
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Python n'est pas installé ou pas dans le PATH
    pause
    exit /b 1
)

REM Vérification de pip
pip --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ pip n'est pas installé ou pas dans le PATH
    pause
    exit /b 1
)

REM Vérification du port
set PORT=%PORT%
if "%PORT%"=="" set PORT=5000

REM Vérification si le port est utilisé
netstat -an | find ":%PORT%" | find "LISTENING" >nul
if %errorlevel% equ 0 (
    echo ⚠️  Le port %PORT% est déjà utilisé
    echo    Tentative d'arrêt du processus...
    for /f "tokens=5" %%a in ('netstat -ano ^| find ":%PORT%" ^| find "LISTENING"') do (
        taskkill /PID %%a /F >nul 2>&1
    )
    timeout /t 2 >nul
)

REM Installation des dépendances
echo 📦 Installation des dépendances...
pip install -r requirements.txt

if %errorlevel% neq 0 (
    echo ❌ Erreur lors de l'installation des dépendances
    pause
    exit /b 1
)

REM Configuration de l'environnement
set FLASK_APP=app.py
set FLASK_ENV=development
set PORT=%PORT%

REM Création du répertoire de logs
if not exist logs mkdir logs

REM Démarrage du service
echo 🚀 Démarrage du microservice sur le port %PORT%...
echo 📋 Endpoints disponibles:
echo    - GET  http://localhost:%PORT%/health
echo    - POST http://localhost:%PORT%/generate-reminder
echo    - POST http://localhost:%PORT%/batch-reminders
echo    - GET  http://localhost:%PORT%/templates
echo.
echo 🛑 Pour arrêter le service, appuyez sur Ctrl+C
echo.

REM Démarrage avec gestion des erreurs
python app.py

if %errorlevel% neq 0 (
    echo ❌ Erreur lors du démarrage du service
    pause
    exit /b 1
)

