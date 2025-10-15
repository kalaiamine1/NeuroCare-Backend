package com.BrainStack.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Point de série temporelle pour un paramètre donné (utilisé par l'endpoint chart).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSeriesPointDTO {
    // Date (jour) au format ISO-8601 yyyy-MM-dd
    private String date;
    private Double value;
}

