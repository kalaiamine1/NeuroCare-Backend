package com.example.healthai;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class ExcelTrainingLoader {
    public static final String RELATIVE_TRAINING_PATH = "src/main/resources/training-data/health_records_training.xlsx";

    public List<HealthRecordLike> loadOrCreateTrainingData() {
        try {
            ensureTrainingFileExists();
            return loadFromFile(getTrainingFilePath());
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du chargement des données d'entraînement", e);
        }
    }

    public void appendRecord(HealthRecordLike rec) {
        try {
            ensureTrainingFileExists();
            Path path = getTrainingFilePath();
            try (FileInputStream fis = new FileInputStream(path.toFile());
                 Workbook workbook = new XSSFWorkbook(fis)) {
                Sheet sheet = workbook.getSheetAt(0);
                int lastRowNum = sheet.getLastRowNum();
                Row row = sheet.createRow(lastRowNum + 1);
                writeRow(row, rec);
                try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
                    workbook.write(fos);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'ajout d'une ligne d'entraînement", e);
        }
    }

    private void ensureTrainingFileExists() throws IOException {
        Path path = getTrainingFilePath();
        if (Files.exists(path)) return;
        Files.createDirectories(path.getParent());
        generateRandomTrainingFile(path, 200);
    }

    private Path getTrainingFilePath() {
        return Paths.get(System.getProperty("user.dir"), RELATIVE_TRAINING_PATH.replace("/", File.separator));
    }

    private List<HealthRecordLike> loadFromFile(Path path) throws IOException {
        List<HealthRecordLike> list = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(path.toFile());
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            int startRow = 1; // skip header
            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                HealthRecordLike rec = new HealthRecordLike(
                        getNumeric(row, 0), // sleepHours
                        getNumeric(row, 1), // steps
                        getNumeric(row, 2), // heartRate
                        getNumeric(row, 3), // weight
                        getNumeric(row, 4), // moodScore
                        getNumeric(row, 5)  // dietScore
                );
                list.add(rec);
            }
        }
        return list;
    }

    private double getNumeric(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return 0d;
        if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
        if (cell.getCellType() == CellType.STRING) {
            try { return Double.parseDouble(cell.getStringCellValue()); } catch (Exception ignored) {}
        }
        return 0d;
    }

    private void generateRandomTrainingFile(Path path, int rows) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("training");
            // header
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("sleepHours");
            header.createCell(1).setCellValue("steps");
            header.createCell(2).setCellValue("heartRate");
            header.createCell(3).setCellValue("weight");
            header.createCell(4).setCellValue("moodScore");
            header.createCell(5).setCellValue("dietScore");

            Random r = new Random();
            for (int i = 1; i <= rows; i++) {
                Row row = sheet.createRow(i);
                HealthRecordLike rec = randomRecord(r);
                writeRow(row, rec);
            }
            Files.createDirectories(path.getParent());
            try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
                workbook.write(fos);
            }
        }
    }

    private HealthRecordLike randomRecord(Random r) {
        double sleep = 4 + r.nextDouble() * 6; // 4 - 10
        double steps = 1000 + r.nextInt(11001); // 1000 - 12000
        double hr = 60 + r.nextInt(71); // 60 - 130
        double weight = 10 + r.nextDouble() * 50; // 10 - 60
        double mood = 1 + r.nextInt(5); // 1 - 5
        double diet = 1 + r.nextInt(5); // 1 - 5
        return new HealthRecordLike(sleep, steps, hr, weight, mood, diet);
    }

    private void writeRow(Row row, HealthRecordLike rec) {
        row.createCell(0).setCellValue(rec.getSleepHours());
        row.createCell(1).setCellValue(rec.getSteps());
        row.createCell(2).setCellValue(rec.getHeartRate());
        row.createCell(3).setCellValue(rec.getWeight());
        row.createCell(4).setCellValue(rec.getMoodScore());
        row.createCell(5).setCellValue(rec.getDietScore());
    }
}

