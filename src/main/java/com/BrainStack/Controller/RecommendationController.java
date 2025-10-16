package com.BrainStack.Controller;

import com.BrainStack.Entity.Activity;
import com.BrainStack.Services.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin(origins = "*")
public class RecommendationController {

    @Autowired
    private ActivityService activityService;

    private final RestTemplate restTemplate = new RestTemplate();

    // URL du microservice IA (Python)
    private final String IA_SERVICE_URL = "http://localhost:5000/recommend";

    // =============================
    // Endpoint de recommandation IA
    // =============================
    @GetMapping("/recommendations/{childId}")
    public ResponseEntity<List<Activity>> getRecommendations(@PathVariable Long childId) {

        // 🔹 1. Simuler un profil enfant (données statiques)
        Map<String, Object> child = getStaticChildProfile(childId);

        // 🔹 2. Récupérer toutes les activités
        List<Activity> activities = activityService.getAllActivities();

        // 🔹 3. Construire le payload pour le microservice IA
        Map<String, Object> payload = new HashMap<>();
        payload.put("child", child);

        List<Map<String, Object>> activityList = new ArrayList<>();
        for (Activity a : activities) {
            activityList.add(Map.of(
                    "id", a.getId(),
                    "ageRange", a.getAgeRange(),
                    "category", a.getCategory(),
                    "difficulty", a.getDifficultyLevel(),
                    "pointsReward", a.getPointsReward()
            ));
        }
        payload.put("activities", activityList);

        // 🔹 4. Appel du microservice IA (Flask)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<IAResponse[]> response;
        try {
            response = restTemplate.postForEntity(IA_SERVICE_URL, request, IAResponse[].class);
        } catch (Exception e) {
            // si le microservice n'est pas dispo → renvoie une réponse factice
            return ResponseEntity.ok(getFallbackRecommendations(activities));
        }

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }

        // 🔹 5. Mapper la réponse IA → liste d’activités
        IAResponse[] recommendations = response.getBody();
        List<Long> recommendedIds = new ArrayList<>();
        for (IAResponse r : recommendations) recommendedIds.add(r.getActivityId());

        Map<Long, Activity> mapById = new HashMap<>();
        for (Activity a : activities) mapById.put(a.getId(), a);

        List<Activity> ordered = new ArrayList<>();
        for (Long id : recommendedIds) {
            if (mapById.containsKey(id)) ordered.add(mapById.get(id));
        }

        return ResponseEntity.ok(ordered);
    }

    // =============================
    // 🔸 Méthodes utilitaires
    // =============================

    // Profil enfant statique simulé
    // Profil enfant statique simulé (plusieurs profils pour test)
    private Map<String, Object> getStaticChildProfile(Long id) {
        Map<String, Object> profile = new HashMap<>();

        switch (id.intValue()) {
            case 1:
                profile.put("id", id);
                profile.put("age", 7);
                profile.put("diagnosis", "TDAH");
                profile.put("preferences", List.of("Math", "Langage", "Jeux interactifs"));
                break;

            case 2:
                profile.put("id", id);
                profile.put("age", 10);
                profile.put("diagnosis", "Autisme");
                profile.put("preferences", List.of("Science", "Jeux interactifs"));
                break;

            case 3:
                profile.put("id", id);
                profile.put("age", 5);
                profile.put("diagnosis", "Normal");
                profile.put("preferences", List.of("Coloriage", "Musique"));
                break;

            default:
                // Profil par défaut
                profile.put("id", id);
                profile.put("age", 8);
                profile.put("diagnosis", "Normal");
                profile.put("preferences", List.of("Math", "Langage"));
                break;
        }

        return profile;
    }

    // Recommandations factices (si IA non dispo)
    private List<Activity> getFallbackRecommendations(List<Activity> activities) {
        // Exemple : renvoyer les 3 premières activités pour tester
        return activities.stream().limit(3).toList();
    }

    // Classe interne pour représenter la réponse du microservice IA
    static class IAResponse {
        private Long activityId;
        private double score;

        public Long getActivityId() { return activityId; }
        public void setActivityId(Long activityId) { this.activityId = activityId; }

        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
    }
}
