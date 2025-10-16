package com.BrainStack.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO pour la requête d'analyse de type de rendez-vous
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIAnalysisRequest {
    
    @NotBlank(message = "La description est requise")
    private String description;
    
    private String context; // Contexte supplémentaire (optionnel)
}
