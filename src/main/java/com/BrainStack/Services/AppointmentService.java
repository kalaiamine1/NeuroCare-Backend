package com.BrainStack.Services;

import com.BrainStack.Dto.AppointmentDTO;
import com.BrainStack.Dto.AppointmentFilterDTO;
import com.BrainStack.Dto.CreateAppointmentRequest;
import com.BrainStack.Dto.UpdateAppointmentRequest;
import com.BrainStack.Entity.Appointment;
import com.BrainStack.Enums.AppointmentStatus;
import com.BrainStack.Enums.AppointmentType;
import com.BrainStack.Exception.ConflictException;
import com.BrainStack.Exception.ResourceNotFoundException;
import com.BrainStack.Repository.AppointmentRepository;
import com.BrainStack.Utils.AppointmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AppointmentMapper appointmentMapper;

    /**
     * Crée un nouveau rendez-vous
     */
    public AppointmentDTO createAppointment(CreateAppointmentRequest request) {
        log.info("Création d'un rendez-vous : {}", request.getTitle());

        // Validation des champs requis
        validateCreateRequest(request);

        // Vérification des conflits d'horaires
        checkForConflicts(request.getProfessionalId(), request.getStartTime(), request.getEndTime());
        
        // Vérification des conflits de parent (même parent, horaires qui se chevauchent)
        checkForParentConflicts(request.getParentId(), request.getStartTime(), request.getEndTime());

        // Création de l'entité
        Appointment appointment = Appointment.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .type(AppointmentType.valueOf(request.getType().toUpperCase()))
                .status(AppointmentStatus.PENDING)
                .parentId(request.getParentId())
                .professionalId(request.getProfessionalId())
                .childId(request.getChildId())
                .location(request.getLocation())
                .notes(request.getNotes())
                .notificationSent(false)
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Rendez-vous créé avec succès. ID: {}", saved.getId());

        return appointmentMapper.toDTO(saved);
    }

    /**
     * Récupère un rendez-vous par son ID
     */
    public AppointmentDTO getAppointmentById(Long id) {
        log.info("Récupération du rendez-vous ID: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'ID: " + id));

        return appointmentMapper.toDTO(appointment);
    }

    /**
     * Récupère tous les rendez-vous du parent avec pagination
     */
    public Page<AppointmentDTO> getAppointmentsByParent(Long parentId, int page, int size) {
        log.info("Récupération des rendez-vous du parent ID: {} (page={}, size={})", parentId, page, size);

        // Validation des paramètres
        if (parentId == null || parentId <= 0) {
            throw new IllegalArgumentException("L'ID du parent doit être un nombre positif");
        }

        if (page < 0) {
            throw new IllegalArgumentException("Le numéro de page doit être supérieur ou égal à 0");
        }

        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("La taille de page doit être entre 1 et 100");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Appointment> appointments = appointmentRepository.findByParentId(parentId, pageable);

        log.info("Trouvé {} rendez-vous pour le parent ID: {} (page {}/{})", 
                appointments.getTotalElements(), parentId, page + 1, appointments.getTotalPages());

        return appointments.map(appointmentMapper::toDTO);
    }

    /**
     * Récupère tous les rendez-vous du professionnel avec pagination
     */
    public Page<AppointmentDTO> getAppointmentsByProfessional(Long professionalId, int page, int size) {
        log.info("Récupération des rendez-vous du professionnel ID: {} (page={}, size={})", professionalId, page, size);

        // Validation des paramètres
        if (professionalId == null || professionalId <= 0) {
            throw new IllegalArgumentException("L'ID du professionnel doit être un nombre positif");
        }

        if (page < 0) {
            throw new IllegalArgumentException("Le numéro de page doit être supérieur ou égal à 0");
        }

        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("La taille de page doit être entre 1 et 100");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Appointment> appointments = appointmentRepository.findByProfessionalId(professionalId, pageable);

        log.info("Trouvé {} rendez-vous pour le professionnel ID: {} (page {}/{})", 
                appointments.getTotalElements(), professionalId, page + 1, appointments.getTotalPages());

        return appointments.map(appointmentMapper::toDTO);
    }

    /**
     * Filtre les rendez-vous selon les critères
     */
    public Page<AppointmentDTO> filterAppointments(AppointmentFilterDTO filter, int page, int size) {
        log.info("Filtrage des rendez-vous avec les critères : {}", filter);

        Pageable pageable = PageRequest.of(page, size);

        if (filter.getParentId() != null) {
            return getAppointmentsByParent(filter.getParentId(), page, size);
        }

        if (filter.getProfessionalId() != null) {
            return getAppointmentsByProfessional(filter.getProfessionalId(), page, size);
        }

        if (filter.getType() != null) {
            Page<Appointment> appointments = appointmentRepository.findByType(filter.getType(), pageable);
            return appointments.map(appointmentMapper::toDTO);
        }

        if (filter.getStatus() != null) {
            Page<Appointment> appointments = appointmentRepository.findByStatus(filter.getStatus(), pageable);
            return appointments.map(appointmentMapper::toDTO);
        }

        return Page.empty();
    }

    /**
     * Met à jour un rendez-vous
     */
    public AppointmentDTO updateAppointment(Long id, UpdateAppointmentRequest request) {
        log.info("Mise à jour du rendez-vous ID: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'ID: " + id));

        if (request.getTitle() != null) {
            appointment.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            appointment.setDescription(request.getDescription());
        }

        if (request.getStartTime() != null && request.getEndTime() != null) {
            // Vérifier les conflits si les horaires ont changé
            if (!appointment.getStartTime().equals(request.getStartTime()) || 
                !appointment.getEndTime().equals(request.getEndTime())) {
                checkForConflicts(appointment.getProfessionalId(), request.getStartTime(), request.getEndTime());
                checkForParentConflicts(appointment.getParentId(), request.getStartTime(), request.getEndTime());
            }
            appointment.setStartTime(request.getStartTime());
            appointment.setEndTime(request.getEndTime());
        }

        if (request.getStatus() != null) {
            appointment.setStatus(AppointmentStatus.valueOf(request.getStatus().toUpperCase()));
        }

        if (request.getLocation() != null) {
            appointment.setLocation(request.getLocation());
        }

        if (request.getNotes() != null) {
            appointment.setNotes(request.getNotes());
        }

        Appointment updated = appointmentRepository.save(appointment);
        log.info("Rendez-vous mise à jour avec succès. ID: {}", updated.getId());

        return appointmentMapper.toDTO(updated);
    }

    /**
     * Annule un rendez-vous
     */
    public AppointmentDTO cancelAppointment(Long id) {
        log.info("Annulation du rendez-vous ID: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'ID: " + id));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            log.warn("Le rendez-vous est déjà annulé. ID: {}", id);
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment cancelled = appointmentRepository.save(appointment);

        return appointmentMapper.toDTO(cancelled);
    }

    /**
     * Confirme un rendez-vous
     */
    public AppointmentDTO confirmAppointment(Long id) {
        log.info("Confirmation du rendez-vous ID: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'ID: " + id));

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        Appointment confirmed = appointmentRepository.save(appointment);

        return appointmentMapper.toDTO(confirmed);
    }

    /**
     * Supprime un rendez-vous
     */
    public void deleteAppointment(Long id) {
        log.info("Suppression du rendez-vous ID: {}", id);

        if (!appointmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Rendez-vous non trouvé avec l'ID: " + id);
        }

        appointmentRepository.deleteById(id);
        log.info("Rendez-vous supprimé avec succès. ID: {}", id);
    }

    /**
     * Récupère les rendez-vous à venir
     */
    public List<AppointmentDTO> getUpcomingAppointments() {
        log.info("Récupération des rendez-vous à venir");

        List<Appointment> appointments = appointmentRepository.findUpcomingAppointments(LocalDateTime.now());

        return appointments.stream()
                .map(appointmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les rendez-vous passés
     */
    public List<AppointmentDTO> getPastAppointments() {
        log.info("Récupération des rendez-vous passés");

        List<Appointment> appointments = appointmentRepository.findPastAppointments(LocalDateTime.now());

        return appointments.stream()
                .map(appointmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Détecte les conflits d'horaires
     */
    public List<AppointmentDTO> detectScheduleConflicts(Long professionalId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Détection des conflits pour le professionnel ID: {}", professionalId);

        List<Appointment> conflicts = appointmentRepository.findConflicts(professionalId, startTime, endTime);

        return conflicts.stream()
                .map(appointmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Détecte les conflits d'horaires pour un parent
     */
    public List<AppointmentDTO> detectParentConflicts(Long parentId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Détection des conflits pour le parent ID: {}", parentId);

        List<Appointment> conflicts = appointmentRepository.findParentConflicts(parentId, startTime, endTime);

        return conflicts.stream()
                .map(appointmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Valide la demande de création
     */
    private void validateCreateRequest(CreateAppointmentRequest request) {
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new IllegalArgumentException("Les heures de début et fin sont requises");
        }

        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new IllegalArgumentException("L'heure de début doit être avant l'heure de fin");
        }

        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Impossible de planifier dans le passé");
        }

        if (request.getParentId() == null || request.getProfessionalId() == null) {
            throw new IllegalArgumentException("L'ID du parent et du professionnel sont requis");
        }
    }

    /**
     * Vérifie les conflits d'horaires
     */
    private void checkForConflicts(Long professionalId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Appointment> conflicts = appointmentRepository.findConflicts(professionalId, startTime, endTime);

        if (!conflicts.isEmpty()) {
            log.warn("Conflits détectés pour le professionnel ID: {}", professionalId);
            throw new ConflictException("Conflits d'horaires détectés. Le professionnel n'est pas disponible.");
        }
    }

    /**
     * Vérifie les conflits d'horaires pour un parent
     */
    private void checkForParentConflicts(Long parentId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Appointment> conflicts = appointmentRepository.findParentConflicts(parentId, startTime, endTime);

        if (!conflicts.isEmpty()) {
            log.warn("Conflits détectés pour le parent ID: {}", parentId);
            throw new ConflictException("Vous ne pouvez pas être à 2 endroits en même temps ! Conflit d'horaires détecté.");
        }
    }

    // ========================================
    // 🤖 MÉTHODES POUR LES RAPPELS IA
    // ========================================

    @Autowired
    private ReminderAIService reminderAIService;

    /**
     * 📨 Génère un rappel pour un rendez-vous spécifique
     * 
     * @param appointmentId ID du rendez-vous
     * @return Map avec le rappel généré
     */
    public Map<String, Object> generateReminderForAppointment(Long appointmentId) {
        log.info("📨 Génération rappel pour RDV ID: {}", appointmentId);

        try {
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'ID: " + appointmentId));

            // Génération du rappel via le service IA
            var reminderResponse = reminderAIService.generateReminderForAppointment(appointment);
            
            // Mise à jour du statut de notification
            appointment.setNotificationSent(true);
            appointment.setReminderSentAt(LocalDateTime.now());
            appointmentRepository.save(appointment);

            log.info("✅ Rappel généré et envoyé pour RDV ID: {}", appointmentId);
            
            return Map.of(
                "success", true,
                "appointment_id", appointmentId,
                "message", reminderResponse.getMessage(),
                "generated_at", reminderResponse.getGeneratedAt(),
                "source", "ai_service"
            );

        } catch (Exception e) {
            log.error("❌ Erreur génération rappel pour RDV ID: {}", appointmentId, e);
            return Map.of(
                "success", false,
                "appointment_id", appointmentId,
                "error", e.getMessage(),
                "source", "fallback"
            );
        }
    }

    /**
     * 👨‍👩‍👧‍👦 Génère des rappels pour tous les rendez-vous d'un parent
     * 
     * @param parentId ID du parent
     * @return Map avec les résultats
     */
    public Map<String, Object> generateRemindersForParent(Long parentId) {
        log.info("👨‍👩‍👧‍👦 Génération rappels pour parent ID: {}", parentId);

        try {
            // Récupération des rendez-vous du parent
            List<Appointment> appointments = appointmentRepository.findByParentId(parentId, 
                    org.springframework.data.domain.PageRequest.of(0, 100)).getContent();

            if (appointments.isEmpty()) {
                return Map.of(
                    "success", true,
                    "parent_id", parentId,
                    "count", 0,
                    "message", "Aucun rendez-vous trouvé pour ce parent"
                );
            }

            // Génération des rappels en lot
            var batchResult = reminderAIService.generateBatchReminders(appointments);
            
            // Mise à jour des statuts de notification
            for (Appointment appointment : appointments) {
                appointment.setNotificationSent(true);
                appointment.setReminderSentAt(LocalDateTime.now());
            }
            appointmentRepository.saveAll(appointments);

            log.info("✅ Rappels générés pour parent ID: {} - {} rappels", 
                    parentId, batchResult.get("count"));
            
            return batchResult;

        } catch (Exception e) {
            log.error("❌ Erreur génération rappels parent ID: {}", parentId, e);
            return Map.of(
                "success", false,
                "parent_id", parentId,
                "error", e.getMessage(),
                "source", "fallback"
            );
        }
    }

    /**
     * 🔄 Tâche planifiée pour l'envoi automatique des rappels
     * Exécutée tous les jours à 8h00
     */
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 8 * * *")
    public void sendAutomaticReminders() {
        log.info("🔄 Démarrage envoi automatique des rappels");

        try {
            // Récupération des rendez-vous des prochaines 24-48h
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime tomorrow = now.plusDays(1);
            LocalDateTime dayAfterTomorrow = now.plusDays(2);

            List<Appointment> upcomingAppointments = appointmentRepository
                    .findAppointmentsBetween(tomorrow, dayAfterTomorrow)
                    .stream()
                    .filter(appointment -> !appointment.getNotificationSent())
                    .collect(Collectors.toList());

            if (upcomingAppointments.isEmpty()) {
                log.info("ℹ️ Aucun rappel automatique à envoyer");
                return;
            }

            // Génération des rappels
            var batchResult = reminderAIService.generateBatchReminders(upcomingAppointments);
            
            // Mise à jour des statuts
            for (Appointment appointment : upcomingAppointments) {
                appointment.setNotificationSent(true);
                appointment.setReminderSentAt(LocalDateTime.now());
            }
            appointmentRepository.saveAll(upcomingAppointments);

            log.info("✅ Rappels automatiques envoyés: {} rappels", batchResult.get("count"));

        } catch (Exception e) {
            log.error("❌ Erreur envoi automatique des rappels", e);
        }
    }

    /**
     * 👁️ Prévisualise un rappel sans l'envoyer
     * 
     * @param appointmentId ID du rendez-vous
     * @return Map avec le rappel prévisualisé
     */
    public Map<String, Object> previewReminder(Long appointmentId) {
        log.info("👁️ Prévisualisation rappel pour RDV ID: {}", appointmentId);

        try {
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'ID: " + appointmentId));

            // Génération du rappel sans mise à jour
            var reminderResponse = reminderAIService.generateReminderForAppointment(appointment);
            
            return Map.of(
                "success", true,
                "appointment_id", appointmentId,
                "message", reminderResponse.getMessage(),
                "preview", true,
                "generated_at", reminderResponse.getGeneratedAt()
            );

        } catch (Exception e) {
            log.error("❌ Erreur prévisualisation rappel pour RDV ID: {}", appointmentId, e);
            return Map.of(
                "success", false,
                "appointment_id", appointmentId,
                "error", e.getMessage()
            );
        }
    }
}