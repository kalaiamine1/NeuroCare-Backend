package com.BrainStack.Controller;



import com.BrainStack.Dto.ApiResponse;
import com.BrainStack.Dto.AppointmentDTO;
import com.BrainStack.Dto.PagedResponse;

import com.BrainStack.Dto.CreateAppointmentRequest;
import com.BrainStack.Dto.UpdateAppointmentRequest;
import com.BrainStack.Services.AppointmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

/**
 * Controller pour la gestion des rendez-vous
 * Base URL: /api/v1/appointments
 */
@RestController
@RequestMapping("/appointments")
@CrossOrigin(origins = "*")
@Tag(name = "Appointments", description = "Appointment management APIs")
@Validated
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;
    private static final Logger log = LoggerFactory.getLogger(AppointmentController.class);

    /**
     * Crée un nouveau rendez-vous
     * POST /api/v1/appointments
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentDTO>> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request) {
        log.info("POST /appointments - Création d'un rendez-vous");

        try {
            AppointmentDTO appointment = appointmentService.createAppointment(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Rendez-vous créé avec succès", appointment));
        } catch (Exception e) {
            log.error("Erreur lors de la création du rendez-vous", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Erreur lors de la création",
                            List.of(e.getMessage())));
        }
    }

    /**
     * Récupère un rendez-vous par ID
     * GET /api/v1/appointments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentDTO>> getAppointmentById(
            @PathVariable Long id) {
        log.info("GET /appointments/{} - Récupération d'un rendez-vous", id);

        try {
            AppointmentDTO appointment = appointmentService.getAppointmentById(id);
            return ResponseEntity.ok(ApiResponse.success("Rendez-vous récupéré", appointment));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du rendez-vous", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Rendez-vous non trouvé",
                            List.of(e.getMessage())));
        }
    }

    /**
     * Récupère les rendez-vous du parent
     * GET /api/v1/appointments/parent/{parentId}
     */
    @GetMapping("/parent/{parentId}")
    public ResponseEntity<ApiResponse<PagedResponse<AppointmentDTO>>> getAppointmentsByParent(
            @PathVariable @NotNull @Min(1) Long parentId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.info("GET /appointments/parent/{} - Récupération des rendez-vous du parent (page={}, size={})", 
                parentId, page, size);

        // Validation des paramètres de pagination
        if (page < 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Le numéro de page doit être supérieur ou égal à 0", 
                            List.of("page: " + page)));
        }
        
        if (size < 1 || size > 100) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("La taille de page doit être entre 1 et 100", 
                            List.of("size: " + size)));
        }

        try {
            Page<AppointmentDTO> appointmentsPage = appointmentService.getAppointmentsByParent(parentId, page, size);
            
            if (appointmentsPage.isEmpty()) {
                log.info("Aucun rendez-vous trouvé pour le parent ID: {}", parentId);
                PagedResponse<AppointmentDTO> emptyPaged = PagedResponse.<AppointmentDTO>builder()
                    .content(List.of())
                    .page(page)
                    .size(size)
                    .totalElements(0)
                    .totalPages(0)
                    .last(true)
                    .build();
                return ResponseEntity.ok(ApiResponse.success("Aucun rendez-vous trouvé pour ce parent", emptyPaged));
            }
            
            PagedResponse<AppointmentDTO> paged = PagedResponse.<AppointmentDTO>builder()
                .content(appointmentsPage.getContent())
                .page(appointmentsPage.getNumber())
                .size(appointmentsPage.getSize())
                .totalElements(appointmentsPage.getTotalElements())
                .totalPages(appointmentsPage.getTotalPages())
                .last(appointmentsPage.isLast())
                .build();
                
            log.info("Récupération réussie: {} rendez-vous trouvés pour le parent ID: {}", 
                    appointmentsPage.getTotalElements(), parentId);
            return ResponseEntity.ok(ApiResponse.success("Rendez-vous du parent récupérés avec succès", paged));
            
        } catch (IllegalArgumentException e) {
            log.error("Erreur de validation lors de la récupération pour le parent ID: {}", parentId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Paramètres invalides", List.of(e.getMessage())));
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la récupération des rendez-vous du parent ID: {}", parentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur interne du serveur", 
                            List.of("Une erreur inattendue s'est produite")));
        }
    }

    /**
     * Récupère les rendez-vous du professionnel
     * GET /api/v1/appointments/professional/{professionalId}
     */
    @GetMapping("/professional/{professionalId}")
    public ResponseEntity<ApiResponse<PagedResponse<AppointmentDTO>>> getAppointmentsByProfessional(
            @PathVariable @NotNull @Min(1) Long professionalId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.info("GET /appointments/professional/{} - Récupération des rendez-vous du professionnel (page={}, size={})", 
                professionalId, page, size);

        // Validation des paramètres de pagination
        if (page < 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Le numéro de page doit être supérieur ou égal à 0", 
                            List.of("page: " + page)));
        }
        
        if (size < 1 || size > 100) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("La taille de page doit être entre 1 et 100", 
                            List.of("size: " + size)));
        }

        try {
            Page<AppointmentDTO> appointmentsPage = appointmentService.getAppointmentsByProfessional(professionalId, page, size);
            
            if (appointmentsPage.isEmpty()) {
                log.info("Aucun rendez-vous trouvé pour le professionnel ID: {}", professionalId);
                PagedResponse<AppointmentDTO> emptyPaged = PagedResponse.<AppointmentDTO>builder()
                    .content(List.of())
                    .page(page)
                    .size(size)
                    .totalElements(0)
                    .totalPages(0)
                    .last(true)
                    .build();
                return ResponseEntity.ok(ApiResponse.success("Aucun rendez-vous trouvé pour ce professionnel", emptyPaged));
            }
            
            PagedResponse<AppointmentDTO> paged = PagedResponse.<AppointmentDTO>builder()
                .content(appointmentsPage.getContent())
                .page(appointmentsPage.getNumber())
                .size(appointmentsPage.getSize())
                .totalElements(appointmentsPage.getTotalElements())
                .totalPages(appointmentsPage.getTotalPages())
                .last(appointmentsPage.isLast())
                .build();
                
            log.info("Récupération réussie: {} rendez-vous trouvés pour le professionnel ID: {}", 
                    appointmentsPage.getTotalElements(), professionalId);
            return ResponseEntity.ok(ApiResponse.success("Rendez-vous du professionnel récupérés avec succès", paged));
            
        } catch (IllegalArgumentException e) {
            log.error("Erreur de validation lors de la récupération pour le professionnel ID: {}", professionalId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Paramètres invalides", List.of(e.getMessage())));
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la récupération des rendez-vous du professionnel ID: {}", professionalId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur interne du serveur", 
                            List.of("Une erreur inattendue s'est produite")));
        }
    }

    /**
     * Met à jour un rendez-vous
     * PUT /api/v1/appointments/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentDTO>> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAppointmentRequest request) {
        log.info("PUT /appointments/{} - Mise à jour d'un rendez-vous", id);

        try {
            AppointmentDTO appointment = appointmentService.updateAppointment(id, request);
            return ResponseEntity.ok(ApiResponse.success("Rendez-vous mis à jour", appointment));
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Erreur lors de la mise à jour",
                            List.of(e.getMessage())));
        }
    }

    /**
     * Confirme un rendez-vous
     * PATCH /api/v1/appointments/{id}/confirm
     */
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<AppointmentDTO>> confirmAppointment(
            @PathVariable Long id) {
        log.info("PATCH /appointments/{}/confirm - Confirmation d'un rendez-vous", id);

        try {
            AppointmentDTO appointment = appointmentService.confirmAppointment(id);
            return ResponseEntity.ok(ApiResponse.success("Rendez-vous confirmé", appointment));
        } catch (Exception e) {
            log.error("Erreur lors de la confirmation", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Erreur lors de la confirmation",
                            List.of(e.getMessage())));
        }
    }

    /**
     * Annule un rendez-vous
     * PATCH /api/v1/appointments/{id}/cancel
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<AppointmentDTO>> cancelAppointment(
            @PathVariable Long id) {
        log.info("PATCH /appointments/{}/cancel - Annulation d'un rendez-vous", id);

        try {
            AppointmentDTO appointment = appointmentService.cancelAppointment(id);
            return ResponseEntity.ok(ApiResponse.success("Rendez-vous annulé", appointment));
        } catch (Exception e) {
            log.error("Erreur lors de l'annulation", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Erreur lors de l'annulation",
                            List.of(e.getMessage())));
        }
    }

    /**
     * Supprime un rendez-vous
     * DELETE /api/v1/appointments/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAppointment(
            @PathVariable Long id) {
        log.info("DELETE /appointments/{} - Suppression d'un rendez-vous", id);

        try {
            appointmentService.deleteAppointment(id);
            return ResponseEntity.ok(ApiResponse.success("Rendez-vous supprimé", null));
        } catch (Exception e) {
            log.error("Erreur lors de la suppression", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Erreur lors de la suppression",
                            List.of(e.getMessage())));
        }
    }

    /**
     * Récupère les rendez-vous à venir
     * GET /api/v1/appointments/upcoming
     */
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getUpcomingAppointments() {
        log.info("GET /appointments/upcoming - Récupération des rendez-vous à venir");

        try {
            List<AppointmentDTO> appointments = appointmentService.getUpcomingAppointments();
            return ResponseEntity.ok(ApiResponse.success("Rendez-vous à venir récupérés", appointments));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Erreur lors de la récupération",
                            List.of(e.getMessage())));
        }
    }

    /**
     * Détecte les conflits d'horaires
     * GET /api/v1/appointments/conflicts/detect
     */
    @GetMapping("/conflicts/detect")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> detectConflicts(
            @RequestParam Long professionalId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        log.info("GET /appointments/conflicts/detect - Détection des conflits");

        try {
            List<AppointmentDTO> conflicts = appointmentService.detectScheduleConflicts(
                    professionalId,
                    java.time.LocalDateTime.parse(startTime),
                    java.time.LocalDateTime.parse(endTime));
            return ResponseEntity.ok(ApiResponse.success("Conflits détectés", conflicts));
        } catch (Exception e) {
            log.error("Erreur lors de la détection", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Erreur lors de la détection",
                            List.of(e.getMessage())));
        }
    }

    /**
     * Détecte les conflits d'horaires pour un parent
     * GET /api/v1/appointments/conflicts/parent/detect
     */
    @GetMapping("/conflicts/parent/detect")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> detectParentConflicts(
            @RequestParam Long parentId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        log.info("GET /appointments/conflicts/parent/detect - Détection des conflits de parent");

        try {
            List<AppointmentDTO> conflicts = appointmentService.detectParentConflicts(
                    parentId,
                    java.time.LocalDateTime.parse(startTime),
                    java.time.LocalDateTime.parse(endTime));
            return ResponseEntity.ok(ApiResponse.success("Conflits de parent détectés", conflicts));
        } catch (Exception e) {
            log.error("Erreur lors de la détection des conflits de parent", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Erreur lors de la détection des conflits de parent",
                            List.of(e.getMessage())));
        }
    }

    // ========================================
    // 🤖 ENDPOINTS POUR LES RAPPELS IA
    // ========================================

    /**
     * 📨 Génère un rappel pour un rendez-vous
     * POST /api/v1/appointments/{id}/generate-reminder
     */
    @PostMapping("/{id}/generate-reminder")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateReminderForAppointment(
            @PathVariable Long id) {
        log.info("POST /appointments/{}/generate-reminder - Génération rappel", id);

        try {
            Map<String, Object> result = appointmentService.generateReminderForAppointment(id);

            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(ApiResponse.success("Rappel généré avec succès", result));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Erreur génération rappel",
                                List.of(result.get("error").toString())));
            }
        } catch (Exception e) {
            log.error("Erreur génération rappel pour RDV ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur interne", List.of(e.getMessage())));
        }
    }

    /**
     * 📨 Génère un rappel pour un rendez-vous (HEAD support)
     * HEAD /api/v1/appointments/{id}/generate-reminder
     */
    @RequestMapping(value = "/{id}/generate-reminder", method = RequestMethod.HEAD)
    public ResponseEntity<Void> headGenerateReminderForAppointment(@PathVariable Long id) {
        log.info("HEAD /appointments/{}/generate-reminder - Check rappel endpoint", id);
        return ResponseEntity.ok().build();
    }

    /**
     * 👁️ Prévisualise un rappel sans l'envoyer
     * GET /api/v1/appointments/{id}/preview-reminder
     */
    @GetMapping("/{id}/preview-reminder")
    public ResponseEntity<ApiResponse<Map<String, Object>>> previewReminder(
            @PathVariable Long id) {
        log.info("GET /appointments/{}/preview-reminder - Prévisualisation rappel", id);

        try {
            Map<String, Object> result = appointmentService.previewReminder(id);
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(ApiResponse.success("Rappel prévisualisé", result));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Erreur prévisualisation", 
                                List.of(result.get("error").toString())));
            }
        } catch (Exception e) {
            log.error("Erreur prévisualisation rappel pour RDV ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur interne", List.of(e.getMessage())));
        }
    }

    /**
     * 👨‍👩‍👧‍👦 Génère des rappels pour tous les rendez-vous d'un parent
     * POST /api/v1/appointments/parent/{parentId}/generate-reminders
     */
    @PostMapping("/parent/{parentId}/generate-reminders")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateRemindersForParent(
            @PathVariable Long parentId) {
        log.info("POST /appointments/parent/{}/generate-reminders - Génération rappels parent", parentId);

        try {
            Map<String, Object> result = appointmentService.generateRemindersForParent(parentId);

            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(ApiResponse.success("Rappels générés avec succès", result));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Erreur génération rappels",
                                List.of(result.get("error").toString())));
            }
        } catch (Exception e) {
            log.error("Erreur génération rappels pour parent ID: {}", parentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur interne", List.of(e.getMessage())));
        }
    }

    /**
     * 👨‍👩‍👧‍👦 Génère des rappels pour tous les rendez-vous d'un parent (HEAD support)
     * HEAD /api/v1/appointments/parent/{parentId}/generate-reminders
     */
    @RequestMapping(value = "/parent/{parentId}/generate-reminders", method = RequestMethod.HEAD)
    public ResponseEntity<Void> headGenerateRemindersForParent(@PathVariable Long parentId) {
        log.info("HEAD /appointments/parent/{}/generate-reminders - Check rappels parent endpoint", parentId);
        return ResponseEntity.ok().build();
    }

    /**
     * 🔄 Déclenche l'envoi automatique des rappels
     * POST /api/v1/appointments/send-automatic-reminders
     */
    @PostMapping("/send-automatic-reminders")
    public ResponseEntity<ApiResponse<String>> sendAutomaticReminders() {
        log.info("POST /appointments/send-automatic-reminders - Envoi automatique");

        try {
            appointmentService.sendAutomaticReminders();
            return ResponseEntity.ok(ApiResponse.success("Rappels automatiques envoyés", "Tâche exécutée"));
        } catch (Exception e) {
            log.error("Erreur envoi automatique", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur envoi automatique", List.of(e.getMessage())));
        }
    }
}