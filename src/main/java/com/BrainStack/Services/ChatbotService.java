package com.BrainStack.Services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.BrainStack.Dto.ChatbotRequest;
import com.BrainStack.Dto.ChatbotResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final RestTemplate restTemplate;

    @Value("${chatbot.groq.api.key:YOUR_GROQ_API_KEY_HERE}")
    private String groqApiKey;

    @Value("${chatbot.groq.model:llama-3.3-70b-versatile}")
    private String model;

    @Value("${chatbot.groq.url:https://api.groq.com/openai/v1/chat/completions}")
    private String groqUrl;

    public ChatbotResponse getChatbotResponse(ChatbotRequest request) {
        try {
            // Prepare the request for Groq API
            Map<String, Object> groqRequest = new HashMap<>();
            groqRequest.put("model", model);
            groqRequest.put("temperature", 0.7);
            groqRequest.put("max_tokens", 500);

            // Build messages array
            Object[] messages = {
                Map.of(
                    "role", "system",
                    "content", buildSystemPrompt(request.getLanguage())
                ),
                Map.of(
                    "role", "user", 
                    "content", buildUserPrompt(request)
                )
            };
            groqRequest.put("messages", messages);

            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + groqApiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(groqRequest, headers);

            // Make the API call
            ResponseEntity<Map> response = restTemplate.postForEntity(groqUrl, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                
                // Extract the AI response
                String aiAnswer = extractAiResponse(responseBody);
                
                // Build the response
                return new ChatbotResponse(
                    request.getQuestion(),
                    aiAnswer,
                    request.getUser_id() != null ? request.getUser_id() : "anonymous",
                    model,
                    LocalDateTime.now().toString(),
                    "success",
                    "high", // Default confidence
                    categorizeQuestion(request.getQuestion()),
                    generateSuggestedActions(request.getQuestion())
                );
            } else {
                throw new RuntimeException("Failed to get response from AI service");
            }

        } catch (Exception e) {
            throw new RuntimeException("Error processing chatbot request: " + e.getMessage(), e);
        }
    }

    private String buildSystemPrompt(String language) {
        String lang = (language != null && !language.isEmpty()) ? language : "français";
        
        return String.format("""
            Tu es un assistant virtuel intelligent pour répondre aux questions fréquentes des parents d'élèves. 
            Tu dois répondre en %s et:
            
            1. Répondre de manière claire, professionnelle et empathique
            2. Fournir des informations précises sur:
               - Horaires et calendrier scolaire
               - Procédures d'inscription et de réinscription
               - Absences et retards
               - Cantine et services périscolaires
               - Activités extrascolaires
               - Communication avec les enseignants
               - Fournitures scolaires
               - Règlement intérieur
               - Santé et sécurité
               - Transport scolaire
               - TDAH, Autisme et autres besoins spéciaux
               - Ressources et recommandations éducatives
            
            3. Si tu ne connais pas la réponse exacte, suggère de contacter l'administration
            4. Reste courtois et utilise un ton rassurant
            5. Propose des ressources et recommandations pertinentes
            6. Réponds de manière concise mais complète
            
            Réponds de manière structurée avec des conseils pratiques.
            """, lang);
    }

    private String buildUserPrompt(ChatbotRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Question: ").append(request.getQuestion());
        
        if (request.getContext() != null && !request.getContext().isEmpty()) {
            prompt.append("\n\nContexte: ").append(request.getContext());
        }
        
        return prompt.toString();
    }

    @SuppressWarnings("unchecked")
    private String extractAiResponse(Map<String, Object> responseBody) {
        try {
            Object choices = responseBody.get("choices");
            if (choices instanceof java.util.List) {
                java.util.List<Object> choicesList = (java.util.List<Object>) choices;
                if (!choicesList.isEmpty()) {
                    Object firstChoice = choicesList.get(0);
                    if (firstChoice instanceof Map) {
                        Map<String, Object> choiceMap = (Map<String, Object>) firstChoice;
                        Object message = choiceMap.get("message");
                        if (message instanceof Map) {
                            Map<String, Object> messageMap = (Map<String, Object>) message;
                            Object content = messageMap.get("content");
                            if (content instanceof String) {
                                return (String) content;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Fallback if parsing fails
        }
        return "Désolé, je n'ai pas pu traiter votre question. Veuillez réessayer ou contacter l'administration.";
    }

    private String categorizeQuestion(String question) {
        String lowerQuestion = question.toLowerCase();
        
        if (lowerQuestion.contains("horaire") || lowerQuestion.contains("heure") || lowerQuestion.contains("temps")) {
            return "horaires";
        } else if (lowerQuestion.contains("inscription") || lowerQuestion.contains("inscrire") || lowerQuestion.contains("réinscription")) {
            return "inscription";
        } else if (lowerQuestion.contains("absence") || lowerQuestion.contains("retard") || lowerQuestion.contains("malade")) {
            return "absences";
        } else if (lowerQuestion.contains("cantine") || lowerQuestion.contains("repas") || lowerQuestion.contains("manger")) {
            return "cantine";
        } else if (lowerQuestion.contains("activité") || lowerQuestion.contains("sport") || lowerQuestion.contains("club")) {
            return "activites";
        } else if (lowerQuestion.contains("enseignant") || lowerQuestion.contains("professeur") || lowerQuestion.contains("communication")) {
            return "communication";
        } else if (lowerQuestion.contains("fourniture") || lowerQuestion.contains("matériel") || lowerQuestion.contains("cahier")) {
            return "fournitures";
        } else if (lowerQuestion.contains("règlement") || lowerQuestion.contains("règle") || lowerQuestion.contains("discipline")) {
            return "reglement";
        } else if (lowerQuestion.contains("santé") || lowerQuestion.contains("médical") || lowerQuestion.contains("sécurité")) {
            return "sante";
        } else if (lowerQuestion.contains("transport") || lowerQuestion.contains("bus") || lowerQuestion.contains("véhicule")) {
            return "transport";
        } else if (lowerQuestion.contains("tdah") || lowerQuestion.contains("autisme") || lowerQuestion.contains("besoin spécial")) {
            return "besoins_speciaux";
        } else {
            return "general";
        }
    }

    private String generateSuggestedActions(String question) {
        String category = categorizeQuestion(question);
        
        switch (category) {
            case "horaires":
                return "Consultez le calendrier scolaire en ligne ou contactez le secrétariat pour les horaires détaillés.";
            case "inscription":
                return "Rendez-vous au secrétariat avec les documents requis (livret de famille, justificatif de domicile, etc.).";
            case "absences":
                return "Prévenez l'école par téléphone le matin même et justifiez l'absence par écrit au retour.";
            case "cantine":
                return "Inscrivez votre enfant via le portail parents ou contactez le service de restauration.";
            case "activites":
                return "Consultez le planning des activités extrascolaires et inscrivez-vous auprès des responsables.";
            case "communication":
                return "Utilisez le carnet de correspondance ou contactez directement l'enseignant par email.";
            case "fournitures":
                return "Consultez la liste des fournitures sur le site de l'école ou demandez-la au secrétariat.";
            case "reglement":
                return "Le règlement intérieur est disponible sur le site de l'école et dans le carnet de correspondance.";
            case "sante":
                return "Contactez l'infirmière scolaire ou le médecin scolaire pour les questions de santé.";
            case "transport":
                return "Renseignez-vous auprès de la mairie ou de la société de transport scolaire.";
            case "besoins_speciaux":
                return "Contactez l'équipe pédagogique et le médecin scolaire pour un accompagnement adapté.";
            default:
                return "Pour plus d'informations, contactez le secrétariat de l'école.";
        }
    }
}
