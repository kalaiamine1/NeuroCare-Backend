package com.BrainStack.Services;

import com.BrainStack.Dto.AIReminderRequest;
import com.BrainStack.Dto.AIReminderResponse;
import com.BrainStack.Entity.Appointment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 🤖 Service IA pour la génération de rappels personnalisés
 * =========================================================
 * 
 * Ce service communique avec le microservice Python Flask
 * pour générer des rappels personnalisés pour les rendez-vous.
 * 
 * @author Agent IA Expert
 * @version 1.0.0
 */
@Service
public class ReminderAIService {

    private static final Logger log = LoggerFactory.getLogger(ReminderAIService.class);

    @Value("${ai.microservice.url:http://localhost:5000}")
    private String aiMicroserviceUrl;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 🎯 Génère un rappel personnalisé pour un rendez-vous
     * 
     * @param appointment L'entité Appointment
     * @return AIReminderResponse avec le rappel généré
     */
    public AIReminderResponse generateReminderForAppointment(Appointment appointment) {
        log.info("🤖 Génération rappel pour RDV ID: {} - Enfant: {}", 
                appointment.getId(), appointment.getTitle());

        try {
            // Conversion de l'entité en Map pour le microservice
            Map<String, Object> appointmentData = convertAppointmentToMap(appointment);
            
            // Appel au microservice Python
            return generateReminder(appointmentData);
            
        } catch (Exception e) {
            log.error("❌ Erreur génération rappel pour RDV ID: {}", appointment.getId(), e);
            return generateFallbackReminder(appointment);
        }
    }

    /**
     * 📦 Génère des rappels en lot pour plusieurs rendez-vous
     * 
     * @param appointments Liste des rendez-vous
     * @return Map avec les résultats de génération
     */
    public Map<String, Object> generateBatchReminders(List<Appointment> appointments) {
        log.info("📦 Génération batch pour {} rendez-vous", appointments.size());

        try {
            // Conversion des entités en Map
            List<Map<String, Object>> appointmentsData = new ArrayList<>();
            for (Appointment appointment : appointments) {
                appointmentsData.add(convertAppointmentToMap(appointment));
            }

            // Préparation de la requête
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("appointments", appointmentsData);

            // Appel au microservice
            String url = aiMicroserviceUrl + "/batch-reminders";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                log.info("✅ Batch généré: {} rappels sur {} demandés", 
                        responseBody.get("count"), appointments.size());
                return responseBody;
            } else {
                throw new RuntimeException("Réponse invalide du microservice IA");
            }

        } catch (ResourceAccessException e) {
            log.error("❌ Impossible de se connecter au microservice IA", e);
            return generateFallbackBatch(appointments);
        } catch (HttpClientErrorException e) {
            log.error("❌ Erreur HTTP du microservice IA: {}", e.getStatusCode(), e);
            return generateFallbackBatch(appointments);
        } catch (Exception e) {
            log.error("❌ Erreur lors de la génération batch", e);
            return generateFallbackBatch(appointments);
        }
    }

    /**
     * 🔍 Vérifie la santé du microservice IA
     * 
     * @return true si le service est disponible
     */
    public boolean isAIServiceAvailable() {
        try {
            String url = aiMicroserviceUrl + "/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> body = response.getBody();
                return "healthy".equals(body.get("status"));
            }
            return false;
        } catch (Exception e) {
            log.warn("⚠️ Service IA indisponible: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 📋 Récupère les templates disponibles du microservice
     * 
     * @return Map avec les templates
     */
    public Map<String, Object> getAvailableTemplates() {
        try {
            String url = aiMicroserviceUrl + "/templates";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
            return Collections.emptyMap();
        } catch (Exception e) {
            log.error("❌ Erreur récupération templates", e);
            return Collections.emptyMap();
        }
    }

    /**
     * 🎯 Génère un rappel unique via le microservice
     * 
     * @param appointmentData Données du rendez-vous
     * @return AIReminderResponse
     */
    private AIReminderResponse generateReminder(Map<String, Object> appointmentData) {
        String url = aiMicroserviceUrl + "/generate-reminder";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(appointmentData, headers);

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
    }

    /**
     * 🔄 Convertit une entité Appointment en Map pour le microservice
     * 
     * @param appointment L'entité Appointment
     * @return Map avec les données formatées
     */
    private Map<String, Object> convertAppointmentToMap(Appointment appointment) {
        Map<String, Object> data = new HashMap<>();
        
        data.put("childName", appointment.getTitle()); // Utilise le title comme nom d'enfant
        data.put("professionalName", "Dr. " + appointment.getProfessionalId()); // Placeholder
        data.put("startTime", appointment.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        data.put("type", appointment.getType().name());
        data.put("location", appointment.getLocation() != null ? appointment.getLocation() : "");
        data.put("notes", appointment.getNotes() != null ? appointment.getNotes() : "");
        
        return data;
    }

    /**
     * 🆘 Génère un rappel de secours en cas d'erreur
     * 
     * @param appointment L'entité Appointment
     * @return AIReminderResponse de secours
     */
    private AIReminderResponse generateFallbackReminder(Appointment appointment) {
        log.warn("🆘 Utilisation du mode fallback pour RDV ID: {}", appointment.getId());
        
        String fallbackMessage = String.format(
                "📋 Rappel: %s a un rendez-vous %s le %s%s%s",
                appointment.getTitle(),
                appointment.getType().getLabel().toLowerCase(),
                appointment.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")),
                appointment.getLocation() != null ? " - " + appointment.getLocation() : "",
                appointment.getNotes() != null ? " - " + appointment.getNotes() : ""
        );

        return AIReminderResponse.builder()
                .message(fallbackMessage)
                .generatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .language("fr")
                .tone("professional")
                .estimatedLength(fallbackMessage.length())
                .build();
    }

    /**
     * 🆘 Génère des rappels de secours en lot
     * 
     * @param appointments Liste des rendez-vous
     * @return Map avec les résultats de secours
     */
    private Map<String, Object> generateFallbackBatch(List<Appointment> appointments) {
        log.warn("🆘 Utilisation du mode fallback pour batch de {} rendez-vous", appointments.size());
        
        List<Map<String, Object>> fallbackReminders = new ArrayList<>();
        
        for (Appointment appointment : appointments) {
            AIReminderResponse fallback = generateFallbackReminder(appointment);
            Map<String, Object> reminder = new HashMap<>();
            reminder.put("success", true);
            reminder.put("message", fallback.getMessage());
            reminder.put("urgency", "UPCOMING");
            reminder.put("appointment_id", appointment.getId());
            reminder.put("source", "fallback");
            fallbackReminders.add(reminder);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("count", fallbackReminders.size());
        result.put("total_requested", appointments.size());
        result.put("reminders", fallbackReminders);
        result.put("source", "fallback_batch");
        result.put("generated_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return result;
    }

    /**
     * 📊 Génère des statistiques sur l'utilisation du service
     * 
     * @return Map avec les statistiques
     */
    public Map<String, Object> getServiceStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("service_available", isAIServiceAvailable());
        stats.put("microservice_url", aiMicroserviceUrl);
        stats.put("last_check", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        if (isAIServiceAvailable()) {
            try {
                Map<String, Object> health = getAvailableTemplates();
                stats.put("templates_count", health.getOrDefault("templates", Collections.emptyMap()).toString().length());
            } catch (Exception e) {
                stats.put("templates_count", "N/A");
            }
        }
        
        return stats;
    }
}
