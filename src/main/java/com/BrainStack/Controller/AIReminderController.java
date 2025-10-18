package com.BrainStack.Controller;

import com.BrainStack.Dto.ApiResponse;
import com.BrainStack.Dto.AIReminderRequest;
import com.BrainStack.Dto.AIReminderResponse;
import com.BrainStack.Entity.Appointment;
import com.BrainStack.Services.AppointmentService;
import com.BrainStack.Services.ReminderAIService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 🤖 Contrôleur pour la génération de rappels IA
 * ===============================================
 * 
 * Ce contrôleur expose les endpoints pour générer des rappels
 * personnalisés en utilisant le microservice Python.
 * 
 * @author Agent IA Expert
 * @version 1.0.0
 */
@RestController
@RequestMapping("/ai-reminders")
@CrossOrigin(origins = "*")
@Tag(name = "AI Reminders", description = "Génération de rappels personnalisés avec IA")
public class AIReminderController {

    private static final Logger log = LoggerFactory.getLogger(AIReminderController.class);

    @Autowired
    private ReminderAIService reminderAIService;

    @Autowired
    private AppointmentService appointmentService;

    /**
     * 🏥 Vérification de la santé du service IA
     * GET /api/v1/ai-reminders/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkHealth() {
        log.info("🔍 Vérification santé service IA");

        try {
            boolean isAvailable = reminderAIService.isAIServiceAvailable();
            Map<String, Object> stats = reminderAIService.getServiceStats();
            
            if (isAvailable) {
                return ResponseEntity.ok(ApiResponse.success("Service IA opérationnel", stats));
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(ApiResponse.error("Service IA indisponible", List.of("Le microservice Python n'est pas accessible")));
            }
        } catch (Exception e) {
            log.error("❌ Erreur vérification santé", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur interne", List.of(e.getMessage())));
        }
    }

    /**
     * 📨 Génère un rappel pour un rendez-vous existant
     * POST /api/v1/ai-reminders/appointment/{id}
     */
    @PostMapping("/appointment/{id}")
    public ResponseEntity<ApiResponse<AIReminderResponse>> generateReminderForAppointment(
            @PathVariable Long id) {
        log.info("📨 Génération rappel pour RDV ID: {}", id);

        try {
            // Récupération du rendez-vous
            var appointmentDTO = appointmentService.getAppointmentById(id);
            
            // Conversion en entité (simulation - en réalité il faudrait un mapper)
            Appointment appointment = new Appointment();
            appointment.setId(appointmentDTO.getId());
            appointment.setTitle(appointmentDTO.getTitle());
            appointment.setStartTime(appointmentDTO.getStartTime());
            appointment.setType(appointmentDTO.getType());
            appointment.setLocation(appointmentDTO.getLocation());
            appointment.setNotes(appointmentDTO.getNotes());
            appointment.setProfessionalId(appointmentDTO.getProfessionalId());

            // Génération du rappel
            AIReminderResponse reminder = reminderAIService.generateReminderForAppointment(appointment);
            
            log.info("✅ Rappel généré avec succès pour RDV ID: {}", id);
            return ResponseEntity.ok(ApiResponse.success("Rappel généré avec succès", reminder));

        } catch (Exception e) {
            log.error("❌ Erreur génération rappel pour RDV ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Erreur génération rappel", List.of(e.getMessage())));
        }
    }

    /**
     * 📦 Génère des rappels en lot pour plusieurs rendez-vous
     * POST /api/v1/ai-reminders/batch
     */
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateBatchReminders(
            @RequestBody List<Long> appointmentIds) {
        log.info("📦 Génération batch pour {} rendez-vous", appointmentIds.size());

        try {
            // Validation
            if (appointmentIds == null || appointmentIds.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Aucun ID de rendez-vous fourni", List.of("La liste ne peut pas être vide")));
            }

            if (appointmentIds.size() > 50) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Trop de rendez-vous", List.of("Maximum 50 rendez-vous par lot")));
            }

            // Récupération des rendez-vous
            List<Appointment> appointments = new java.util.ArrayList<>();
            for (Long id : appointmentIds) {
                try {
                    var appointmentDTO = appointmentService.getAppointmentById(id);
                    Appointment appointment = new Appointment();
                    appointment.setId(appointmentDTO.getId());
                    appointment.setTitle(appointmentDTO.getTitle());
                    appointment.setStartTime(appointmentDTO.getStartTime());
                    appointment.setType(appointmentDTO.getType());
                    appointment.setLocation(appointmentDTO.getLocation());
                    appointment.setNotes(appointmentDTO.getNotes());
                    appointment.setProfessionalId(appointmentDTO.getProfessionalId());
                    appointments.add(appointment);
                } catch (Exception e) {
                    log.warn("⚠️ Rendez-vous ID {} non trouvé, ignoré", id);
                }
            }

            if (appointments.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Aucun rendez-vous valide", List.of("Tous les IDs fournis sont invalides")));
            }

            // Génération des rappels
            Map<String, Object> result = reminderAIService.generateBatchReminders(appointments);
            
            log.info("✅ Batch généré: {} rappels sur {} demandés", 
                    result.get("count"), appointmentIds.size());
            return ResponseEntity.ok(ApiResponse.success("Rappels générés avec succès", result));

        } catch (Exception e) {
            log.error("❌ Erreur génération batch", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur génération batch", List.of(e.getMessage())));
        }
    }

    /**
     * 👨‍👩‍👧‍👦 Génère des rappels pour tous les rendez-vous d'un parent
     * POST /api/v1/ai-reminders/parent/{parentId}
     */
    @PostMapping("/parent/{parentId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateRemindersForParent(
            @PathVariable Long parentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("👨‍👩‍👧‍👦 Génération rappels pour parent ID: {}", parentId);

        try {
            // Récupération des rendez-vous du parent
            var appointmentsPage = appointmentService.getAppointmentsByParent(parentId, page, size);
            
            if (appointmentsPage.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("Aucun rendez-vous trouvé pour ce parent", 
                        Map.of("count", 0, "reminders", List.of())));
            }

            // Conversion en entités
            List<Appointment> appointments = new java.util.ArrayList<>();
            for (var appointmentDTO : appointmentsPage.getContent()) {
                Appointment appointment = new Appointment();
                appointment.setId(appointmentDTO.getId());
                appointment.setTitle(appointmentDTO.getTitle());
                appointment.setStartTime(appointmentDTO.getStartTime());
                appointment.setType(appointmentDTO.getType());
                appointment.setLocation(appointmentDTO.getLocation());
                appointment.setNotes(appointmentDTO.getNotes());
                appointment.setProfessionalId(appointmentDTO.getProfessionalId());
                appointments.add(appointment);
            }

            // Génération des rappels
            Map<String, Object> result = reminderAIService.generateBatchReminders(appointments);
            
            log.info("✅ Rappels générés pour parent ID: {} - {} rappels", 
                    parentId, result.get("count"));
            return ResponseEntity.ok(ApiResponse.success("Rappels générés avec succès", result));

        } catch (Exception e) {
            log.error("❌ Erreur génération rappels parent ID: {}", parentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur génération rappels", List.of(e.getMessage())));
        }
    }

    /**
     * 📋 Récupère les templates disponibles
     * GET /api/v1/ai-reminders/templates
     */
    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTemplates() {
        log.info("📋 Récupération des templates disponibles");

        try {
            Map<String, Object> templates = reminderAIService.getAvailableTemplates();
            
            if (templates.isEmpty()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(ApiResponse.error("Templates non disponibles", 
                                List.of("Le microservice IA n'est pas accessible")));
            }

            return ResponseEntity.ok(ApiResponse.success("Templates récupérés", templates));

        } catch (Exception e) {
            log.error("❌ Erreur récupération templates", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur récupération templates", List.of(e.getMessage())));
        }
    }

    /**
     * 🔄 Déclenche l'envoi automatique des rappels
     * POST /api/v1/ai-reminders/send-automatic
     */
    @PostMapping("/send-automatic")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendAutomaticReminders() {
        log.info("🔄 Déclenchement envoi automatique des rappels");

        try {
            // Récupération des rendez-vous des prochaines 24-48h
            var upcomingAppointments = appointmentService.getUpcomingAppointments();
            
            if (upcomingAppointments.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.success("Aucun rendez-vous à venir", 
                        Map.of("count", 0, "message", "Aucun rappel à envoyer")));
            }

            // Conversion en entités
            List<Appointment> appointments = new java.util.ArrayList<>();
            for (var appointmentDTO : upcomingAppointments) {
                Appointment appointment = new Appointment();
                appointment.setId(appointmentDTO.getId());
                appointment.setTitle(appointmentDTO.getTitle());
                appointment.setStartTime(appointmentDTO.getStartTime());
                appointment.setType(appointmentDTO.getType());
                appointment.setLocation(appointmentDTO.getLocation());
                appointment.setNotes(appointmentDTO.getNotes());
                appointment.setProfessionalId(appointmentDTO.getProfessionalId());
                appointments.add(appointment);
            }

            // Génération des rappels
            Map<String, Object> result = reminderAIService.generateBatchReminders(appointments);
            
            log.info("✅ Rappels automatiques envoyés: {} rappels", result.get("count"));
            return ResponseEntity.ok(ApiResponse.success("Rappels automatiques envoyés", result));

        } catch (Exception e) {
            log.error("❌ Erreur envoi automatique", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur envoi automatique", List.of(e.getMessage())));
        }
    }
}
