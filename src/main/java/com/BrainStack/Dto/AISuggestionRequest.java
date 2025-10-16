package com.BrainStack.Dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO pour la requête de suggestion d'horaires IA
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AISuggestionRequest {
    
    @NotNull(message = "L'ID du professionnel est requis")
    private Long professionalId;
    
    @Min(value = 15, message = "La durée minimale est de 15 minutes")
    private Integer durationMinutes = 60;
    
    private String preferredDate; // Format ISO string (optionnel)
    
    private String timePreference; // "morning", "afternoon", "evening" (optionnel)
}
