package com.BrainStack.Utils;

import com.BrainStack.Dto.AppointmentDTO;
import com.BrainStack.Entity.Appointment;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre Appointment et AppointmentDTO
 */
@Component
public class AppointmentMapper {

    /**
     * Convertit une entité Appointment en DTO
     */
    public AppointmentDTO toDTO(Appointment appointment) {
        if (appointment == null) {
            return null;
        }

        return AppointmentDTO.builder()
                .id(appointment.getId())
                .title(appointment.getTitle())
                .description(appointment.getDescription())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .type(appointment.getType())
                .status(appointment.getStatus())
                .parentId(appointment.getParentId())
                .professionalId(appointment.getProfessionalId())
                .childId(appointment.getChildId())
                .location(appointment.getLocation())
                .notes(appointment.getNotes())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .notificationSent(appointment.getNotificationSent())
                .build();
    }

    /**
     * Convertit un DTO en entité Appointment
     */
    public Appointment toEntity(AppointmentDTO dto) {
        if (dto == null) {
            return null;
        }

        return Appointment.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .type(dto.getType())
                .status(dto.getStatus())
                .parentId(dto.getParentId())
                .professionalId(dto.getProfessionalId())
                .childId(dto.getChildId())
                .location(dto.getLocation())
                .notes(dto.getNotes())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .notificationSent(dto.getNotificationSent())
                .build();
    }
}