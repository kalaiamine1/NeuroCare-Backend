package com.BrainStack.Dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

/**
 * DTO pour la création/modification d'un enregistrement de santé (HealthRecord).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthRecordRequestDTO {
    @NotNull(message = "La date est obligatoire.")
    private LocalDate date;

    @NotNull(message = "Le nombre d'heures de sommeil est obligatoire.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Les heures de sommeil doivent être >= 0.")
    @DecimalMax(value = "24.0", inclusive = true, message = "Les heures de sommeil doivent être <= 24.")
    private Double sleepHours;

    @NotNull(message = "Le nombre de pas est obligatoire.")
    @Min(value = 0, message = "Le nombre de pas doit être >= 0.")
    private Integer steps;

    @NotBlank(message = "L'humeur est obligatoire.")
    @Size(max = 100, message = "L'humeur ne doit pas dépasser 100 caractères.")
    private String mood;

    @NotBlank(message = "La qualité de l'alimentation est obligatoire.")
    @Size(max = 150, message = "La qualité de l'alimentation ne doit pas dépasser 150 caractères.")
    private String dietQuality;

    @Positive(message = "Le poids doit être > 0.")
    private Double weight;

    @Positive(message = "La fréquence cardiaque doit être > 0.")
    private Double heartRate;
}

