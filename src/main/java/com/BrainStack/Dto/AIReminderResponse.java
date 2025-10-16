package com.BrainStack.Dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO pour la réponse de génération de rappel
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIReminderResponse {
    
    private String message; // Message de rappel généré
    
    private String generatedAt;
    
    private String language; // Langue du message (fr, en, etc.)
    
    private String tone; // Ton du message (professional, friendly, etc.)
    
    private Integer estimatedLength; // Longueur estimée en caractères
}
