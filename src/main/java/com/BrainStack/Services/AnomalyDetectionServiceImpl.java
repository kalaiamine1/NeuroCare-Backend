package com.BrainStack.Services;

import com.BrainStack.Dto.AnomalyDetectionDTO;
import com.BrainStack.Entity.AnomalyDetection;
import com.BrainStack.Entity.HealthRecord;
import com.BrainStack.Exception.AnomalyNotFoundException;
import com.BrainStack.Repository.AnomalyDetectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AnomalyDetectionServiceImpl implements IAnomalyDetectionService {

    private final AnomalyDetectionRepository anomalyDetectionRepository;

    @Override
    public void detectAnomaliesForRecord(HealthRecord record) {
        if (record == null || record.getChild() == null) {
            return; // rien à faire si record ou enfant manquant
        }

        List<AnomalyDetection> anomalies = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // Règle: Sommeil insuffisant
        if (record.getSleepHours() != null && record.getSleepHours() < 6.0) {
            AnomalyDetection a = new AnomalyDetection();
            a.setDetectedAt(now);
            a.setType("Sommeil insuffisant");
            a.setDescription("Heures de sommeil observées: " + record.getSleepHours());
            a.setResolved(false);
            a.setChild(record.getChild());
            anomalies.add(a);
        }

        // Règle: Baisse d’activité
        if (record.getSteps() != null && record.getSteps() < 3000) {
            AnomalyDetection a = new AnomalyDetection();
            a.setDetectedAt(now);
            a.setType("Baisse d’activité");
            a.setDescription("Nombre de pas observé: " + record.getSteps());
            a.setResolved(false);
            a.setChild(record.getChild());
            anomalies.add(a);
        }

        // Règle: Stress élevé
        if (record.getMood() != null && record.getMood().equalsIgnoreCase("Stressé")) {
            AnomalyDetection a = new AnomalyDetection();
            a.setDetectedAt(now);
            a.setType("Stress élevé");
            a.setDescription("Humeur signalée: " + record.getMood());
            a.setResolved(false);
            a.setChild(record.getChild());
            anomalies.add(a);
        }

        if (!anomalies.isEmpty()) {
            anomalyDetectionRepository.saveAll(anomalies);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnomalyDetectionDTO> getAnomaliesByChildId(int childId) {
        return anomalyDetectionRepository.findByChildId(childId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnomalyDetectionDTO> getUnresolvedAnomaliesByChildId(int childId) {
        return anomalyDetectionRepository.findByChildIdAndResolvedFalse(childId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void resolveAnomaly(Long anomalyId) {
        AnomalyDetection anomaly = anomalyDetectionRepository.findById(anomalyId)
                .orElseThrow(() -> new AnomalyNotFoundException("No anomaly found with id " + anomalyId));
        if (Boolean.TRUE.equals(anomaly.getResolved())) {
            return; // déjà résolue, ne rien faire
        }
        anomaly.setResolved(true);
        anomalyDetectionRepository.save(anomaly);
    }

    private AnomalyDetectionDTO toDto(AnomalyDetection a) {
        return AnomalyDetectionDTO.builder()
                .id(a.getId())
                .detectedAt(a.getDetectedAt())
                .type(a.getType())
                .description(a.getDescription())
                .resolved(a.getResolved())
                .childId(a.getChild() != null ? a.getChild().getId() : 0)
                .build();
    }
}
