package com.BrainStack.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyReportDTO {
    private int childId;
    private long total;
    private long resolved;
    private long unresolved;
    private List<String> dominantTypes; // top types
    private Map<String, Long> countsByType; // répartition
}

