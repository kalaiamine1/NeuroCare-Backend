#!/bin/bash
# 🚀 Script de démarrage pour le Microservice IA Rappels
# =====================================================

echo "🤖 Démarrage du Microservice IA Rappels NeuroCare"
echo "================================================="

# Vérification de Python
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 n'est pas installé"
    exit 1
fi

# Vérification de pip
if ! command -v pip3 &> /dev/null; then
    echo "❌ pip3 n'est pas installé"
    exit 1
fi

# Vérification du port
PORT=${PORT:-5000}
if lsof -Pi :$PORT -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo "⚠️  Le port $PORT est déjà utilisé"
    echo "   Arrêt du processus existant..."
    lsof -ti:$PORT | xargs kill -9 2>/dev/null || true
    sleep 2
fi

# Installation des dépendances
echo "📦 Installation des dépendances..."
pip3 install -r requirements.txt

# Vérification de l'installation
if [ $? -ne 0 ]; then
    echo "❌ Erreur lors de l'installation des dépendances"
    exit 1
fi

# Configuration de l'environnement
export FLASK_APP=app.py
export FLASK_ENV=development
export PORT=$PORT

# Création du répertoire de logs
mkdir -p logs

# Démarrage du service
echo "🚀 Démarrage du microservice sur le port $PORT..."
echo "📋 Endpoints disponibles:"
echo "   - GET  http://localhost:$PORT/health"
echo "   - POST http://localhost:$PORT/generate-reminder"
echo "   - POST http://localhost:$PORT/batch-reminders"
echo "   - GET  http://localhost:$PORT/templates"
echo ""
echo "🛑 Pour arrêter le service, appuyez sur Ctrl+C"
echo ""

# Démarrage avec gestion des erreurs
python3 app.py

