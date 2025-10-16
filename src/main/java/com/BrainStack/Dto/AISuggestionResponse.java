package com.BrainStack.Dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * DTO pour la réponse de suggestion d'horaires IA
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AISuggestionResponse {
    
    private List<String> suggestedSlots; // Liste des créneaux suggérés (ISO strings)
    
    private Integer totalSlots;
    
    private String message;
    
    private String generatedAt;
    
    private Double confidence; // Score de confiance de l'IA (0.0 à 1.0)
    
    private String algorithm; // Algorithme utilisé (Prophet, etc.)
}
