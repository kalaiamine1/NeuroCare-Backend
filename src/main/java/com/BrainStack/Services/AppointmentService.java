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
        log.info("Récupération des rendez-vous du parent ID: {}", parentId);

        Pageable pageable = PageRequest.of(page, size);
        Page<Appointment> appointments = appointmentRepository.findByParentId(parentId, pageable);

        return appointments.map(appointmentMapper::toDTO);
    }

    /**
     * Récupère tous les rendez-vous du professionnel avec pagination
     */
    public Page<AppointmentDTO> getAppointmentsByProfessional(Long professionalId, int page, int size) {
        log.info("Récupération des rendez-vous du professionnel ID: {}", professionalId);

        Pageable pageable = PageRequest.of(page, size);
        Page<Appointment> appointments = appointmentRepository.findByProfessionalId(professionalId, pageable);

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
}