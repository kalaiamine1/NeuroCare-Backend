#!/bin/bash
# 🚀 Script de démarrage complet pour NeuroCare avec IA Rappels
# =============================================================

echo "🤖 Démarrage du système NeuroCare avec IA Rappels"
echo "================================================"

# Configuration des couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Fonction pour afficher les messages colorés
print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Vérification des prérequis
echo "🔍 Vérification des prérequis..."

# Vérification Java
if ! command -v java &> /dev/null; then
    print_error "Java n'est pas installé ou pas dans le PATH"
    exit 1
fi
print_success "Java détecté"

# Vérification Python
if ! command -v python3 &> /dev/null; then
    print_error "Python 3 n'est pas installé ou pas dans le PATH"
    exit 1
fi
print_success "Python 3 détecté"

# Vérification Maven
if ! command -v mvn &> /dev/null; then
    print_error "Maven n'est pas installé ou pas dans le PATH"
    exit 1
fi
print_success "Maven détecté"

# Vérification Node.js
if ! command -v node &> /dev/null; then
    print_error "Node.js n'est pas installé ou pas dans le PATH"
    exit 1
fi
print_success "Node.js détecté"

echo
echo "📦 Démarrage des services..."
echo

# 1. Démarrage du microservice Python IA
echo "🤖 [1/3] Démarrage du microservice IA Python..."
cd ai-reminder-service
nohup python3 app.py > ../logs/ai-service.log 2>&1 &
AI_PID=$!
cd ..
sleep 3

# Vérification que le service Python est démarré
if curl -s http://localhost:5000/health > /dev/null; then
    print_success "Microservice IA démarré sur le port 5000 (PID: $AI_PID)"
else
    print_warning "Microservice IA en cours de démarrage..."
fi

# 2. Démarrage du backend Spring Boot
echo "🍃 [2/3] Démarrage du backend Spring Boot..."
nohup mvn spring-boot:run > logs/spring-boot.log 2>&1 &
SPRING_PID=$!
sleep 5

# Vérification que le service Spring Boot est démarré
if curl -s http://localhost:8089/api/v1/ai-reminders/health > /dev/null; then
    print_success "Backend Spring Boot démarré sur le port 8089 (PID: $SPRING_PID)"
else
    print_warning "Backend Spring Boot en cours de démarrage..."
fi

# 3. Démarrage du frontend Angular
echo "🅰️ [3/3] Démarrage du frontend Angular..."
cd ../NeuroCare_Front
nohup npm start > ../NeuroCare-Backend/logs/angular.log 2>&1 &
ANGULAR_PID=$!
cd ../NeuroCare-Backend
sleep 3

print_success "Frontend Angular démarré sur le port 4200 (PID: $ANGULAR_PID)"

echo
echo "🎉 Tous les services sont démarrés !"
echo "===================================="
echo
echo "📋 Services disponibles:"
echo "   🤖 Microservice IA:    http://localhost:5000"
echo "   🍃 Backend Spring Boot: http://localhost:8089"
echo "   🅰️ Frontend Angular:    http://localhost:4200"
echo
echo "📡 Endpoints IA Rappels:"
echo "   GET  http://localhost:5000/health"
echo "   POST http://localhost:5000/generate-reminder"
echo "   POST http://localhost:5000/batch-reminders"
echo "   GET  http://localhost:5000/templates"
echo
echo "📡 Endpoints Spring Boot:"
echo "   GET  http://localhost:8089/api/v1/ai-reminders/health"
echo "   POST http://localhost:8089/api/v1/appointments/{id}/generate-reminder"
echo "   GET  http://localhost:8089/api/v1/appointments/{id}/preview-reminder"
echo "   POST http://localhost:8089/api/v1/appointments/parent/{parentId}/generate-reminders"
echo
echo "🧪 Tests disponibles:"
echo "   python3 ai-reminder-service/test_service.py"
echo
echo "📊 PIDs des services:"
echo "   Microservice IA: $AI_PID"
echo "   Spring Boot: $SPRING_PID"
echo "   Angular: $ANGULAR_PID"
echo
echo "🛑 Pour arrêter tous les services:"
echo "   kill $AI_PID $SPRING_PID $ANGULAR_PID"
echo

# Sauvegarde des PIDs
echo "$AI_PID" > logs/ai-service.pid
echo "$SPRING_PID" > logs/spring-boot.pid
echo "$ANGULAR_PID" > logs/angular.pid

print_info "PIDs sauvegardés dans le dossier logs/"
print_info "Logs disponibles dans le dossier logs/"

# Attendre que l'utilisateur appuie sur une touche
read -p "Appuyez sur Entrée pour continuer..."
