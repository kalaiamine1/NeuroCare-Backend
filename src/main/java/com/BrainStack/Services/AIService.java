package com.BrainStack.Services;

import com.BrainStack.Dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Service pour l'intégration avec le microservice IA Python
 */
@Service
public class AIService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);

    @Value("${ai.microservice.url:http://localhost:5000}")
    private String aiMicroserviceUrl;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Suggère des horaires optimaux en utilisant l'IA
     */
    public AISuggestionResponse suggestOptimalTimes(AISuggestionRequest request) {
        log.info("Demande de suggestion d'horaires pour professionnel: {}", request.getProfessionalId());

        try {
            String url = aiMicroserviceUrl + "/ai/suggest";
            
            // Préparation de la requête pour le microservice Python
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("professionalId", request.getProfessionalId());
            requestBody.put("durationMinutes", request.getDurationMinutes());
            if (request.getPreferredDate() != null) {
                requestBody.put("preferredDate", request.getPreferredDate());
            }
            if (request.getTimePreference() != null) {
                requestBody.put("timePreference", request.getTimePreference());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                
                return AISuggestionResponse.builder()
                        .suggestedSlots((java.util.List<String>) responseBody.get("suggestedSlots"))
                        .totalSlots((Integer) responseBody.get("totalSlots"))
                        .message((String) responseBody.get("message"))
                        .generatedAt(java.time.LocalDateTime.now().toString())
                        .confidence(0.9) // Score par défaut
                        .algorithm("Prophet")
                        .build();
            } else {
                throw new RuntimeException("Réponse invalide du microservice IA");
            }

        } catch (ResourceAccessException e) {
            log.error("Impossible de se connecter au microservice IA", e);
            return getFallbackSuggestion(request);
        } catch (HttpClientErrorException e) {
            log.error("Erreur HTTP du microservice IA: {}", e.getStatusCode(), e);
            return getFallbackSuggestion(request);
        } catch (Exception e) {
            log.error("Erreur lors de la suggestion d'horaires", e);
            return getFallbackSuggestion(request);
        }
    }

    /**
     * Analyse le type de rendez-vous à partir de la description
     */
    public AIAnalysisResponse analyzeAppointmentType(AIAnalysisRequest request) {
        log.info("Analyse du type de rendez-vous pour description: {}", request.getDescription());

        try {
            String url = aiMicroserviceUrl + "/ai/analyze";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("description", request.getDescription());
            if (request.getContext() != null) {
                requestBody.put("context", request.getContext());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Map<String, Object> analysis = (Map<String, Object>) responseBody.get("analysis");
                
                return AIAnalysisResponse.builder()
                        .appointmentType((String) analysis.get("type"))
                        .confidence(((Number) analysis.get("confidence")).doubleValue())
                        .scores((Map<String, Integer>) analysis.get("scores"))
                        .entities(convertEntities((java.util.List<Map<String, Object>>) analysis.get("entities")))
                        .suggestedDuration((Integer) analysis.get("suggested_duration"))
                        .generatedAt(java.time.LocalDateTime.now().toString())
                        .build();
            } else {
                throw new RuntimeException("Réponse invalide du microservice IA");
            }

        } catch (ResourceAccessException e) {
            log.error("Impossible de se connecter au microservice IA", e);
            return getFallbackAnalysis(request);
        } catch (HttpClientErrorException e) {
            log.error("Erreur HTTP du microservice IA: {}", e.getStatusCode(), e);
            return getFallbackAnalysis(request);
        } catch (Exception e) {
            log.error("Erreur lors de l'analyse du type de rendez-vous", e);
            return getFallbackAnalysis(request);
        }
    }

    /**
     * Génère un message de rappel personnalisé
     */
    public AIReminderResponse generateReminder(AIReminderRequest request) {
        log.info("Génération d'un message de rappel pour: {}", request.getChildName());

        try {
            String url = aiMicroserviceUrl + "/ai/reminder";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("child_name", request.getChildName());
            requestBody.put("professional_name", request.getProfessionalName());
            requestBody.put("appointment_time", request.getAppointmentTime());
            requestBody.put("location", request.getLocation());
            requestBody.put("type", request.getAppointmentType());
            requestBody.put("notes", request.getNotes());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                
                return AIReminderResponse.builder()
                        .message((String) responseBody.get("message"))
                        .generatedAt((String) responseBody.get("generated_at"))
                        .language("fr")
                        .tone("professional")
                        .estimatedLength(((String) responseBody.get("message")).length())
                        .build();
            } else {
                throw new RuntimeException("Réponse invalide du microservice IA");
            }

        } catch (ResourceAccessException e) {
            log.error("Impossible de se connecter au microservice IA", e);
            return getFallbackReminder(request);
        } catch (HttpClientErrorException e) {
            log.error("Erreur HTTP du microservice IA: {}", e.getStatusCode(), e);
            return getFallbackReminder(request);
        } catch (Exception e) {
            log.error("Erreur lors de la génération du rappel", e);
            return getFallbackReminder(request);
        }
    }

    /**
     * Vérifie l'état du service IA
     */
    public String healthCheck() {
        try {
            String url = aiMicroserviceUrl + "/ai/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return "Service IA opérationnel";
            } else {
                return "Service IA en panne";
            }
        } catch (Exception e) {
            log.error("Service IA indisponible", e);
            return "Service IA indisponible";
        }
    }

    /**
     * Méthodes de fallback en cas d'indisponibilité du microservice IA
     */
    private AISuggestionResponse getFallbackSuggestion(AISuggestionRequest request) {
        log.warn("Utilisation du mode fallback pour les suggestions d'horaires");
        
        // Génération de créneaux par défaut
        java.util.List<String> defaultSlots = java.util.Arrays.asList(
                java.time.LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).toString(),
                java.time.LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).toString(),
                java.time.LocalDateTime.now().plusDays(1).withHour(14).withMinute(0).toString(),
                java.time.LocalDateTime.now().plusDays(1).withHour(15).withMinute(0).toString()
        );

        return AISuggestionResponse.builder()
                .suggestedSlots(defaultSlots)
                .totalSlots(defaultSlots.size())
                .message("Suggestions générées en mode dégradé")
                .generatedAt(java.time.LocalDateTime.now().toString())
                .confidence(0.5)
                .algorithm("Fallback")
                .build();
    }

    private AIAnalysisResponse getFallbackAnalysis(AIAnalysisRequest request) {
        log.warn("Utilisation du mode fallback pour l'analyse de type");
        
        Map<String, Integer> defaultScores = new HashMap<>();
        defaultScores.put("CONSULTATION", 1);
        
        return AIAnalysisResponse.builder()
                .appointmentType("CONSULTATION")
                .confidence(0.5)
                .scores(defaultScores)
                .entities(java.util.Collections.emptyList())
                .suggestedDuration(30)
                .generatedAt(java.time.LocalDateTime.now().toString())
                .build();
    }

    private AIReminderResponse getFallbackReminder(AIReminderRequest request) {
        log.warn("Utilisation du mode fallback pour la génération de rappel");
        
        String fallbackMessage = String.format(
                "Rappel: Rendez-vous pour %s avec %s le %s",
                request.getChildName(),
                request.getProfessionalName(),
                request.getAppointmentTime()
        );

        return AIReminderResponse.builder()
                .message(fallbackMessage)
                .generatedAt(java.time.LocalDateTime.now().toString())
                .language("fr")
                .tone("professional")
                .estimatedLength(fallbackMessage.length())
                .build();
    }

    private java.util.List<AIAnalysisResponse.EntityInfo> convertEntities(java.util.List<Map<String, Object>> entities) {
        return entities.stream()
                .map(entity -> AIAnalysisResponse.EntityInfo.builder()
                        .text((String) entity.get("text"))
                        .label((String) entity.get("label"))
                        .description((String) entity.get("description"))
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }
}
