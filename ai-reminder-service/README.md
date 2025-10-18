# 🤖 Microservice IA Rappels NeuroCare

## 📋 Description

Microservice Python Flask pour la génération de rappels personnalisés pour les rendez-vous médicaux et thérapeutiques des enfants avec des besoins spéciaux.

## ✨ Fonctionnalités

- 🎯 **Génération de rappels personnalisés** selon le type de rendez-vous
- ⏰ **Calcul automatique de l'urgence** (URGENT, TODAY, TOMORROW, UPCOMING)
- 📦 **Traitement par lot** jusqu'à 50 rendez-vous
- 🌟 **Templates adaptés** par type (CONSULTATION, THERAPEUTIC, MEDICAL, EDUCATIONAL)
- 🔄 **Mode fallback** en cas d'erreur
- 📊 **Logging complet** avec rotation des fichiers
- 🌐 **CORS activé** pour Angular et Spring Boot

## 🚀 Installation

### Prérequis

- Python 3.8+
- pip3

### Installation rapide

```bash
# Cloner le projet
cd ai-reminder-service

# Installer les dépendances
pip3 install -r requirements.txt

# Démarrer le service
python3 app.py
```

### Scripts de démarrage

#### Linux/Mac
```bash
chmod +x start.sh
./start.sh
```

#### Windows
```cmd
start.bat
```

## 📡 Endpoints API

### 1. Health Check
```http
GET /health
```

**Réponse :**
```json
{
  "status": "healthy",
  "service": "ai-reminder-service",
  "version": "1.0.0",
  "timestamp": "2024-01-01T12:00:00",
  "uptime": "running",
  "endpoints": ["/health", "/generate-reminder", "/batch-reminders"]
}
```

### 2. Génération d'un rappel unique
```http
POST /generate-reminder
Content-Type: application/json

{
  "childName": "Alice",
  "professionalName": "Dr. Martin",
  "startTime": "2024-01-02T14:30:00",
  "type": "CONSULTATION",
  "location": "Cabinet médical",
  "notes": "Apporter le carnet de santé"
}
```

**Réponse :**
```json
{
  "success": true,
  "message": "Bonjour ! 📋 Rappel important : Alice a une consultation avec Dr. Martin demain à 14:30. 📍 Cabinet médical 📝 Apporter le carnet de santé",
  "urgency": "TOMORROW",
  "urgency_icon": "⏰",
  "urgency_color": "yellow",
  "appointment_type": "CONSULTATION",
  "child_name": "Alice",
  "professional_name": "Dr. Martin",
  "appointment_time": "2024-01-02T14:30:00",
  "location": "Cabinet médical",
  "notes": "Apporter le carnet de santé",
  "generated_at": "2024-01-01T12:00:00",
  "source": "ai_reminder_service",
  "version": "1.0.0"
}
```

### 3. Génération de rappels en lot
```http
POST /batch-reminders
Content-Type: application/json

{
  "appointments": [
    {
      "childName": "Alice",
      "professionalName": "Dr. Martin",
      "startTime": "2024-01-02T14:30:00",
      "type": "CONSULTATION"
    },
    {
      "childName": "Bob",
      "professionalName": "Mme. Dupont",
      "startTime": "2024-01-03T10:00:00",
      "type": "THERAPEUTIC"
    }
  ]
}
```

### 4. Récupération des templates
```http
GET /templates
```

## 🎯 Types de rendez-vous supportés

- **CONSULTATION** : Consultations médicales générales
- **THERAPEUTIC** : Séances thérapeutiques
- **MEDICAL** : Rendez-vous médicaux spécialisés
- **EDUCATIONAL** : Suivi éducatif

## ⏰ Niveaux d'urgence

- **URGENT** (🚨) : Dans les 2 heures
- **TODAY** (📅) : Aujourd'hui
- **TOMORROW** (⏰) : Demain
- **UPCOMING** (📋) : Dans la semaine

## 🔧 Configuration

### Variables d'environnement

```bash
# Port du service (défaut: 5000)
PORT=5000

# Mode debug (défaut: False)
DEBUG=True

# Niveau de log (défaut: INFO)
LOG_LEVEL=DEBUG

# Taille max des lots (défaut: 50)
MAX_BATCH_SIZE=50
```

### Configuration personnalisée

Modifiez `config.py` pour personnaliser :
- Templates de rappels
- Niveaux d'urgence
- Messages d'erreur
- Configuration CORS

## 📊 Logging

Les logs sont écrits dans :
- Console (niveau INFO)
- Fichier `ai_reminder_service.log`

Format des logs :
```
2024-01-01 12:00:00 - app - INFO - Génération rappel pour Alice - CONSULTATION
```

## 🧪 Tests

### Test manuel avec curl

```bash
# Health check
curl http://localhost:5000/health

# Génération d'un rappel
curl -X POST http://localhost:5000/generate-reminder \
  -H "Content-Type: application/json" \
  -d '{
    "childName": "Alice",
    "professionalName": "Dr. Martin",
    "startTime": "2024-01-02T14:30:00",
    "type": "CONSULTATION",
    "location": "Cabinet médical"
  }'
```

### Tests automatisés

```bash
# Installation des dépendances de test
pip install pytest pytest-flask

# Exécution des tests
pytest tests/
```

## 🚨 Gestion d'erreurs

Le service inclut :
- **Validation des données** d'entrée
- **Mode fallback** en cas d'erreur
- **Logging détaillé** des erreurs
- **Messages d'erreur** explicites
- **Codes de statut HTTP** appropriés

## 🔄 Intégration

### Avec Spring Boot

```java
@Value("${ai.microservice.url:http://localhost:5000}")
private String aiMicroserviceUrl;

public AIReminderResponse generateReminder(AIReminderRequest request) {
    String url = aiMicroserviceUrl + "/generate-reminder";
    // Appel HTTP vers le microservice
}
```

### Avec Angular

```typescript
@Injectable()
export class AIReminderService {
  private apiUrl = 'http://localhost:5000';
  
  generateReminder(data: ReminderRequest): Observable<ReminderResponse> {
    return this.http.post(`${this.apiUrl}/generate-reminder`, data);
  }
}
```

## 📈 Monitoring

### Métriques disponibles

- Nombre de rappels générés
- Taux d'erreur
- Temps de réponse
- Utilisation mémoire

### Health check

```bash
curl http://localhost:5000/health
```

## 🛠️ Développement

### Structure du projet

```
ai-reminder-service/
├── app.py              # Application principale
├── config.py           # Configuration
├── requirements.txt    # Dépendances Python
├── start.sh           # Script de démarrage Linux/Mac
├── start.bat          # Script de démarrage Windows
├── README.md          # Documentation
└── logs/              # Fichiers de logs
```

### Ajout de nouveaux templates

1. Modifiez `config.py`
2. Ajoutez le nouveau type dans `REMINDER_TEMPLATES`
3. Redémarrez le service

### Ajout de nouveaux niveaux d'urgence

1. Modifiez `config.py`
2. Ajoutez le nouveau niveau dans `URGENCY_LEVELS`
3. Mettez à jour la logique dans `ReminderGenerator`

## 📝 Changelog

### v1.0.0 (2024-01-01)
- ✨ Génération de rappels personnalisés
- 📦 Support des lots jusqu'à 50 rendez-vous
- ⏰ Calcul automatique de l'urgence
- 🌟 Templates par type de rendez-vous
- 🔄 Mode fallback robuste
- 📊 Logging complet
- 🌐 CORS activé

## 🤝 Support

Pour toute question ou problème :
1. Vérifiez les logs dans `ai_reminder_service.log`
2. Testez avec l'endpoint `/health`
3. Consultez la documentation des endpoints

## 📄 Licence

Ce projet fait partie du système NeuroCare et est destiné à un usage médical et thérapeutique.

