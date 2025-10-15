package com.BrainStack.Controller;

import com.BrainStack.Dto.StatsPointDTO;
import com.BrainStack.Dto.TimeSeriesPointDTO;
import com.BrainStack.Services.IStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/health-records")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Endpoints pour les statistiques de santé des enfants")
public class StatisticsController {

    private final IStatisticsService statisticsService;

    @GetMapping("/child/{childId}/weekly")
    @Operation(summary = "Moyennes journalières sur la dernière semaine",
            description = "Retourne, pour chaque jour des 7 derniers jours, les moyennes des heures de sommeil, poids, fréquence cardiaque et pas.")
    @ApiResponse(responseCode = "200", description = "Statistiques hebdomadaires retournées avec succès")
    @ApiResponse(responseCode = "404", description = "Enfant introuvable")
    public ResponseEntity<List<StatsPointDTO>> getWeeklyStats(
            @Parameter(description = "Identifiant de l'enfant")
            @PathVariable int childId) {
        return ResponseEntity.ok(statisticsService.getWeeklyStats(childId));
    }

    @GetMapping("/child/{childId}/monthly")
    @Operation(summary = "Moyennes mensuelles sur les 6 derniers mois",
            description = "Retourne, pour chaque mois sur 6 mois, les moyennes des heures de sommeil, poids, fréquence cardiaque et pas.")
    @ApiResponse(responseCode = "200", description = "Statistiques mensuelles retournées avec succès")
    @ApiResponse(responseCode = "404", description = "Enfant introuvable")
    public ResponseEntity<List<StatsPointDTO>> getMonthlyStats(
            @Parameter(description = "Identifiant de l'enfant")
            @PathVariable int childId) {
        return ResponseEntity.ok(statisticsService.getMonthlyStats(childId));
    }

    @GetMapping("/child/{childId}/chart")
    @Operation(summary = "Série journalière pour un paramètre donné",
            description = "Retourne les valeurs journalières moyennes pour un paramètre (sleep|weight|heartRate|steps) sur une période (ex: 6m, 30d)")
    @ApiResponse(responseCode = "200", description = "Série temporelle retournée avec succès")
    @ApiResponse(responseCode = "400", description = "Paramètres invalides")
    @ApiResponse(responseCode = "404", description = "Enfant introuvable")
    public ResponseEntity<List<TimeSeriesPointDTO>> getChartSeries(
            @Parameter(description = "Identifiant de l'enfant")
            @PathVariable int childId,
            @Parameter(description = "Paramètre: sleep|weight|heartRate|steps")
            @RequestParam String param,
            @Parameter(description = "Période: ex 7d, 30d, 6m")
            @RequestParam String period) {
        return ResponseEntity.ok(statisticsService.getChartSeries(childId, param, period));
    }
}

