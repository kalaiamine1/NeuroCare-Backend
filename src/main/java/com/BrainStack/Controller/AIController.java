package com.BrainStack.Controller;

import com.BrainStack.Dto.ApiResponse;
import com.BrainStack.Dto.AISuggestionRequest;
import com.BrainStack.Dto.AISuggestionResponse;
import com.BrainStack.Dto.AIAnalysisRequest;
import com.BrainStack.Dto.AIAnalysisResponse;
import com.BrainStack.Dto.AIReminderRequest;
import com.BrainStack.Dto.AIReminderResponse;
import com.BrainStack.Services.AIService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;

import java.util.List;

/**
 * Controller pour les fonctionnalités d'Intelligence Artificielle
 * Base URL: /api/v1/ai
 */
@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "*")
@Tag(name = "AI Services", description = "Intelligence Artificielle pour les rendez-vous")
public class AIController {

    @Autowired
    private AIService aiService;
    private static final Logger log = LoggerFactory.getLogger(AIController.class);

    /**
     * Suggère des horaires optimaux basés sur l'IA
     * POST /api/v1/ai/suggest
     */
    @PostMapping("/suggest")
    public ResponseEntity<ApiResponse<AISuggestionResponse>> suggestOptimalTimes(
            @Valid @RequestBody AISuggestionRequest request) {
        log.info("POST /ai/suggest - Suggestion d'horaires optimaux pour professionnel: {}", request.getProfessionalId());

        try {
            AISuggestionResponse response = aiService.suggestOptimalTimes(request);
            return ResponseEntity.ok(ApiResponse.success("Suggestions d'horaires générées", response));
        } catch (Exception e) {
            log.error("Erreur lors de la génération des suggestions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de la génération des suggestions",
                            List.of(e.getMessage())));
        }
    }

    /**
     * Analyse le type de rendez-vous à partir de la description
     * POST /api/v1/ai/analyze
     */
    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<AIAnalysisResponse>> analyzeAppointmentType(
            @Valid @RequestBody AIAnalysisRequest request) {
        log.info("POST /ai/analyze - Analyse du type de rendez-vous");

        try {
            AIAnalysisResponse response = aiService.analyzeAppointmentType(request);
            return ResponseEntity.ok(ApiResponse.success("Analyse terminée", response));
        } catch (Exception e) {
            log.error("Erreur lors de l'analyse", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de l'analyse",
                            List.of(e.getMessage())));
        }
    }

    /**
     * Génère un message de rappel personnalisé
     * POST /api/v1/ai/reminder
     */
    @PostMapping("/reminder")
    public ResponseEntity<ApiResponse<AIReminderResponse>> generateReminder(
            @Valid @RequestBody AIReminderRequest request) {
        log.info("POST /ai/reminder - Génération d'un message de rappel");

        try {
            AIReminderResponse response = aiService.generateReminder(request);
            return ResponseEntity.ok(ApiResponse.success("Message de rappel généré", response));
        } catch (Exception e) {
            log.error("Erreur lors de la génération du rappel", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de la génération du rappel",
                            List.of(e.getMessage())));
        }
    }

    /**
     * Vérifie l'état du service IA
     * GET /api/v1/ai/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        log.info("GET /ai/health - Vérification de l'état du service IA");

        try {
            String status = aiService.healthCheck();
            return ResponseEntity.ok(ApiResponse.success("Service IA opérationnel", status));
        } catch (Exception e) {
            log.error("Service IA indisponible", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.error("Service IA indisponible",
                            List.of(e.getMessage())));
        }
    }
}
