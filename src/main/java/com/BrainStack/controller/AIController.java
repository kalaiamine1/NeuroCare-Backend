package com.brainstack.controller;

import com.brainstack.entity.User;
import com.brainstack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/ai")
public class AIController {

    // 🚀 Modèle futuriste / réaliste / cartoon
    private static final String API_URL = "https://api-inference.huggingface.co/models/stabilityai/sdxl-turbo";



    @Autowired
    private UserRepository userRepository;

    // === GÉNÉRATION AVATAR ===
    @PostMapping("/generate-avatar/{userId}")
    public ResponseEntity<?> generateAvatar(@PathVariable Long userId) {
        try {
            // 🧠 Vérifie l’utilisateur
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur introuvable");
            }

            // 🧠 Prompt pour l’image IA
            String prompt = "Futuristic cyberpunk portrait of " + user.getFullName() +
                    ", ultra-realistic, neon lighting, luxury atmosphere, 3D render style";

            // ⚙️ Prépare la requête HTTP vers Hugging Face
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
       

            Map<String, Object> body = Map.of("inputs", prompt);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            // 🚀 Appel Hugging Face
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    API_URL,
                    HttpMethod.POST,
                    request,
                    byte[].class
            );

            // ⚠️ Vérifie la réponse
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                System.err.println("❌ Erreur Hugging Face : " + response.getStatusCode());
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body("Erreur API Hugging Face (" + response.getStatusCode() + ")");
            }

            // 🧩 Convertir image en Base64
            String base64Image = Base64.getEncoder().encodeToString(response.getBody());
            String avatarUrl = "data:image/png;base64," + base64Image;

            // 💾 Sauvegarder dans la base
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);

            System.out.println("✅ Avatar IA généré avec succès pour " + user.getEmail());

            return ResponseEntity.ok(Map.of(
                    "message", "Avatar IA généré avec succès 🎨",
                    "avatarUrl", avatarUrl
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Erreur serveur IA : " + e.getMessage());
        }
    }
}
