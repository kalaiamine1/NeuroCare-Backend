package com.BrainStack.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    // Suivi santé
    private Double sleepHours; // durée du sommeil en heures
    private Integer steps; // nombre de pas / activité physique
    private String mood; // Ex : "Heureux", "Fatigué", "Stressé"
    private String dietQuality; // Ex : "Équilibré", "Trop sucré", etc.
    private Double weight;
    private Double heartRate;

    // Relation avec l’enfant
    @ManyToOne
    @JoinColumn(name = "child_id")
    private Child child;
}
