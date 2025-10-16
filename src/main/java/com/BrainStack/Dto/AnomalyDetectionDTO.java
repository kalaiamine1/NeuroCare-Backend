package com.BrainStack.Dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnomalyDetectionDTO {
    private Long id;
    private LocalDateTime detectedAt;
    private String type;
    private String description;
    private Boolean resolved;
    private int childId;
}

