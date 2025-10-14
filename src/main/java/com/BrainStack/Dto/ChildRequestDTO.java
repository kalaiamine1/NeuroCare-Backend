package com.BrainStack.Dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

/**
 * DTO pour la création/modification d'un enfant.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChildRequestDTO {
    @NotBlank(message = "Le nom complet est obligatoire.")
    private String fullName;

    @NotNull(message = "La date de naissance est obligatoire.")
    private LocalDate birthDate;

    @NotBlank(message = "Le genre est obligatoire.")
    @Pattern(regexp = "M|F", message = "Le genre doit être 'M' ou 'F'.")
    private String gender;

    @NotBlank(message = "Le diagnostic est obligatoire.")
    private String diagnosis;
}


