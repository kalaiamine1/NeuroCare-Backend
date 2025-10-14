package com.BrainStack.Dto;

import lombok.*;
import java.time.LocalDate;

/**
 * DTO pour la réponse d'un enfant.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChildResponseDTO {
    private int id;
    private String fullName;
    private LocalDate birthDate;
    private String gender;
    private String diagnosis;
    private int parentId;
}
