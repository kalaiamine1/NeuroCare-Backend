package com.BrainStack.Dto;

import com.BrainStack.Enums.AppointmentStatus;
import com.BrainStack.Enums.AppointmentType;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentFilterDTO {
    private Long parentId;
    private Long professionalId;
    private Long childId;
    private AppointmentType type;
    private AppointmentStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}