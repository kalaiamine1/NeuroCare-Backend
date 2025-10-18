package com.example.healthai;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import smile.anomaly.IsolationForest;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class AIModelTrainer implements Serializable {

    public static final String RELATIVE_MODEL_PATH = "src/main/resources/model/anomaly_model.ser";

    @Getter
    private IsolationForest isolationForest; // peut être null si non entraîné

    // Fallback statistiques simples
    @Getter
    private double[] means; // taille 6
    @Getter
    private double[] stds;  // taille 6

    public void trainAndSave(List<HealthRecordLike> data) {
        if (data == null || data.isEmpty()) {
            log.warn("Aucune donnée d'entraînement fournie, annulation");
            return;
        }
        double[][] X = data.stream().map(HealthRecordLike::toArray).toArray(double[][]::new);
        try {
            // Entraîner IsolationForest avec configuration par défaut
            isolationForest = IsolationForest.fit(X);
            computeStats(X);
            saveModel();
            log.info("Modèle IsolationForest entraîné et sauvegardé (config par défaut)");
        } catch (Throwable t) {
            log.error("Échec entraînement IsolationForest, fallback Z-Score", t);
            isolationForest = null;
            computeStats(X);
            saveModel();
        }
    }

    public boolean loadModelIfExists() {
        Path path = getModelPath();
        if (!Files.exists(path)) return false;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()))) {
            AIModelTrainer loaded = (AIModelTrainer) ois.readObject();
            this.isolationForest = loaded.isolationForest;
            this.means = loaded.means;
            this.stds = loaded.stds;
            return true;
        } catch (Exception e) {
            log.error("Impossible de charger le modèle, on repartira d'un entraînement.", e);
            return false;
        }
    }

    public double predictAnomalyScore(HealthRecordLike rec) {
        double[] x = rec.toArray();
        if (isolationForest != null) {
            try {
                // Smile: plus le score est élevé, plus c'est anormal
                double score = isolationForest.score(x);
                if (Double.isFinite(score)) return score;
            } catch (Throwable t) {
                log.warn("Score IsolationForest échoué, fallback Z-Score", t);
            }
        }
        // fallback: Z-score moyen des features
        if (means == null || stds == null) return 0.0;
        double sum = 0.0;
        int n = x.length;
        int count = 0;
        for (int i = 0; i < n; i++) {
            double s = stds[i] == 0.0 ? 0.0 : Math.abs((x[i] - means[i]) / stds[i]);
            if (Double.isFinite(s)) {
                sum += s;
                count++;
            }
        }
        return count == 0 ? 0.0 : sum / count; // ~ >2 indique anomalie
    }

    private void computeStats(double[][] X) {
        int d = X[0].length;
        means = new double[d];
        stds = new double[d];
        for (int j = 0; j < d; j++) {
            double m = 0.0;
            for (double[] doubles : X) m += doubles[j];
            m /= X.length;
            means[j] = m;
            double v = 0.0;
            for (double[] doubles : X) {
                double diff = doubles[j] - m;
                v += diff * diff;
            }
            v /= Math.max(1, X.length - 1);
            stds[j] = Math.sqrt(v);
        }
    }

    private void saveModel() {
        Path path = getModelPath();
        try {
            Files.createDirectories(path.getParent());
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path.toFile()))) {
                oos.writeObject(this);
            }
        } catch (IOException e) {
            log.error("Erreur lors de la sauvegarde du modèle IA", e);
        }
    }

    public static Path getModelPath() {
        return Paths.get(System.getProperty("user.dir"), RELATIVE_MODEL_PATH.replace("/", File.separator));
    }
}
