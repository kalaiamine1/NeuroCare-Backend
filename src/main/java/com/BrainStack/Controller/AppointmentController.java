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

import java.util.List;

/**
 * Controller pour la gestion des rendez-vous
 * Base URL: /api/v1/appointments
 */
@RestController
@RequestMapping("/appointments")
@CrossOrigin(origins = "*")
@Tag(name = "Appointments", description = "Appointment management APIs")
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
            @PathVariable Long parentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /appointments/parent/{} - Récupération des rendez-vous du parent", parentId);

        try {
        Page<AppointmentDTO> appointmentsPage = appointmentService.getAppointmentsByParent(parentId, page, size);
        PagedResponse<AppointmentDTO> paged = PagedResponse.<AppointmentDTO>builder()
            .content(appointmentsPage.getContent())
            .page(appointmentsPage.getNumber())
            .size(appointmentsPage.getSize())
            .totalElements(appointmentsPage.getTotalElements())
            .totalPages(appointmentsPage.getTotalPages())
            .last(appointmentsPage.isLast())
            .build();
        return ResponseEntity.ok(ApiResponse.success("Rendez-vous du parent récupérés", paged));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Erreur lors de la récupération",
                            List.of(e.getMessage())));
        }
    }

    /**
     * Récupère les rendez-vous du professionnel
     * GET /api/v1/appointments/professional/{professionalId}
     */
    @GetMapping("/professional/{professionalId}")
    public ResponseEntity<ApiResponse<PagedResponse<AppointmentDTO>>> getAppointmentsByProfessional(
            @PathVariable Long professionalId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /appointments/professional/{} - Récupération des rendez-vous du professionnel", professionalId);

        try {
        Page<AppointmentDTO> appointmentsPage = appointmentService.getAppointmentsByProfessional(professionalId, page, size);
        PagedResponse<AppointmentDTO> paged = PagedResponse.<AppointmentDTO>builder()
            .content(appointmentsPage.getContent())
            .page(appointmentsPage.getNumber())
            .size(appointmentsPage.getSize())
            .totalElements(appointmentsPage.getTotalElements())
            .totalPages(appointmentsPage.getTotalPages())
            .last(appointmentsPage.isLast())
            .build();
        return ResponseEntity.ok(ApiResponse.success("Rendez-vous du professionnel récupérés", paged));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Erreur lors de la récupération",
                            List.of(e.getMessage())));
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
}