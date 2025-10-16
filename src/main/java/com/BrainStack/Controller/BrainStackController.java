package com.BrainStack.Controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.BrainStack.Dto.ChatbotRequest;
import com.BrainStack.Dto.ChatbotResponse;
import com.BrainStack.Dto.ChatbotErrorResponse;
import com.BrainStack.Dto.EventRequest;
import com.BrainStack.Dto.EventResponse;
import com.BrainStack.Entity.Discussion;
import com.BrainStack.Entity.Evenement;
import com.BrainStack.Entity.Groupe;
import com.BrainStack.Entity.Message;
import com.BrainStack.Entity.Recommendation;
import com.BrainStack.Entity.User;
import com.BrainStack.Services.BrainStackService;
import com.BrainStack.Services.EventService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BrainStackController {

    private final BrainStackService service;
    private final EventService eventService;

    @GetMapping("/evenements")
    public List<Evenement> listEvenements() { return service.listEvenements(); }

    @GetMapping("/evenements/{id}")
    public ResponseEntity<Evenement> getEvenement(@PathVariable Long id) {
        Optional<Evenement> e = service.getEvenement(id);
        return e.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/evenements")
    public Evenement createEvenement(@RequestBody Evenement e) { return service.saveEvenement(e); }

    // New endpoint for n8n workflow integration
    @PostMapping("/create-event")
    public ResponseEntity<?> createEventWithModeration(@RequestBody EventRequest eventRequest) {
        try {
            // Validate required fields
            if (eventRequest.getTitle() == null || eventRequest.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new EventErrorResponse("INVALID_REQUEST", "Title is required", 400));
            }
            if (eventRequest.getDescription() == null || eventRequest.getDescription().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new EventErrorResponse("INVALID_REQUEST", "Description is required", 400));
            }

            // Process event through n8n workflow
            EventResponse eventResponse = eventService.createEventWithModeration(eventRequest);
            
            // Return appropriate response based on status
            if ("rejected".equals(eventResponse.getStatus())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(eventResponse);
            } else if ("error".equals(eventResponse.getStatus())) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(eventResponse);
            } else {
                return ResponseEntity.status(HttpStatus.CREATED).body(eventResponse);
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new EventErrorResponse("PROCESSING_ERROR", 
                    "Error processing event creation: " + e.getMessage(), 500));
        }
    }

    @GetMapping("/events/{id}/status")
    public ResponseEntity<?> getEventStatus(@PathVariable Long id) {
        EventResponse eventResponse = eventService.getEventStatus(id);
        if (eventResponse != null) {
            return ResponseEntity.ok(eventResponse);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/evenements/{id}")
    public ResponseEntity<Void> deleteEvenement(@PathVariable Long id) { service.deleteEvenement(id); return ResponseEntity.noContent().build(); }

    @GetMapping("/groupes")
    public List<Groupe> listGroupes() { return service.listGroupes(); }

    @GetMapping("/groupes/{id}")
    public ResponseEntity<Groupe> getGroupe(@PathVariable Long id) {
        return service.getGroupe(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/groupes")
    public Groupe createGroupe(@RequestBody Groupe g) { return service.saveGroupe(g); }

    @DeleteMapping("/groupes/{id}")
    public ResponseEntity<Void> deleteGroupe(@PathVariable Long id) { service.deleteGroupe(id); return ResponseEntity.noContent().build(); }

    @GetMapping("/messages")
    public List<Message> listMessages() { return service.listMessages(); }

    @GetMapping("/messages/{id}")
    public ResponseEntity<Message> getMessage(@PathVariable Long id) {
        return service.getMessage(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/messages")
    public ResponseEntity<?> createMessage(@RequestBody Message m) { 
        Message savedMessage = service.saveMessage(m);
        
        // Check if message was flagged as inappropriate
        if ("bad".equals(savedMessage.getModerationStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ModerationErrorResponse(
                    "Bad message - Content flagged as inappropriate", 
                    savedMessage.getModerationReason(),
                    savedMessage.getModerationStatus()
                ));
        }
        
        // Return the message with moderation details in n8n format
        return ResponseEntity.ok(new MessageModerationResponse(
            savedMessage.getContenu(),
            savedMessage.getModerationStatus(),
            savedMessage.getModerationReason(),
            savedMessage.getModeratedAt()
        )); 
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) { service.deleteMessage(id); return ResponseEntity.noContent().build(); }

    @GetMapping("/discussions")
    public List<Discussion> listDiscussions() { return service.listDiscussions(); }

    @GetMapping("/discussions/{id}")
    public ResponseEntity<Discussion> getDiscussion(@PathVariable Long id) {
        return service.getDiscussion(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/discussions")
    public Discussion createDiscussion(@RequestBody Discussion d) { return service.saveDiscussion(d); }

    @DeleteMapping("/discussions/{id}")
    public ResponseEntity<Void> deleteDiscussion(@PathVariable Long id) { service.deleteDiscussion(id); return ResponseEntity.noContent().build(); }

    @GetMapping("/recommendations")
    public List<Recommendation> listRecommendations() { return service.listRecommendations(); }

    @GetMapping("/recommendations/{id}")
    public ResponseEntity<Recommendation> getRecommendation(@PathVariable Long id) {
        return service.getRecommendation(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/recommendations")
    public Recommendation createRecommendation(@RequestBody Recommendation r) { return service.saveRecommendation(r); }

    @DeleteMapping("/recommendations/{id}")
    public ResponseEntity<Void> deleteRecommendation(@PathVariable Long id) { service.deleteRecommendation(id); return ResponseEntity.noContent().build(); }

    // Enhanced recommendation endpoints
    @GetMapping("/recommendations/category/{category}")
    public List<Recommendation> getRecommendationsByCategory(@PathVariable String category) {
        return service.getRecommendationsByCategory(category);
    }

    @GetMapping("/recommendations/theme/{theme}")
    public List<Recommendation> getRecommendationsByTheme(@PathVariable String theme) {
        return service.getRecommendationsByTheme(theme);
    }

    @GetMapping("/recommendations/active")
    public List<Recommendation> getActiveRecommendations() {
        return service.getActiveRecommendations();
    }

    @GetMapping("/recommendations/search")
    public List<Recommendation> searchRecommendations(@RequestParam String keyword) {
        return service.searchRecommendations(keyword);
    }

    @GetMapping("/recommendations/popular")
    public List<Recommendation> getMostUsedRecommendations() {
        return service.getMostUsedRecommendations();
    }

    // User endpoints
    @GetMapping("/users")
    public List<User> listUsers() { return service.listUsers(); }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return service.getUser(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/users")
    public User createUser(@RequestBody User u) { return service.saveUser(u); }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) { service.deleteUser(id); return ResponseEntity.noContent().build(); }

    // Chatbot endpoints
    @PostMapping("/chatbot/ask")
    public ResponseEntity<?> askChatbot(@RequestBody ChatbotRequest request) {
        try {
            // Validate request
            if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ChatbotErrorResponse(
                        "INVALID_REQUEST", 
                        "Question is required", 
                        java.time.LocalDateTime.now().toString(),
                        400
                    ));
            }

            // Set default values
            if (request.getUser_id() == null) {
                request.setUser_id("anonymous");
            }
            if (request.getLanguage() == null) {
                request.setLanguage("français");
            }

            // Get chatbot response
            ChatbotResponse response = service.getChatbotResponse(request);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ChatbotErrorResponse(
                    "CHATBOT_ERROR", 
                    "Error processing chatbot request: " + e.getMessage(), 
                    java.time.LocalDateTime.now().toString(),
                    500
                ));
        }
    }

    @PostMapping("/parent-chatbot")
    public ResponseEntity<?> parentChatbot(@RequestBody ChatbotRequest request) {
        // This endpoint matches the n8n webhook format
        return askChatbot(request);
    }

    // Moderation endpoints
    @GetMapping("/messages/{id}/moderation")
    public ResponseEntity<?> getMessageModerationStatus(@PathVariable Long id) {
        Optional<Message> message = service.getMessage(id);
        if (message.isPresent()) {
            return ResponseEntity.ok(new ModerationStatusResponse(
                message.get().getModerationStatus(),
                message.get().getModerationReason(),
                message.get().getModeratedAt()
            ));
        }
        return ResponseEntity.notFound().build();
    }

    // Helper classes for responses
    public static class ModerationErrorResponse {
        private String message;
        private String reason;
        private String status;

        public ModerationErrorResponse(String message, String reason, String status) {
            this.message = message;
            this.reason = reason;
            this.status = status;
        }

        // Getters
        public String getMessage() { return message; }
        public String getReason() { return reason; }
        public String getStatus() { return status; }
    }

    public static class ModerationStatusResponse {
        private String status;
        private String reason;
        private java.time.LocalDateTime moderatedAt;

        public ModerationStatusResponse(String status, String reason, java.time.LocalDateTime moderatedAt) {
            this.status = status;
            this.reason = reason;
            this.moderatedAt = moderatedAt;
        }

        // Getters
        public String getStatus() { return status; }
        public String getReason() { return reason; }
        public java.time.LocalDateTime getModeratedAt() { return moderatedAt; }
    }

    public static class MessageModerationResponse {
        private String content;
        private String status;
        private String message;
        private String ai_reason;
        private String model;
        private String timestamp;

        public MessageModerationResponse(String content, String status, String ai_reason, java.time.LocalDateTime moderatedAt) {
            this.content = content;
            this.status = status;
            this.message = "good".equals(status) ? "Good message - Content approved" : "Bad message - Content flagged as inappropriate";
            this.ai_reason = ai_reason;
            this.model = "llama-3.1-8b-instant";
            this.timestamp = moderatedAt != null ? moderatedAt.toString() : java.time.Instant.now().toString();
        }

        // Getters
        public String getContent() { return content; }
        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public String getAi_reason() { return ai_reason; }
        public String getModel() { return model; }
        public String getTimestamp() { return timestamp; }
    }

    public static class EventErrorResponse {
        private String error;
        private String message;
        private int status;

        public EventErrorResponse(String error, String message, int status) {
            this.error = error;
            this.message = message;
            this.status = status;
        }

        // Getters
        public String getError() { return error; }
        public String getMessage() { return message; }
        public int getStatus() { return status; }
    }
}


