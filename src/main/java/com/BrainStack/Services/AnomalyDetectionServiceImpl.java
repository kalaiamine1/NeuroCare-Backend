package com.BrainStack.Services;

import com.BrainStack.Dto.AnomalyDetectionDTO;
import com.BrainStack.Dto.AnomalyReportDTO;
import com.BrainStack.Entity.AnomalyDetection;
import com.BrainStack.Entity.HealthRecord;
import com.BrainStack.Exception.AnomalyNotFoundException;
import com.BrainStack.Repository.AnomalyDetectionRepository;
import com.BrainStack.Repository.HealthRecordRepository;
import com.example.healthai.AnomalyDetectionAIService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AnomalyDetectionServiceImpl implements IAnomalyDetectionService {

    private final AnomalyDetectionRepository anomalyDetectionRepository;

    // Injection optionnelle pour ne pas casser les tests unitaires existants
    @Autowired(required = false)
    private AnomalyDetectionAIService anomalyDetectionAIService;
    @Autowired(required = false)
    private HealthRecordRepository healthRecordRepository;

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
            a.setDescription("Heures de sommeil observées: " + record.getSleepHours() + ". Conseils: Essayez d’établir une routine de coucher régulière.");
            a.setResolved(false);
            a.setChild(record.getChild());
            anomalies.add(a);
        }

        // Règle: Baisse d’activité
        if (record.getSteps() != null && record.getSteps() < 3000) {
            AnomalyDetection a = new AnomalyDetection();
            a.setDetectedAt(now);
            a.setType("Baisse d’activité");
            a.setDescription("Nombre de pas observé: " + record.getSteps() + ". Conseils: Encouragez plus de marche ou jeux extérieurs.");
            a.setResolved(false);
            a.setChild(record.getChild());
            anomalies.add(a);
        }

        // Règle: Stress élevé
        if (record.getMood() != null && record.getMood().equalsIgnoreCase("Stressé")) {
            AnomalyDetection a = new AnomalyDetection();
            a.setDetectedAt(now);
            a.setType("Stress élevé");
            a.setDescription("Humeur signalée: " + record.getMood() + ". Conseils: Vérifiez le niveau de stress ou la fatigue.");
            a.setResolved(false);
            a.setChild(record.getChild());
            anomalies.add(a);
        }

        // IA dynamique si disponible
        if (anomalyDetectionAIService != null) {
            List<AnomalyDetection> aiAnoms = anomalyDetectionAIService.detectDynamicAnomalies(record);
            // attacher l'enfant et enrichir la description si besoin
            for (AnomalyDetection ai : aiAnoms) {
                ai.setChild(record.getChild());
                if (ai.getDescription() == null || ai.getDescription().isBlank()) {
                    String advice = anomalyDetectionAIService.generateAdvice(ai.getType());
                    ai.setDescription("Anomalie " + ai.getType() + ". " + advice);
                }
            }
            // fusion sans doublons par (type, description)
            if (!aiAnoms.isEmpty()) {
                Set<String> sigs = anomalies.stream().map(a -> a.getType() + "|" + a.getDescription()).collect(Collectors.toSet());
                for (AnomalyDetection a : aiAnoms) {
                    String sig = a.getType() + "|" + a.getDescription();
                    if (!sigs.contains(sig)) {
                        anomalies.add(a);
                        sigs.add(sig);
                    }
                }
            }
        }

        if (!anomalies.isEmpty()) {
            anomalyDetectionRepository.saveAll(anomalies);
        }

        // Résolution automatique si amélioration détectée (uniquement si IA et repository disponibles)
        if (anomalyDetectionAIService != null && healthRecordRepository != null && record.getChild() != null) {
            try {
                List<HealthRecord> history = healthRecordRepository.findByChildId(record.getChild().getId());
                if (history != null && history.size() >= 2) {
                    history.sort(Comparator.comparing(HealthRecord::getDate, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(HealthRecord::getId));
                    HealthRecord last = history.get(history.size() - 2); // précédent
                    boolean improved = anomalyDetectionAIService.checkImprovement(last, record);
                    if (improved) {
                        List<AnomalyDetection> unresolved = anomalyDetectionRepository.findByChildIdAndResolvedFalse(record.getChild().getId());
                        for (AnomalyDetection a : unresolved) {
                            a.setResolved(true);
                        }
                        if (!unresolved.isEmpty()) anomalyDetectionRepository.saveAll(unresolved);
                    }
                }
            } catch (Exception ignored) {}
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

    @Override
    @Transactional(readOnly = true)
    public AnomalyReportDTO getReportByChildId(int childId) {
        List<AnomalyDetection> list = anomalyDetectionRepository.findByChildId(childId);
        long total = list.size();
        long resolved = list.stream().filter(a -> Boolean.TRUE.equals(a.getResolved())).count();
        long unresolved = total - resolved;
        Map<String, Long> countsByType = list.stream()
                .collect(Collectors.groupingBy(a -> Optional.ofNullable(a.getType()).orElse("Unknown"), Collectors.counting()));
        List<String> dominant = countsByType.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        return AnomalyReportDTO.builder()
                .childId(childId)
                .total(total)
                .resolved(resolved)
                .unresolved(unresolved)
                .countsByType(countsByType)
                .dominantTypes(dominant)
                .build();
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
