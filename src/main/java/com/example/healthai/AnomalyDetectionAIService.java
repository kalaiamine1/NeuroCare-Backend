package com.example.healthai;

import com.BrainStack.Entity.AnomalyDetection;
import com.BrainStack.Entity.HealthRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnomalyDetectionAIService {

    private final ExcelTrainingLoader excelTrainingLoader;
    private AIModelTrainer modelTrainer = new AIModelTrainer();

    @PostConstruct
    public void init() {
        try {
            // Assurer dossier des logs
            new File("logs").mkdirs();
            boolean loaded = modelTrainer.loadModelIfExists();
            if (!loaded) {
                log.info("Modèle IA absent, entraînement initial à partir de l'Excel...");
                trainModelFromExcel();
            } else {
                log.info("Modèle IA chargé depuis le disque.");
            }
        } catch (Exception e) {
            log.error("Échec initialisation du modèle IA, tentative d'entraînement.", e);
            trainModelFromExcel();
        }
    }

    public void trainModelFromExcel() {
        List<HealthRecordLike> data = excelTrainingLoader.loadOrCreateTrainingData();
        modelTrainer.trainAndSave(data);
    }

    public List<AnomalyDetection> detectDynamicAnomalies(HealthRecord newRecord) {
        List<AnomalyDetection> anomalies = new ArrayList<>();
        if (newRecord == null) return anomalies;
        HealthRecordLike like = toLike(newRecord);
        double score = modelTrainer.predictAnomalyScore(like);
        log.info("AI score={} pour record id={} child={}", String.format(Locale.US, "%.3f", score),
                newRecord.getId(), newRecord.getChild() != null ? newRecord.getChild().getId() : null);

        // Heuristique de seuil: si IsolationForest -> seuil ~0.6, sinon ZScore moyen -> seuil ~2.0
        boolean iso = modelTrainer.getIsolationForest() != null;
        double threshold = iso ? 0.6 : 2.0;
        if (score < threshold) {
            // Pas d'anomalie globale
            appendTrainingExample(newRecord);
            return anomalies;
        }

        // Décomposer en anomalies par feature significatives via Z-scores
        double[] xs = like.toArray();
        double[] means = modelTrainer.getMeans();
        double[] stds = modelTrainer.getStds();
        String[] types = new String[]{"Sleep", "Steps", "HeartRate", "Weight", "Mood", "Diet"};
        for (int i = 0; i < xs.length; i++) {
            double z = (stds != null && stds.length > i && stds[i] != 0) ? Math.abs((xs[i] - means[i]) / stds[i]) : 0.0;
            if (z >= 2.0) { // significatif
                String type = types[i];
                String advice = generateAdvice(type);
                String desc = String.format(Locale.US, "Anomalie %s détectée (z=%.2f, valeur=%.2f). %s", type, z, xs[i], advice);
                anomalies.add(AnomalyDetection.builder()
                        .detectedAt(LocalDateTime.now())
                        .type(type)
                        .description(desc)
                        .resolved(false)
                        .build());
            }
        }

        appendTrainingExample(newRecord);
        return anomalies;
    }

    public boolean checkImprovement(HealthRecord lastRecord, HealthRecord newRecord) {
        if (lastRecord == null || newRecord == null) return false;
        boolean improved = false;
        if (lastRecord.getSleepHours() != null && newRecord.getSleepHours() != null) {
            improved |= newRecord.getSleepHours() - lastRecord.getSleepHours() >= 0.5;
        }
        if (lastRecord.getSteps() != null && newRecord.getSteps() != null) {
            improved |= newRecord.getSteps() - lastRecord.getSteps() >= 500;
        }
        if (lastRecord.getHeartRate() != null && newRecord.getHeartRate() != null) {
            improved |= lastRecord.getHeartRate() - newRecord.getHeartRate() >= 5.0;
        }
        // Mood
        Integer lastMood = parseMood(lastRecord.getMood());
        Integer newMood = parseMood(newRecord.getMood());
        if (lastMood != null && newMood != null) {
            improved |= newMood - lastMood >= 1; // au moins +1
        }
        // Diet
        Integer lastDiet = parseDiet(lastRecord.getDietQuality());
        Integer newDiet = parseDiet(newRecord.getDietQuality());
        if (lastDiet != null && newDiet != null) {
            improved |= newDiet - lastDiet >= 1;
        }
        return improved;
    }

    public String generateAdvice(String anomalyType) {
        if (anomalyType == null) return "Continuez à suivre l’évolution pour confirmer la tendance.";
        return switch (anomalyType) {
            case "Sleep" -> "Essayez de maintenir une durée de sommeil régulière de 8h par nuit.";
            case "Steps" -> "Encouragez l’enfant à pratiquer des activités physiques légères chaque jour.";
            case "HeartRate" -> "Vérifiez le niveau de stress ou la fatigue.";
            case "Weight" -> "Surveillez l’alimentation et consultez si la tendance persiste.";
            case "Mood" -> "Proposez des activités apaisantes et discutez des émotions.";
            case "Diet" -> "Favorisez des repas équilibrés et réguliers.";
            default -> "Continuez à suivre l’évolution pour confirmer la tendance.";
        };
    }

    private void appendTrainingExample(HealthRecord newRecord) {
        try {
            excelTrainingLoader.appendRecord(toLike(newRecord));
        } catch (Exception e) {
            log.warn("Impossible d'ajouter l'exemple d'entraînement: {}", e.getMessage());
        }
    }

    private HealthRecordLike toLike(HealthRecord r) {
        double sleep = safe(r.getSleepHours());
        double steps = r.getSteps() != null ? r.getSteps().doubleValue() : 0.0;
        double hr = safe(r.getHeartRate());
        double w = safe(r.getWeight());
        double mood = parseMood(r.getMood()) != null ? parseMood(r.getMood()) : 3;
        double diet = parseDiet(r.getDietQuality()) != null ? parseDiet(r.getDietQuality()) : 3;
        return new HealthRecordLike(sleep, steps, hr, w, mood, diet);
    }

    private double safe(Double v) { return v == null ? 0.0 : v; }

    private Integer parseMood(String mood) {
        if (mood == null) return null;
        String m = mood.toLowerCase(Locale.ROOT);
        if (m.contains("heureux") || m.contains("happy")) return 5;
        if (m.contains("fatigu")) return 2;
        if (m.contains("stress")) return 1;
        if (m.contains("neutre") || m.contains("ok")) return 3;
        return 3;
    }

    private Integer parseDiet(String diet) {
        if (diet == null) return null;
        String d = diet.toLowerCase(Locale.ROOT);
        if (d.contains("équilibr") || d.contains("equilibr")) return 5;
        if (d.contains("trop sucr") || d.contains("sucre")) return 2;
        if (d.contains("gras")) return 2;
        return 3;
    }

    // Réentraînement périodique (tous les jours à 02:00)
    @Scheduled(cron = "0 0 2 * * *")
    public void periodicRetraining() {
        try {
            log.info("Déclenchement du réentraînement quotidien du modèle IA...");
            trainModelFromExcel();
        } catch (Exception e) {
            log.error("Échec du réentraînement quotidien", e);
        }
    }
}

