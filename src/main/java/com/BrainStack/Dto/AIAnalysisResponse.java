package com.BrainStack.Dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Map;
import java.util.List;

/**
 * DTO pour la réponse d'analyse de type de rendez-vous
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIAnalysisResponse {
    
    private String appointmentType; // THERAPEUTIC, MEDICAL, PSYCHOLOGICAL, CONSULTATION
    
    private Double confidence; // Score de confiance (0.0 à 1.0)
    
    private Map<String, Integer> scores; // Scores pour chaque type
    
    private List<EntityInfo> entities; // Entités extraites du texte
    
    private Integer suggestedDuration; // Durée suggérée en minutes
    
    private String generatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EntityInfo {
        private String text;
        private String label;
        private String description;
    }
}
