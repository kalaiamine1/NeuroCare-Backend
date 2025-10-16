package com.BrainStack.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO pour la requête de génération de rappel
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIReminderRequest {
    
    @NotBlank(message = "Le nom de l'enfant est requis")
    private String childName;
    
    @NotBlank(message = "Le nom du professionnel est requis")
    private String professionalName;
    
    @NotBlank(message = "L'heure du rendez-vous est requise")
    private String appointmentTime; // ISO string
    
    private String location;
    
    private String appointmentType; // THERAPEUTIC, MEDICAL, etc.
    
    private String notes; // Notes supplémentaires (optionnel)
}
