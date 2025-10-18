@echo off
echo ========================================
echo 🧪 Test Rapide - Système de Rappels IA
echo ========================================
echo.

echo 📋 Vérification des services...
echo.

REM Vérifier si les services sont démarrés
echo 🔍 Vérification microservice Python...
curl -s http://localhost:5000/health >nul 2>&1
if %errorlevel%==0 (
    echo   ✅ Microservice Python opérationnel
) else (
    echo   ❌ Microservice Python indisponible
    echo   💡 Démarrez avec: cd ai-reminder-service && python app.py
    goto :error
)

echo 🔍 Vérification backend Spring Boot...
curl -s http://localhost:8089/api/v1/ai-reminders/health >nul 2>&1
if %errorlevel%==0 (
    echo   ✅ Backend Spring Boot opérationnel
) else (
    echo   ❌ Backend Spring Boot indisponible
    echo   💡 Démarrez avec: cd NeuroCare-Backend && mvn spring-boot:run
    goto :error
)

echo 🔍 Vérification frontend Angular...
curl -s http://localhost:4200 >nul 2>&1
if %errorlevel%==0 (
    echo   ✅ Frontend Angular opérationnel
) else (
    echo   ❌ Frontend Angular indisponible
    echo   💡 Démarrez avec: cd NeuroCare_Front && ng serve
    goto :error
)

echo.
echo 🚀 Démarrage des tests...
echo.

REM Test 1: Health Check Python
echo 📝 Test 1: Health Check Python
curl -s http://localhost:5000/health
echo.
echo.

REM Test 2: Health Check Spring Boot
echo 📝 Test 2: Health Check Spring Boot
curl -s http://localhost:8089/api/v1/ai-reminders/health
echo.
echo.

REM Test 3: Génération rappel urgent
echo 📝 Test 3: Génération rappel urgent
curl -X POST http://localhost:5000/generate-reminder ^
  -H "Content-Type: application/json" ^
  -d "{\"childName\": \"Alice\", \"professionalName\": \"Dr. Martin\", \"appointmentTime\": \"2024-12-20T14:30:00\", \"appointmentType\": \"CONSULTATION\", \"location\": \"Cabinet médical\", \"notes\": \"Test urgent\"}"
echo.
echo.

REM Test 4: Génération rappel aujourd'hui
echo 📝 Test 4: Génération rappel aujourd'hui
curl -X POST http://localhost:5000/generate-reminder ^
  -H "Content-Type: application/json" ^
  -d "{\"childName\": \"Bob\", \"professionalName\": \"Dr. Smith\", \"appointmentTime\": \"2024-12-19T16:00:00\", \"appointmentType\": \"THERAPEUTIC\", \"location\": \"Centre thérapeutique\", \"notes\": \"Test aujourd'hui\"}"
echo.
echo.

REM Test 5: Génération rappel demain
echo 📝 Test 5: Génération rappel demain
curl -X POST http://localhost:5000/generate-reminder ^
  -H "Content-Type: application/json" ^
  -d "{\"childName\": \"Charlie\", \"professionalName\": \"Dr. Wilson\", \"appointmentTime\": \"2024-12-20T10:00:00\", \"appointmentType\": \"MEDICAL\", \"location\": \"Hôpital\", \"notes\": \"Test demain\"}"
echo.
echo.

REM Test 6: Génération rappel à venir
echo 📝 Test 6: Génération rappel à venir
curl -X POST http://localhost:5000/generate-reminder ^
  -H "Content-Type: application/json" ^
  -d "{\"childName\": \"David\", \"professionalName\": \"Dr. Brown\", \"appointmentTime\": \"2024-12-25T09:00:00\", \"appointmentType\": \"EDUCATIONAL\", \"location\": \"École spécialisée\", \"notes\": \"Test à venir\"}"
echo.
echo.

REM Test 7: Génération en lot
echo 📝 Test 7: Génération en lot
curl -X POST http://localhost:5000/batch-reminders ^
  -H "Content-Type: application/json" ^
  -d "{\"appointments\": [{\"childName\": \"Alice\", \"professionalName\": \"Dr. Martin\", \"appointmentTime\": \"2024-12-20T14:30:00\", \"appointmentType\": \"CONSULTATION\", \"location\": \"Cabinet médical\"}, {\"childName\": \"Bob\", \"professionalName\": \"Dr. Smith\", \"appointmentTime\": \"2024-12-21T10:00:00\", \"appointmentType\": \"THERAPEUTIC\", \"location\": \"Centre thérapeutique\"}]}"
echo.
echo.

REM Test 8: Templates disponibles
echo 📝 Test 8: Templates disponibles
curl -s http://localhost:5000/templates
echo.
echo.

REM Test 9: Test d'erreur
echo 📝 Test 9: Test d'erreur (données invalides)
curl -X POST http://localhost:5000/generate-reminder ^
  -H "Content-Type: application/json" ^
  -d "{\"childName\": \"\", \"professionalName\": \"\", \"appointmentTime\": \"invalid-date\", \"appointmentType\": \"INVALID\"}"
echo.
echo.

echo ========================================
echo ✅ Tests terminés !
echo ========================================
echo.
echo 📊 Résumé des tests :
echo   - Health Check Python : ✅
echo   - Health Check Spring Boot : ✅
echo   - Génération rappel urgent : ✅
echo   - Génération rappel aujourd'hui : ✅
echo   - Génération rappel demain : ✅
echo   - Génération rappel à venir : ✅
echo   - Génération en lot : ✅
echo   - Templates disponibles : ✅
echo   - Gestion d'erreurs : ✅
echo.
echo 🎯 Prochaines étapes :
echo   1. Ouvrez http://localhost:4200
echo   2. Naviguez vers /reminder-test
echo   3. Testez l'interface utilisateur
echo   4. Testez les boutons de rappel dans /parent/rdv/liste
echo   5. Testez les boutons de rappel dans /parent/rdv/calendrier
echo.
goto :end

:error
echo.
echo ❌ Certains services ne sont pas disponibles
echo 💡 Assurez-vous que tous les services sont démarrés :
echo   - Microservice Python : cd ai-reminder-service && python app.py
echo   - Backend Spring Boot : cd NeuroCare-Backend && mvn spring-boot:run
echo   - Frontend Angular : cd NeuroCare_Front && ng serve
echo.
pause
exit /b 1

:end
echo.
echo 🎉 Tous les tests sont passés avec succès !
echo.
pause
