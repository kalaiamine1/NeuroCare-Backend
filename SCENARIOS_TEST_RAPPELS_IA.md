# 🧪 Scénarios de Test - Système de Rappels IA NeuroCare

## 📋 Table des Matières

1. [Préparation des Tests](#préparation-des-tests)
2. [Tests Unitaires](#tests-unitaires)
3. [Tests d'Intégration](#tests-dintégration)
4. [Tests End-to-End](#tests-end-to-end)
5. [Tests de Performance](#tests-de-performance)
6. [Tests de Sécurité](#tests-de-sécurité)
7. [Tests de Récupération](#tests-de-récupération)
8. [Tests de Charge](#tests-de-charge)
9. [Tests de Compatibilité](#tests-de-compatibilité)
10. [Checklist de Validation](#checklist-de-validation)

---

## 🚀 Préparation des Tests

### 1. Environnement de Test

#### Prérequis
```bash
# Services requis
✅ Microservice Python Flask (port 5000)
✅ Backend Spring Boot (port 8089)
✅ Frontend Angular (port 4200)
✅ Base de données MySQL
✅ Données de test chargées
```

#### Données de Test
```sql
-- Rendez-vous de test avec différents scénarios
INSERT INTO appointments (id, title, child_id, professional_id, start_time, end_time, type, status, location, notes, created_at) VALUES
(1, 'Alice - Consultation', 1, 1, '2024-12-20 14:30:00', '2024-12-20 15:30:00', 'CONSULTATION', 'PENDING', 'Cabinet médical', 'Apporter carnet de santé', NOW()),
(2, 'Bob - Thérapie', 2, 2, '2024-12-19 16:00:00', '2024-12-19 17:00:00', 'THERAPEUTIC', 'CONFIRMED', 'Centre thérapeutique', 'Séance de rééducation', NOW()),
(3, 'Charlie - Urgent', 3, 1, NOW() + INTERVAL 1 HOUR, NOW() + INTERVAL 2 HOUR, 'MEDICAL', 'PENDING', 'Hôpital', 'Examen urgent', NOW()),
(4, 'David - Demain', 4, 3, NOW() + INTERVAL 24 HOUR, NOW() + INTERVAL 25 HOUR, 'EDUCATIONAL', 'PENDING', 'École spécialisée', 'Bilan éducatif', NOW()),
(5, 'Eve - Semaine prochaine', 5, 2, NOW() + INTERVAL 7 DAY, NOW() + INTERVAL 7 DAY + INTERVAL 1 HOUR, 'CONSULTATION', 'PENDING', 'Cabinet médical', 'Suivi régulier', NOW());
```

---

## 🔬 Tests Unitaires

### 1. Tests Microservice Python

#### Scénario 1.1 : Health Check
```python
def test_health_check():
    """Test de vérification de santé du service"""
    response = client.get('/health')
    assert response.status_code == 200
    data = response.json()
    assert data['status'] == 'healthy'
    assert 'version' in data
    assert 'uptime' in data
```

#### Scénario 1.2 : Génération Rappel Urgent
```python
def test_urgent_reminder():
    """Test génération rappel urgent (< 2h)"""
    request_data = {
        "childName": "Alice",
        "professionalName": "Dr. Martin",
        "appointmentTime": (datetime.now() + timedelta(hours=1)).isoformat(),
        "appointmentType": "CONSULTATION",
        "location": "Cabinet médical",
        "notes": "Urgent"
    }
    
    response = client.post('/generate-reminder', json=request_data)
    assert response.status_code == 200
    data = response.json()
    assert data['urgency'] == 'URGENT'
    assert '🚨' in data['message']
    assert 'URGENT' in data['message']
```

#### Scénario 1.3 : Génération Rappel Aujourd'hui
```python
def test_today_reminder():
    """Test génération rappel pour aujourd'hui"""
    request_data = {
        "childName": "Bob",
        "professionalName": "Dr. Smith",
        "appointmentTime": (datetime.now() + timedelta(hours=4)).isoformat(),
        "appointmentType": "THERAPEUTIC",
        "location": "Centre thérapeutique"
    }
    
    response = client.post('/generate-reminder', json=request_data)
    assert response.status_code == 200
    data = response.json()
    assert data['urgency'] == 'TODAY'
    assert '📅' in data['message']
    assert 'Aujourd\'hui' in data['message']
```

#### Scénario 1.4 : Génération Rappel Demain
```python
def test_tomorrow_reminder():
    """Test génération rappel pour demain"""
    request_data = {
        "childName": "Charlie",
        "professionalName": "Dr. Wilson",
        "appointmentTime": (datetime.now() + timedelta(days=1, hours=2)).isoformat(),
        "appointmentType": "MEDICAL",
        "location": "Hôpital"
    }
    
    response = client.post('/generate-reminder', json=request_data)
    assert response.status_code == 200
    data = response.json()
    assert data['urgency'] == 'TOMORROW'
    assert '⏰' in data['message']
    assert 'Demain' in data['message']
```

#### Scénario 1.5 : Génération Rappel À Venir
```python
def test_upcoming_reminder():
    """Test génération rappel à venir (> 48h)"""
    request_data = {
        "childName": "David",
        "professionalName": "Dr. Brown",
        "appointmentTime": (datetime.now() + timedelta(days=3)).isoformat(),
        "appointmentType": "EDUCATIONAL",
        "location": "École spécialisée"
    }
    
    response = client.post('/generate-reminder', json=request_data)
    assert response.status_code == 200
    data = response.json()
    assert data['urgency'] == 'UPCOMING'
    assert '📋' in data['message']
    assert 'Rappel' in data['message']
```

#### Scénario 1.6 : Génération en Lot
```python
def test_batch_reminders():
    """Test génération de rappels en lot"""
    request_data = {
        "appointments": [
            {
                "childName": "Alice",
                "professionalName": "Dr. Martin",
                "appointmentTime": (datetime.now() + timedelta(hours=2)).isoformat(),
                "appointmentType": "CONSULTATION",
                "location": "Cabinet médical"
            },
            {
                "childName": "Bob",
                "professionalName": "Dr. Smith",
                "appointmentTime": (datetime.now() + timedelta(days=1)).isoformat(),
                "appointmentType": "THERAPEUTIC",
                "location": "Centre thérapeutique"
            }
        ]
    }
    
    response = client.post('/batch-reminders', json=request_data)
    assert response.status_code == 200
    data = response.json()
    assert data['success'] == True
    assert data['count'] == 2
    assert len(data['reminders']) == 2
```

#### Scénario 1.7 : Gestion d'Erreurs
```python
def test_invalid_request():
    """Test gestion des requêtes invalides"""
    # Données manquantes
    response = client.post('/generate-reminder', json={})
    assert response.status_code == 400
    
    # Type de rendez-vous invalide
    invalid_data = {
        "childName": "Alice",
        "professionalName": "Dr. Martin",
        "appointmentTime": (datetime.now() + timedelta(hours=2)).isoformat(),
        "appointmentType": "INVALID_TYPE",
        "location": "Cabinet médical"
    }
    response = client.post('/generate-reminder', json=invalid_data)
    assert response.status_code == 400
```

### 2. Tests Backend Spring Boot

#### Scénario 2.1 : Service ReminderAIService
```java
@Test
public void testIsAIServiceAvailable() {
    // Test service disponible
    when(restTemplate.getForEntity(anyString(), eq(Map.class)))
        .thenReturn(ResponseEntity.ok(Map.of("status", "healthy")));
    
    boolean available = reminderAIService.isAIServiceAvailable();
    assertTrue(available);
}

@Test
public void testGenerateReminderForAppointment() {
    // Test génération rappel avec service IA disponible
    Appointment appointment = createTestAppointment();
    AIReminderResponse mockResponse = createMockResponse();
    
    when(restTemplate.postForEntity(anyString(), any(), eq(AIReminderResponse.class)))
        .thenReturn(ResponseEntity.ok(mockResponse));
    
    AIReminderResponse response = reminderAIService.generateReminderForAppointment(appointment);
    assertNotNull(response);
    assertEquals("UPCOMING", response.getUrgency());
}

@Test
public void testFallbackWhenAIServiceUnavailable() {
    // Test fallback quand service IA indisponible
    Appointment appointment = createTestAppointment();
    
    when(restTemplate.getForEntity(anyString(), eq(Map.class)))
        .thenThrow(new ResourceAccessException("Service unavailable"));
    
    AIReminderResponse response = reminderAIService.generateReminderForAppointment(appointment);
    assertNotNull(response);
    assertTrue(response.getMessage().contains("Fallback"));
}
```

#### Scénario 2.2 : Controller AIReminderController
```java
@Test
public void testHealthEndpoint() {
    // Test endpoint health
    when(reminderAIService.isAIServiceAvailable()).thenReturn(true);
    
    ResponseEntity<ApiResponse<Map<String, Object>>> response = 
        airReminderController.getHealth();
    
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().isSuccess());
}

@Test
public void testGenerateReminderEndpoint() {
    // Test endpoint génération rappel
    AIReminderRequest request = createTestRequest();
    AIReminderResponse mockResponse = createMockResponse();
    
    when(reminderAIService.generateReminderForAppointment(any()))
        .thenReturn(mockResponse);
    
    ResponseEntity<ApiResponse<Map<String, Object>>> response = 
        airReminderController.generateReminder(request);
    
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().isSuccess());
}
```

#### Scénario 2.3 : Tâche Planifiée
```java
@Test
public void testScheduledReminders() {
    // Test tâche planifiée
    List<Appointment> upcomingAppointments = createTestAppointments();
    when(appointmentRepository.findAppointmentsBetween(any(), any()))
        .thenReturn(upcomingAppointments);
    
    appointmentService.sendAutomaticReminders();
    
    verify(appointmentRepository, times(1)).saveAll(any());
    verify(reminderAIService, times(1)).generateBatchReminders(any());
}
```

### 3. Tests Frontend Angular

#### Scénario 3.1 : Service AIReminderService
```typescript
describe('AIReminderService', () => {
  let service: AIReminderService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AIReminderService]
    });
    service = TestBed.inject(AIReminderService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should check health successfully', () => {
    const mockHealthResponse = {
      status: 'healthy',
      service: 'AI Reminder Service',
      version: '1.0.0'
    };

    service.checkHealth().subscribe(response => {
      expect(response.status).toBe('healthy');
    });

    const req = httpMock.expectOne(`${service['apiUrl']}/ai-reminders/health`);
    expect(req.request.method).toBe('GET');
    req.flush(mockHealthResponse);
  });

  it('should generate reminder successfully', () => {
    const appointmentId = 1;
    const mockResponse = {
      success: true,
      data: {
        message: 'Test reminder',
        urgency: 'UPCOMING'
      }
    };

    service.generateReminder(appointmentId).subscribe(response => {
      expect(response.message).toBe('Test reminder');
      expect(response.urgency).toBe('UPCOMING');
    });

    const req = httpMock.expectOne(`${service['apiUrl']}/appointments/${appointmentId}/generate-reminder`);
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });
});
```

#### Scénario 3.2 : Composant AppointmentsList
```typescript
describe('AppointmentsListComponent', () => {
  let component: AppointmentsListComponent;
  let fixture: ComponentFixture<AppointmentsListComponent>;
  let aiReminderService: jasmine.SpyObj<AIReminderService>;

  beforeEach(() => {
    const spy = jasmine.createSpyObj('AIReminderService', ['generateReminder', 'previewReminder']);

    TestBed.configureTestingModule({
      declarations: [AppointmentsListComponent],
      providers: [
        { provide: AIReminderService, useValue: spy }
      ]
    });

    fixture = TestBed.createComponent(AppointmentsListComponent);
    component = fixture.componentInstance;
    aiReminderService = TestBed.inject(AIReminderService) as jasmine.SpyObj<AIReminderService>;
  });

  it('should generate reminder when button clicked', () => {
    const mockResponse = {
      message: 'Test reminder',
      urgency: 'UPCOMING'
    };
    aiReminderService.generateReminder.and.returnValue(of(mockResponse));

    component.generateReminder(1);

    expect(aiReminderService.generateReminder).toHaveBeenCalledWith(1);
    expect(component.generatedReminder).toEqual(mockResponse);
    expect(component.showReminderModal).toBeTrue();
  });
});
```

---

## 🔗 Tests d'Intégration

### 1. Scénario Intégration Complète

#### Scénario 4.1 : Flux Complet de Génération
```bash
# 1. Démarrer tous les services
./start-all.bat

# 2. Vérifier la santé des services
curl http://localhost:5000/health
curl http://localhost:8089/api/v1/ai-reminders/health

# 3. Tester génération rappel via API
curl -X POST http://localhost:8089/api/v1/appointments/1/generate-reminder \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 4. Vérifier dans la base de données
SELECT id, notification_sent, reminder_sent_at FROM appointments WHERE id = 1;
```

#### Scénario 4.2 : Test Interface Utilisateur
```bash
# 1. Ouvrir le navigateur
http://localhost:4200

# 2. Se connecter en tant que parent
# 3. Naviguer vers /parent/rdv/liste
# 4. Cliquer sur "Générer Rappels" (bouton global)
# 5. Vérifier que les rappels sont générés
# 6. Cliquer sur l'icône 👁️ d'un rendez-vous
# 7. Vérifier l'affichage du modal de rappel
# 8. Cliquer sur "Copier" et vérifier dans le presse-papiers
```

#### Scénario 4.3 : Test Vue Calendrier
```bash
# 1. Naviguer vers /parent/rdv/calendrier
# 2. Cliquer sur un rendez-vous
# 3. Dans le modal, cliquer sur "Prévisualiser Rappel"
# 4. Vérifier l'affichage du rappel
# 5. Cliquer sur "Générer Rappel"
# 6. Vérifier la mise à jour du statut
```

### 2. Scénarios de Données

#### Scénario 4.4 : Rendez-vous Urgent
```sql
-- Créer un rendez-vous dans 1 heure
INSERT INTO appointments (title, child_id, professional_id, start_time, end_time, type, status, location, notes) 
VALUES ('Test Urgent', 1, 1, NOW() + INTERVAL 1 HOUR, NOW() + INTERVAL 2 HOUR, 'CONSULTATION', 'PENDING', 'Cabinet', 'Test urgent');
```

**Test :**
1. Générer le rappel
2. Vérifier que l'urgence est "URGENT"
3. Vérifier que le message contient "🚨"
4. Vérifier la couleur rouge

#### Scénario 4.5 : Rendez-vous Aujourd'hui
```sql
-- Créer un rendez-vous dans 4 heures
INSERT INTO appointments (title, child_id, professional_id, start_time, end_time, type, status, location, notes) 
VALUES ('Test Aujourd\'hui', 1, 1, NOW() + INTERVAL 4 HOUR, NOW() + INTERVAL 5 HOUR, 'THERAPEUTIC', 'PENDING', 'Centre', 'Test aujourd\'hui');
```

**Test :**
1. Générer le rappel
2. Vérifier que l'urgence est "TODAY"
3. Vérifier que le message contient "📅"
4. Vérifier la couleur orange

#### Scénario 4.6 : Rendez-vous Demain
```sql
-- Créer un rendez-vous demain
INSERT INTO appointments (title, child_id, professional_id, start_time, end_time, type, status, location, notes) 
VALUES ('Test Demain', 1, 1, NOW() + INTERVAL 1 DAY, NOW() + INTERVAL 1 DAY + INTERVAL 1 HOUR, 'MEDICAL', 'PENDING', 'Hôpital', 'Test demain');
```

**Test :**
1. Générer le rappel
2. Vérifier que l'urgence est "TOMORROW"
3. Vérifier que le message contient "⏰"
4. Vérifier la couleur jaune

#### Scénario 4.7 : Rendez-vous À Venir
```sql
-- Créer un rendez-vous dans 3 jours
INSERT INTO appointments (title, child_id, professional_id, start_time, end_time, type, status, location, notes) 
VALUES ('Test À Venir', 1, 1, NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 3 DAY + INTERVAL 1 HOUR, 'EDUCATIONAL', 'PENDING', 'École', 'Test à venir');
```

**Test :**
1. Générer le rappel
2. Vérifier que l'urgence est "UPCOMING"
3. Vérifier que le message contient "📋"
4. Vérifier la couleur bleue

---

## 🎯 Tests End-to-End

### 1. Scénarios Utilisateur

#### Scénario 5.1 : Parent Génère Rappel Individuel
```gherkin
Feature: Génération de rappel individuel
  Scenario: Parent génère un rappel pour un rendez-vous
    Given Je suis connecté en tant que parent
    And J'ai des rendez-vous dans ma liste
    When Je clique sur l'icône 🔔 d'un rendez-vous
    Then Un modal s'affiche avec le rappel généré
    And Le rappel contient les informations du rendez-vous
    And Le niveau d'urgence est correct
    When Je clique sur "Copier"
    Then Le rappel est copié dans le presse-papiers
    And Le statut du rendez-vous est mis à jour
```

#### Scénario 5.2 : Parent Génère Tous les Rappels
```gherkin
Feature: Génération de rappels en lot
  Scenario: Parent génère tous les rappels
    Given Je suis connecté en tant que parent
    And J'ai plusieurs rendez-vous dans ma liste
    When Je clique sur "Générer Rappels"
    Then Un message de confirmation s'affiche
    And Tous les rappels sont générés
    And Les statuts des rendez-vous sont mis à jour
    And Je peux voir le nombre de rappels générés
```

#### Scénario 5.3 : Parent Prévisualise un Rappel
```gherkin
Feature: Prévisualisation de rappel
  Scenario: Parent prévisualise un rappel
    Given Je suis connecté en tant que parent
    And J'ai un rendez-vous dans ma liste
    When Je clique sur l'icône 👁️ du rendez-vous
    Then Un modal s'affiche avec le rappel
    And Le statut du rendez-vous n'est pas modifié
    And Je peux copier le rappel
```

### 2. Scénarios d'Erreur

#### Scénario 5.4 : Service IA Indisponible
```gherkin
Feature: Gestion des erreurs
  Scenario: Service IA indisponible
    Given Le microservice Python est arrêté
    And Je suis connecté en tant que parent
    When Je clique sur "Générer Rappels"
    Then Un message d'erreur s'affiche
    And Le système utilise le fallback
    And Un rappel basique est généré
```

#### Scénario 5.5 : Rendez-vous Inexistant
```gherkin
Feature: Gestion des erreurs
  Scenario: Rendez-vous inexistant
    Given Je suis connecté en tant que parent
    When Je tente de générer un rappel pour un ID inexistant
    Then Un message d'erreur s'affiche
    And Aucun rappel n'est généré
```

---

## ⚡ Tests de Performance

### 1. Scénarios de Charge

#### Scénario 6.1 : Génération de Rappels en Lot
```bash
# Test avec 100 rappels
for i in {1..100}; do
  curl -X POST http://localhost:8089/api/v1/appointments/parent/1/generate-reminders \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer YOUR_JWT_TOKEN" &
done
wait

# Vérifier les performances
# - Temps de réponse < 30 secondes
# - Tous les rappels générés
# - Aucune erreur
```

#### Scénario 6.2 : Stress Test API
```bash
# Test de charge avec Apache Bench
ab -n 1000 -c 10 http://localhost:5000/health
ab -n 500 -c 5 -p reminder.json -T application/json http://localhost:5000/generate-reminder
```

#### Scénario 6.3 : Test de Mémoire
```bash
# Monitoring de la mémoire pendant les tests
# Python
ps aux | grep python
# Spring Boot
jstat -gc $(pgrep java) 1s
# Angular
ps aux | grep ng
```

### 2. Scénarios de Temps de Réponse

#### Scénario 6.4 : Temps de Réponse Acceptable
```bash
# Test temps de réponse
time curl -X POST http://localhost:8089/api/v1/appointments/1/generate-reminder \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Critères :
# - Health check : < 100ms
# - Génération rappel : < 2s
# - Génération lot (10 rappels) : < 10s
```

---

## 🔒 Tests de Sécurité

### 1. Scénarios d'Authentification

#### Scénario 7.1 : Accès Non Autorisé
```bash
# Test sans token
curl -X POST http://localhost:8089/api/v1/appointments/1/generate-reminder \
  -H "Content-Type: application/json"

# Résultat attendu : 401 Unauthorized
```

#### Scénario 7.2 : Token Expiré
```bash
# Test avec token expiré
curl -X POST http://localhost:8089/api/v1/appointments/1/generate-reminder \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer EXPIRED_TOKEN"

# Résultat attendu : 401 Unauthorized
```

#### Scénario 7.3 : Rôle Incorrect
```bash
# Test avec rôle non autorisé
curl -X POST http://localhost:8089/api/v1/appointments/1/generate-reminder \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer DOCTOR_TOKEN"

# Résultat attendu : 403 Forbidden
```

### 2. Scénarios de Validation

#### Scénario 7.4 : Injection SQL
```bash
# Test injection SQL dans les paramètres
curl -X POST http://localhost:8089/api/v1/appointments/1/generate-reminder \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"childName": "Alice\"; DROP TABLE appointments; --"}'

# Résultat attendu : Validation échoue, pas d'injection
```

#### Scénario 7.5 : XSS
```bash
# Test XSS dans les paramètres
curl -X POST http://localhost:5000/generate-reminder \
  -H "Content-Type: application/json" \
  -d '{"childName": "<script>alert(\"XSS\")</script>", "professionalName": "Dr. Test"}'

# Résultat attendu : Script échappé dans la réponse
```

---

## 🔄 Tests de Récupération

### 1. Scénarios de Panne

#### Scénario 8.1 : Panne Microservice Python
```bash
# 1. Démarrer le système complet
./start-all.bat

# 2. Arrêter le microservice Python
pkill -f "python app.py"

# 3. Tester génération de rappel
curl -X POST http://localhost:8089/api/v1/appointments/1/generate-reminder \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 4. Vérifier que le fallback fonctionne
# Résultat attendu : Rappel généré avec source "fallback"
```

#### Scénario 8.2 : Panne Base de Données
```bash
# 1. Arrêter MySQL
sudo systemctl stop mysql

# 2. Tester génération de rappel
curl -X POST http://localhost:8089/api/v1/appointments/1/generate-reminder \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 3. Vérifier gestion d'erreur
# Résultat attendu : Erreur 500 avec message approprié
```

#### Scénario 8.3 : Redémarrage de Service
```bash
# 1. Arrêter le microservice Python
pkill -f "python app.py"

# 2. Redémarrer le microservice Python
cd ai-reminder-service && python app.py &

# 3. Tester génération de rappel
curl -X POST http://localhost:8089/api/v1/appointments/1/generate-reminder \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 4. Vérifier que le service fonctionne
# Résultat attendu : Rappel généré normalement
```

---

## 📱 Tests de Compatibilité

### 1. Scénarios Navigateurs

#### Scénario 9.1 : Chrome
```bash
# Test sur Chrome
# 1. Ouvrir http://localhost:4200
# 2. Se connecter
# 3. Tester toutes les fonctionnalités
# 4. Vérifier les performances
```

#### Scénario 9.2 : Firefox
```bash
# Test sur Firefox
# 1. Ouvrir http://localhost:4200
# 2. Se connecter
# 3. Tester toutes les fonctionnalités
# 4. Vérifier la compatibilité
```

#### Scénario 9.3 : Safari
```bash
# Test sur Safari
# 1. Ouvrir http://localhost:4200
# 2. Se connecter
# 3. Tester toutes les fonctionnalités
# 4. Vérifier la compatibilité
```

### 2. Scénarios Appareils

#### Scénario 9.4 : Mobile
```bash
# Test sur mobile
# 1. Ouvrir http://localhost:4200 sur mobile
# 2. Tester l'interface responsive
# 3. Tester les boutons tactiles
# 4. Vérifier la lisibilité
```

#### Scénario 9.5 : Tablette
```bash
# Test sur tablette
# 1. Ouvrir http://localhost:4200 sur tablette
# 2. Tester l'interface adaptée
# 3. Tester les interactions
# 4. Vérifier la navigation
```

---

## ✅ Checklist de Validation

### 1. Tests Fonctionnels

#### Microservice Python
- [ ] Health check fonctionne
- [ ] Génération rappel urgent (< 2h)
- [ ] Génération rappel aujourd'hui (2-24h)
- [ ] Génération rappel demain (24-48h)
- [ ] Génération rappel à venir (> 48h)
- [ ] Génération en lot
- [ ] Gestion des erreurs
- [ ] Validation des données
- [ ] Templates personnalisés

#### Backend Spring Boot
- [ ] Service ReminderAIService fonctionne
- [ ] Controller AIReminderController fonctionne
- [ ] Intégration avec AppointmentService
- [ ] Tâches planifiées
- [ ] Gestion des erreurs
- [ ] Fallback automatique
- [ ] Mise à jour des statuts
- [ ] Logging approprié

#### Frontend Angular
- [ ] Service AIReminderService fonctionne
- [ ] Composant AppointmentsList intégré
- [ ] Composant CalendarView intégré
- [ ] Composant ReminderTest fonctionne
- [ ] Modals d'affichage
- [ ] Gestion des états
- [ ] Copie presse-papiers
- [ ] Interface responsive

### 2. Tests d'Intégration

- [ ] Communication Python ↔ Spring Boot
- [ ] Communication Spring Boot ↔ Angular
- [ ] Base de données ↔ Spring Boot
- [ ] Flux complet de génération
- [ ] Mise à jour des statuts
- [ ] Gestion des erreurs end-to-end

### 3. Tests de Performance

- [ ] Temps de réponse < 2s (rappels individuels)
- [ ] Temps de réponse < 10s (lots de 10 rappels)
- [ ] Gestion de 100+ rappels simultanés
- [ ] Utilisation mémoire acceptable
- [ ] Pas de fuites mémoire

### 4. Tests de Sécurité

- [ ] Authentification requise
- [ ] Autorisation par rôles
- [ ] Validation des entrées
- [ ] Protection contre injection
- [ ] Protection contre XSS
- [ ] Chiffrement des communications

### 5. Tests de Récupération

- [ ] Fallback en cas de panne Python
- [ ] Gestion des erreurs de base de données
- [ ] Redémarrage automatique
- [ ] Récupération des données
- [ ] Logs d'erreur appropriés

### 6. Tests de Compatibilité

- [ ] Chrome (dernière version)
- [ ] Firefox (dernière version)
- [ ] Safari (dernière version)
- [ ] Edge (dernière version)
- [ ] Mobile (iOS/Android)
- [ ] Tablette (iOS/Android)

---

## 🚀 Scripts de Test Automatisés

### 1. Script de Test Complet

```bash
#!/bin/bash
# test-complete-reminders.sh

echo "🧪 Démarrage des tests complets du système de rappels IA"

# 1. Tests unitaires Python
echo "🐍 Tests unitaires Python..."
cd ai-reminder-service
python test_service.py
if [ $? -ne 0 ]; then
    echo "❌ Tests Python échoués"
    exit 1
fi
echo "✅ Tests Python réussis"

# 2. Tests unitaires Spring Boot
echo "☕ Tests unitaires Spring Boot..."
cd ../NeuroCare-Backend
./mvnw test
if [ $? -ne 0 ]; then
    echo "❌ Tests Spring Boot échoués"
    exit 1
fi
echo "✅ Tests Spring Boot réussis"

# 3. Tests unitaires Angular
echo "🅰️ Tests unitaires Angular..."
cd ../NeuroCare_Front
npm test -- --watch=false
if [ $? -ne 0 ]; then
    echo "❌ Tests Angular échoués"
    exit 1
fi
echo "✅ Tests Angular réussis"

# 4. Tests d'intégration
echo "🔗 Tests d'intégration..."
cd ../NeuroCare-Backend
./test-integration.sh
if [ $? -ne 0 ]; then
    echo "❌ Tests d'intégration échoués"
    exit 1
fi
echo "✅ Tests d'intégration réussis"

echo "🎉 Tous les tests sont passés avec succès !"
```

### 2. Script de Test de Performance

```bash
#!/bin/bash
# test-performance-reminders.sh

echo "⚡ Tests de performance du système de rappels IA"

# 1. Test de charge API Python
echo "🐍 Test de charge API Python..."
ab -n 1000 -c 10 http://localhost:5000/health

# 2. Test de charge API Spring Boot
echo "☕ Test de charge API Spring Boot..."
ab -n 500 -c 5 http://localhost:8089/api/v1/ai-reminders/health

# 3. Test de génération de rappels
echo "📨 Test de génération de rappels..."
for i in {1..50}; do
    curl -X POST http://localhost:8089/api/v1/appointments/1/generate-reminder \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer YOUR_JWT_TOKEN" &
done
wait

echo "✅ Tests de performance terminés"
```

---

## 📊 Rapport de Tests

### Template de Rapport

```markdown
# 📊 Rapport de Tests - Système de Rappels IA

## Résumé Exécutif
- **Date** : [DATE]
- **Version** : 1.0.0
- **Tests Exécutés** : [NOMBRE]
- **Succès** : [NOMBRE]
- **Échecs** : [NOMBRE]
- **Taux de Succès** : [POURCENTAGE]%

## Détails par Composant

### Microservice Python
- ✅ Health check : PASS
- ✅ Génération rappels : PASS
- ✅ Gestion erreurs : PASS
- ❌ Performance : FAIL (temps > 2s)

### Backend Spring Boot
- ✅ Services : PASS
- ✅ Controllers : PASS
- ✅ Intégration : PASS
- ✅ Fallback : PASS

### Frontend Angular
- ✅ Services : PASS
- ✅ Composants : PASS
- ✅ Interface : PASS
- ✅ Responsive : PASS

## Recommandations
1. Optimiser les performances du microservice Python
2. Ajouter plus de tests de charge
3. Améliorer la gestion des erreurs

## Conclusion
Le système fonctionne correctement avec quelques optimisations nécessaires.
```

---

**🧪 Système de Rappels IA NeuroCare - Scénarios de Test v1.0.0**

*Guide complet pour tester toutes les fonctionnalités du système de rappels IA.*

*Dernière mise à jour : Décembre 2024*
