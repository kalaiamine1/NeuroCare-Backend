package com.BrainStack.Services;

import com.BrainStack.Dto.AnomalyDetectionDTO;
import com.BrainStack.Entity.HealthRecord;

import java.util.List;

public interface IAnomalyDetectionService {
    /**
     * Détecte et persiste les anomalies liées à un HealthRecord donné.
     */
    void detectAnomaliesForRecord(HealthRecord record);

    /**
     * Récupère toutes les anomalies d'un enfant.
     */
    List<AnomalyDetectionDTO> getAnomaliesByChildId(int childId);

    /**
     * Récupère les anomalies non résolues d'un enfant.
     */
    List<AnomalyDetectionDTO> getUnresolvedAnomaliesByChildId(int childId);

    /**
     * Marque une anomalie comme résolue.
     */
    void resolveAnomaly(Long anomalyId);
}
