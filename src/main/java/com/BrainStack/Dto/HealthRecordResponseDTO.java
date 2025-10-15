package com.BrainStack.Dto;

import lombok.*;

import java.time.LocalDate;

/**
 * DTO pour la réponse d'un enregistrement de santé (HealthRecord).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthRecordResponseDTO {
    private Long id;
    private LocalDate date;
    private Double sleepHours;
    private Integer steps;
    private String mood;
    private String dietQuality;
    private Double weight;
    private Double heartRate;

    // Informations de liaison
    private Integer childId;
}

