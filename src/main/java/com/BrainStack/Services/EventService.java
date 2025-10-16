package com.BrainStack.Services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.BrainStack.Dto.EventRequest;
import com.BrainStack.Dto.EventResponse;
import com.BrainStack.Entity.Evenement;
import com.BrainStack.Entity.Groupe;
import com.BrainStack.Entity.User;
import com.BrainStack.Repository.EvenementRepository;
import com.BrainStack.Repository.GroupeRepository;
import com.BrainStack.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EvenementRepository evenementRepository;
    private final GroupeRepository groupeRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Value("${event.n8n.webhook.url}")
    private String n8nWebhookUrl;

    @Value("${event.n8n.auth.token}")
    private String n8nAuthToken;

    @Value("${event.fallback.enabled:true}")
    private boolean fallbackEnabled;

    public EventResponse createEventWithModeration(EventRequest eventRequest) {
        try {
            // Prepare headers for n8n webhook
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + n8nAuthToken);

            // Create HTTP entity
            HttpEntity<EventRequest> requestEntity = new HttpEntity<>(eventRequest, headers);

            // Call n8n webhook
            ResponseEntity<EventResponse> response = restTemplate.exchange(
                n8nWebhookUrl,
                HttpMethod.POST,
                requestEntity,
                EventResponse.class
            );

            EventResponse eventResponse = response.getBody();
            
            if (eventResponse != null) {
                // Save event to database if approved
                if ("approved".equals(eventResponse.getStatus())) {
                    saveEventFromResponse(eventRequest, eventResponse);
                }
            }

            return eventResponse;

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // Handle specific HTTP errors
            EventResponse errorResponse = new EventResponse();
            errorResponse.setStatus("error");
            
            if (e.getStatusCode().value() == 404) {
                errorResponse.setModeration_reason("n8n webhook not found. Check workflow status.");
            } else if (e.getStatusCode().value() == 401) {
                errorResponse.setModeration_reason("Unauthorized access to n8n webhook.");
            } else {
                String errorMsg = e.getMessage();
                if (errorMsg != null && errorMsg.length() > 900) {
                    errorMsg = errorMsg.substring(0, 897) + "...";
                }
                errorResponse.setModeration_reason("HTTP error " + e.getStatusCode().value() + ": " + errorMsg);
            }
            errorResponse.setCreated_at(LocalDateTime.now().toString());
            return errorResponse;
            
        } catch (org.springframework.web.client.ResourceAccessException e) {
            // Handle network/connection errors
            if (fallbackEnabled) {
                return createEventWithFallback(eventRequest, "n8n webhook unavailable - using fallback mode");
            } else {
                EventResponse errorResponse = new EventResponse();
                errorResponse.setStatus("error");
                errorResponse.setModeration_reason("Cannot connect to n8n webhook.");
                errorResponse.setCreated_at(LocalDateTime.now().toString());
                return errorResponse;
            }
            
        } catch (Exception e) {
            // Handle other failures
            if (fallbackEnabled) {
                String errorMsg = e.getMessage();
                if (errorMsg != null && errorMsg.length() > 800) {
                    errorMsg = errorMsg.substring(0, 797) + "...";
                }
                return createEventWithFallback(eventRequest, "n8n processing failed - using fallback mode: " + errorMsg);
            } else {
                EventResponse errorResponse = new EventResponse();
                errorResponse.setStatus("error");
                String errorMsg = e.getMessage();
                if (errorMsg != null && errorMsg.length() > 900) {
                    errorMsg = errorMsg.substring(0, 897) + "...";
                }
                errorResponse.setModeration_reason("Failed to process event: " + errorMsg);
                errorResponse.setCreated_at(LocalDateTime.now().toString());
                return errorResponse;
            }
        }
    }

    private void saveEventFromResponse(EventRequest eventRequest, EventResponse eventResponse) {
        Evenement evenement = new Evenement();
        
        // Set basic information
        evenement.setTitre(eventResponse.getTitle());
        evenement.setDescription(eventResponse.getDescription());
        evenement.setOriginalTitle(eventResponse.getOriginal_title());
        evenement.setOriginalDescription(eventResponse.getOriginal_description());
        
        // Parse date_time if provided
        if (eventResponse.getDate_time() != null) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(eventResponse.getDate_time());
                evenement.setDateHeure(dateTime);
            } catch (Exception e) {
                // If parsing fails, use current time
                evenement.setDateHeure(LocalDateTime.now());
            }
        } else {
            evenement.setDateHeure(LocalDateTime.now());
        }
        
        // Set additional fields
        evenement.setLocation(eventResponse.getLocation());
        evenement.setTheme(eventResponse.getTheme());
        evenement.setIsOnline(eventResponse.getIs_online());
        evenement.setMaxParticipants(eventResponse.getMax_participants());
        evenement.setStatus(eventResponse.getStatus());
        evenement.setModerationReason(eventResponse.getModeration_reason());
        evenement.setModeratedAt(LocalDateTime.now());
        evenement.setCreatedAt(LocalDateTime.now());
        evenement.setTranslated(eventResponse.getTranslated());
        evenement.setTargetLanguage(eventResponse.getTarget_language());
        
        // Set group if provided
        if (eventResponse.getGroup_id() != null) {
            groupeRepository.findById(eventResponse.getGroup_id())
                .ifPresent(evenement::setGroupe);
        }
        
        // Set organizer if provided
        if (eventResponse.getOrganizer_id() != null) {
            userRepository.findById(eventResponse.getOrganizer_id())
                .ifPresent(evenement::setOrganizer);
        }
        
        evenementRepository.save(evenement);
    }

    private EventResponse createEventWithFallback(EventRequest eventRequest, String reason) {
        try {
            // Create event directly in database without n8n processing
            Evenement evenement = new Evenement();
            
            // Set basic information
            evenement.setTitre(eventRequest.getTitle());
            evenement.setDescription(eventRequest.getDescription());
            evenement.setOriginalTitle(eventRequest.getTitle());
            evenement.setOriginalDescription(eventRequest.getDescription());
            
            // Parse date_time if provided
            if (eventRequest.getDate_time() != null) {
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(eventRequest.getDate_time());
                    evenement.setDateHeure(dateTime);
                } catch (Exception e) {
                    evenement.setDateHeure(LocalDateTime.now());
                }
            } else {
                evenement.setDateHeure(LocalDateTime.now());
            }
            
            // Set additional fields
            evenement.setLocation(eventRequest.getLocation());
            evenement.setTheme(eventRequest.getTheme());
            evenement.setIsOnline(eventRequest.getIs_online());
            evenement.setMaxParticipants(eventRequest.getMax_participants());
            evenement.setStatus("approved"); // Auto-approve in fallback mode
            // Truncate moderation reason to fit database column
            String truncatedReason = reason.length() > 1000 ? reason.substring(0, 997) + "..." : reason;
            evenement.setModerationReason(truncatedReason);
            evenement.setModeratedAt(LocalDateTime.now());
            evenement.setCreatedAt(LocalDateTime.now());
            evenement.setTranslated(false);
            evenement.setTargetLanguage(eventRequest.getTarget_language());
            
            // Set group if provided
            if (eventRequest.getGroup_id() != null) {
                groupeRepository.findById(eventRequest.getGroup_id())
                    .ifPresent(evenement::setGroupe);
            }
            
            // Set organizer if provided
            if (eventRequest.getOrganizer_id() != null) {
                userRepository.findById(eventRequest.getOrganizer_id())
                    .ifPresent(evenement::setOrganizer);
            }
            
            // Save to database
            Evenement savedEvent = evenementRepository.save(evenement);
            
            // Create response
            EventResponse response = new EventResponse();
            response.setTitle(savedEvent.getTitre());
            response.setDescription(savedEvent.getDescription());
            response.setOriginal_title(savedEvent.getOriginalTitle());
            response.setOriginal_description(savedEvent.getOriginalDescription());
            response.setDate_time(savedEvent.getDateHeure().toString());
            response.setLocation(savedEvent.getLocation());
            response.setGroup_id(eventRequest.getGroup_id());
            response.setOrganizer_id(eventRequest.getOrganizer_id());
            response.setTheme(savedEvent.getTheme());
            response.setIs_online(savedEvent.getIsOnline());
            response.setMax_participants(savedEvent.getMaxParticipants());
            response.setStatus("approved");
            response.setTranslated(false);
            response.setTarget_language(savedEvent.getTargetLanguage());
            response.setCreated_at(savedEvent.getCreatedAt().toString());
            response.setModeration_reason(reason);
            response.setModeration_status("approved");
            
            return response;
            
        } catch (Exception e) {
            EventResponse errorResponse = new EventResponse();
            errorResponse.setStatus("error");
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.length() > 1000) {
                errorMsg = errorMsg.substring(0, 997) + "...";
            }
            errorResponse.setModeration_reason("Fallback mode failed: " + errorMsg);
            errorResponse.setCreated_at(LocalDateTime.now().toString());
            return errorResponse;
        }
    }

    public EventResponse getEventStatus(Long eventId) {
        return evenementRepository.findById(eventId)
            .map(event -> {
                EventResponse response = new EventResponse();
                response.setTitle(event.getTitre());
                response.setDescription(event.getDescription());
                response.setStatus(event.getStatus());
                response.setModeration_reason(event.getModerationReason());
                response.setModeration_status(event.getStatus());
                response.setCreated_at(event.getCreatedAt() != null ? event.getCreatedAt().toString() : null);
                return response;
            })
            .orElse(null);
    }
}
