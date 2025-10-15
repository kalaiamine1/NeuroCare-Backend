package com.BrainStack.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Point de données agrégées pour l'affichage des statistiques (hebdo/mensuelles).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatsPointDTO {
    // Date de référence du point (jour pour hebdo, premier jour du mois pour mensuel) au format ISO-8601 yyyy-MM-dd
    private String date;

    private Double averageSleepHours;
    private Double averageWeight;
    private Double averageHeartRate;
    private Double averageSteps;
}

