package com.BrainStack.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.BrainStack.Dto.ModerationRequest;
import com.BrainStack.Dto.ModerationResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ModerationService {

    @Value("${moderation.n8n.webhook.url}")
    private String webhookUrl;

    @Value("${moderation.n8n.auth.token}")
    private String authToken;

    private final RestTemplate restTemplate;

    public ModerationService() {
        this.restTemplate = new RestTemplate();
    }

    public ModerationResponse checkContent(String content) {
        try {
            log.info("Checking content for moderation: {}", content);
            log.info("Webhook URL: {}", webhookUrl);

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);

            // Prepare request body
            ModerationRequest request = new ModerationRequest(content);
            HttpEntity<ModerationRequest> entity = new HttpEntity<>(request, headers);

            log.info("Sending request to n8n webhook with headers: {}", headers);

            // Make the request to n8n webhook
            ResponseEntity<ModerationResponse> response = restTemplate.postForEntity(
                webhookUrl, 
                entity, 
                ModerationResponse.class
            );

            log.info("Received response status: {}", response.getStatusCode());
            log.info("Received response body: {}", response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Moderation check completed. Status: {}", response.getBody().getStatus());
                return response.getBody();
            } else {
                log.error("Moderation check failed with status: {}", response.getStatusCode());
                return createErrorResponse(content, "Moderation service returned non-2xx status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Error during content moderation: {}", e.getMessage(), e);
            log.error("Exception type: {}", e.getClass().getSimpleName());
            if (e.getCause() != null) {
                log.error("Caused by: {}", e.getCause().getMessage());
            }
            return createErrorResponse(content, "Moderation service error: " + e.getMessage());
        }
    }

    private ModerationResponse createErrorResponse(String content, String errorMessage) {
        return new ModerationResponse(
            content,
            "error",
            errorMessage,
            "Service unavailable",
            "error",
            java.time.Instant.now().toString()
        );
    }

    public boolean isContentAppropriate(String content) {
        ModerationResponse response = checkContent(content);
        return "good".equals(response.getStatus());
    }
}
