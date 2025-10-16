## NeuroCare - Appointments (RDV) + AI Integration Guide

### Overview
This module provides appointment management (CRUD, conflicts, lists, calendars) and AI-assisted features (time suggestions, type analysis, reminder generation).

### Architecture & URLs
- Backend (Spring Boot): `http://localhost:8089/api/v1`
- AI Microservice (Flask): `http://localhost:5000`
- Frontend (Angular): `http://localhost:4200`

### Prerequisites
- MySQL 8+ running locally with user `root` and an accessible schema (auto-created):
  - JDBC: `jdbc:mysql://localhost:3306/brainstack?createDatabaseIfNotExist=true`
- Java 17+, Maven/Gradle per your IDE
- Node 18+, npm 9+
- Python 3.11 for the AI microservice

### Configuration
- Backend: `src/main/resources/application.properties`
  - `server.port=8089`
  - `server.servlet.context-path=/api/v1`
  - `ai.microservice.url=http://localhost:5000`
  - MySQL config and JPA `ddl-auto=update`
- Frontend services use a single base URL via `src/app/shared/utils/api.config.ts`:
  - `backendBaseUrl: 'http://localhost:8089/api/v1'`

### Running the System (Local)
1) AI Microservice
   - Path: `NeuroCare_Front/ai-microservice`
   - Create venv with Python 3.11 and install deps:
     - Windows PowerShell:
       - `py -3.11 -m venv ai_env && .\ai_env\Scripts\Activate.ps1`
       - `python -m pip install --upgrade pip setuptools wheel`
       - `python -m pip install -r requirements.txt`
       - `python -m spacy download fr_core_news_sm`
       - `python -m spacy download en_core_web_sm`
       - Optional (heavy, requires C++ toolchain): `python -m pip install cmdstanpy==1.2.3 prophet==1.1.5`
     - Start: `python app.py`
     - Health: `curl http://localhost:5000/ai/health`

2) Backend (Spring Boot)
   - Ensure MySQL is up
   - Run from IDE or CLI (per your build tool)
   - Health checks:
     - `GET http://localhost:8089/api/v1/ai/health`
     - `GET http://localhost:8089/api/v1/appointments/upcoming`

3) Frontend (Angular)
   - Path: `NeuroCare_Front`
   - `npm install`
   - `npm start`
   - Open `http://localhost:4200`

### Database Seeding (Demo Data)
- Class: `com.BrainStack.Config.DataInitializer`
- On first run (empty DB), seeds:
  - Parents (Users): Leila, Ahmed
  - Children: Amine (Leila), Sarah & Youssef (Ahmed)
  - Appointments for both families, including a conflict scenario
- If appointments already exist, seeding is skipped

### Appointments API (Backend)
- Base: `/appointments`
- Create: `POST /` (body: `CreateAppointmentRequest`)
- Get by id: `GET /{id}`
- List by parent: `GET /parent/{parentId}?page=&size=`
- List by professional: `GET /professional/{professionalId}?page=&size=`
- Update: `PUT /{id}` (body: `UpdateAppointmentRequest`)
- Confirm: `PATCH /{id}/confirm`
- Cancel: `PATCH /{id}/cancel`
- Delete: `DELETE /{id}`
- Upcoming: `GET /upcoming`
- Conflicts: `GET /conflicts/detect?professionalId&startTime&endTime`
- Parent conflicts: `GET /conflicts/parent/detect?parentId&startTime&endTime`

DTOs: see `com.BrainStack.Dto.*` for `AppointmentDTO`, paged responses, and request DTOs.

### AI API (Backend proxy to Flask)
- Base: `/ai`
- Suggest slots: `POST /suggest` → Calls Flask `/ai/suggest`
  - Request: `{ professionalId: number, durationMinutes?: number, preferredDate?: string, timePreference?: 'morning'|'afternoon'|'evening' }`
  - Response: `{ suggestedSlots: string[], totalSlots: number, message: string, generatedAt: string, confidence: number, algorithm: string }`
- Analyze type: `POST /analyze` → Calls Flask `/ai/analyze`
  - Request: `{ description: string, context?: string }`
  - Response: `{ appointmentType: string, confidence: number, scores: { [k:string]:number }, entities: {text,label,description}[], suggestedDuration: number, generatedAt: string }`
- Reminder: `POST /reminder` → Calls Flask `/ai/reminder`
  - Request: `{ childName, professionalName, appointmentTime, location?, appointmentType?, notes? }`
  - Response: `{ message, generatedAt, language, tone, estimatedLength }`
- Health: `GET /health`

Flask endpoints (implemented in `ai-microservice/app.py`): `/ai/health`, `/ai/suggest`, `/ai/analyze`, `/ai/reminder`.
The service degrades gracefully without Prophet (returns default slots).

### End-to-End Scenarios
1) Parent views Mes Rendez-vous (Angular):
   - Lists upcoming and past appointments (via `/appointments` endpoints)
   - Detail view shows title, description, type, status, child, professional, location, notes

2) Create appointment with AI assistance:
   - Use AI suggestions to choose a time (`/ai/suggest`)
   - Use AI analysis to infer type and suggested duration (`/ai/analyze`)
   - Save via `POST /appointments`

3) Conflict detection:
   - Call `/appointments/conflicts/detect` before finalizing
   - UI highlights conflicts if any

4) Reminders:
   - Generate a personalized message using `/ai/reminder`
   - Optionally persist the message client-side or server-side as needed

### Troubleshooting
- CORS errors: ensure `CorsConfig` allows `http://localhost:4200`
- AI errors: verify AI is running at `ai.microservice.url` and reachable
- Seed not applied: table isn’t empty → clear DB or insert manually
- Prophet build issues on Windows: run without Prophet (fallback default slots), or install a C++ toolchain and `cmdstanpy`


