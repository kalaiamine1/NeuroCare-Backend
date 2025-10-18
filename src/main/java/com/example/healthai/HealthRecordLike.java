package com.example.healthai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthRecordLike {
    // Features numériques normalisées pour le modèle
    private double sleepHours;   // 4 - 10
    private double steps;        // 1000 - 12000
    private double heartRate;    // 60 - 130
    private double weight;       // 10 - 60
    private double moodScore;    // 1 - 5
    private double dietScore;    // 1 - 5

    public double[] toArray() {
        return new double[]{sleepHours, steps, heartRate, weight, moodScore, dietScore};
    }
}

