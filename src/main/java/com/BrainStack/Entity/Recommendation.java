package com.BrainStack.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recommendation {
    @Id @GeneratedValue
    private Long id;
    private String type; // Article, vidéo, exercice, chatbot
    private String contenuUrl;
    private String theme; // TDAH, Autisme, etc.
    private String title; // Titre de la recommandation
    private String description; // Description détaillée
    private String category; // Catégorie FAQ (horaires, inscription, etc.)
    private String difficulty; // Niveau de difficulté (facile, moyen, difficile)
    private String language; // Langue de la ressource
    private Boolean isActive; // Si la recommandation est active
    private LocalDateTime createdAt; // Date de création
    private LocalDateTime updatedAt; // Date de mise à jour
    private Integer usageCount; // Nombre d'utilisations
    private String tags; // Tags séparés par des virgules

    @ManyToOne
    private User enfant;
}