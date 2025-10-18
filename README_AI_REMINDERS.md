# 🤖 Système de Rappels IA NeuroCare

## 📋 Vue d'ensemble

Ce système intègre un microservice Python Flask avec le backend Spring Boot et le frontend Angular existants pour générer des rappels personnalisés pour les rendez-vous médicaux et thérapeutiques.

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Backend       │    │   Microservice  │
│   Angular 19    │◄──►│   Spring Boot   │◄──►│   Python Flask  │
│   Port 4200     │    │   Port 8089     │    │   Port 5000     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🚀 Démarrage Rapide

### Option 1: Script automatique (Recommandé)

```bash
# Windows
start-all.bat

# Linux/Mac
chmod +x start-all.sh
./start-all.sh
```

### Option 2: Démarrage manuel

1. **Microservice Python IA**
```bash
cd ai-reminder-service
python app.py
```

2. **Backend Spring Boot**
```bash
mvn spring-boot:run
```

3. **Frontend Angular**
```bash
cd ../NeuroCare_Front
npm start
```

## 📡 Endpoints API

### Microservice Python (Port 5000)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/health` | Vérification de santé |
| POST | `/generate-reminder` | Génération d'un rappel unique |
| POST | `/batch-reminders` | Génération de rappels en lot |
| GET | `/templates` | Récupération des templates |

### Backend Spring Boot (Port 8089)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/v1/ai-reminders/health` | Santé du service IA |
| POST | `/api/v1/appointments/{id}/generate-reminder` | Rappel pour un RDV |
| GET | `/api/v1/appointments/{id}/preview-reminder` | Prévisualisation |
| POST | `/api/v1/appointments/parent/{parentId}/generate-reminders` | Rappels parent |
| POST | `/api/v1/appointments/send-automatic-reminders` | Envoi automatique |

## 🎯 Types de Rappels

- **CONSULTATION** : Consultations médicales générales
- **THERAPEUTIC** : Séances thérapeutiques
- **MEDICAL** : Rendez-vous médicaux spécialisés
- **EDUCATIONAL** : Suivi éducatif

## ⏰ Niveaux d'Urgence

- **URGENT** (🚨) : Dans les 2 heures
- **TODAY** (📅) : Aujourd'hui
- **TOMORROW** (⏰) : Demain
- **UPCOMING** (📋) : Dans la semaine

## 🧪 Tests

### Test automatique
```bash
# Test complet
test-integration.bat

# Test détaillé du microservice
python ai-reminder-service/test_service.py
```

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

## 📊 Fonctionnalités

### ✅ Implémentées

- 🎯 Génération de rappels personnalisés
- ⏰ Calcul automatique de l'urgence
- 📦 Traitement par lot (jusqu'à 50 RDV)
- 🌟 Templates adaptés par type
- 🔄 Mode fallback robuste
- 📊 Logging complet
- 🌐 CORS activé
- 🔄 Tâches planifiées (cron)
- 👁️ Prévisualisation des rappels

### 🔄 Tâches Planifiées

- **Envoi automatique** : Tous les jours à 8h00
- **Cible** : Rendez-vous des prochaines 24-48h
- **Filtre** : Seulement les RDV non notifiés

## 📁 Structure des Fichiers

```
NeuroCare-Backend/
├── ai-reminder-service/          # Microservice Python
│   ├── app.py                   # Application Flask
│   ├── config.py                # Configuration
│   ├── requirements.txt         # Dépendances Python
│   ├── start.sh                 # Script démarrage Linux/Mac
│   ├── start.bat                # Script démarrage Windows
│   ├── test_service.py          # Tests automatisés
│   └── README.md                # Documentation
├── src/main/java/com/BrainStack/
│   ├── Services/
│   │   └── ReminderAIService.java    # Service IA
│   └── Controller/
│       └── AIReminderController.java # Contrôleur IA
├── start-all.bat                # Démarrage complet Windows
├── start-all.sh                 # Démarrage complet Linux/Mac
├── test-integration.bat         # Tests d'intégration
└── README_AI_REMINDERS.md       # Cette documentation
```

## 🔧 Configuration

### Variables d'environnement

```bash
# Microservice Python
PORT=5000
DEBUG=False
LOG_LEVEL=INFO

# Backend Spring Boot
ai.microservice.url=http://localhost:5000
ai.microservice.timeout=5000
```

### Configuration personnalisée

Modifiez `ai-reminder-service/config.py` pour :
- Templates de rappels
- Niveaux d'urgence
- Messages d'erreur
- Configuration CORS

## 🚨 Dépannage

### Problèmes courants

1. **Service Python non accessible**
   ```bash
   # Vérifier le port
   netstat -an | find "5000"
   
   # Redémarrer
   cd ai-reminder-service
   python app.py
   ```

2. **Service Spring Boot non accessible**
   ```bash
   # Vérifier le port
   netstat -an | find "8089"
   
   # Redémarrer
   mvn spring-boot:run
   ```

3. **Erreurs de compilation**
   ```bash
   # Nettoyer et recompiler
   mvn clean compile
   ```

### Logs

- **Microservice Python** : `ai-reminder-service/ai_reminder_service.log`
- **Backend Spring Boot** : Console + logs Spring Boot
- **Frontend Angular** : Console du navigateur

## 📈 Monitoring

### Métriques disponibles

- Nombre de rappels générés
- Taux d'erreur
- Temps de réponse
- Utilisation mémoire

### Health checks

```bash
# Microservice Python
curl http://localhost:5000/health

# Backend Spring Boot
curl http://localhost:8089/api/v1/ai-reminders/health
```

## 🔄 Intégration avec le Frontend

Le frontend Angular peut maintenant utiliser les nouveaux endpoints :

```typescript
// Service Angular
@Injectable()
export class AIReminderService {
  private apiUrl = 'http://localhost:8089/api/v1';
  
  generateReminder(appointmentId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/appointments/${appointmentId}/generate-reminder`, {});
  }
  
  previewReminder(appointmentId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/appointments/${appointmentId}/preview-reminder`);
  }
}
```

## 📝 Changelog

### v1.0.0 (2024-01-01)
- ✨ Microservice Python Flask créé
- 🎯 Génération de rappels personnalisés
- 📦 Support des lots jusqu'à 50 rendez-vous
- ⏰ Calcul automatique de l'urgence
- 🌟 Templates par type de rendez-vous
- 🔄 Mode fallback robuste
- 📊 Logging complet
- 🌐 CORS activé
- 🔄 Tâches planifiées Spring Boot
- 👁️ Prévisualisation des rappels
- 🧪 Tests automatisés
- 📚 Documentation complète

## 🤝 Support

Pour toute question ou problème :
1. Vérifiez les logs dans les fichiers de log
2. Testez avec les endpoints de health check
3. Consultez la documentation des endpoints
4. Exécutez les tests d'intégration

## 📄 Licence

Ce projet fait partie du système NeuroCare et est destiné à un usage médical et thérapeutique.
