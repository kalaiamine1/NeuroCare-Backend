package com.BrainStack.Dto;

import com.BrainStack.Enums.AppointmentStatus;
import com.BrainStack.Enums.AppointmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Appointment data transfer object")
public class AppointmentDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AppointmentType type;
    private AppointmentStatus status;
    private Long parentId;
    private Long professionalId;
    private Long childId;
    private String location;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean notificationSent;
}
