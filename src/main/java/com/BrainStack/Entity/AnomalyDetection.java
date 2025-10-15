package com.BrainStack.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnomalyDetection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime detectedAt;
    private String type; // Ex : "Sommeil insuffisant", "Baisse d’activité", "Stress élevé"
    private String description;

    private Boolean resolved = false;

    // Relation avec l’enfant
    @ManyToOne
    @JoinColumn(name = "child_id")
    private Child child;
}
