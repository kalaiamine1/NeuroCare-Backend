package com.BrainStack.Controller;

import com.BrainStack.Entity.Activity;
import com.BrainStack.Services.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin(origins = "*") // Pour autoriser Angular à accéder à l'API
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Activity> createActivity(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("type") String type,
            @RequestParam("ageRange") String ageRange,
            @RequestParam("difficultyLevel") String difficultyLevel,
            @RequestParam("category") String category,
            @RequestParam("pointsReward") int pointsReward,
            @RequestParam("aiRecommended") boolean aiRecommended,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            // === 1️⃣ Sauvegarder le fichier localement ===
            // Utiliser un chemin absolu
            String uploadDir = "C:/Users/azizc/Desktop/NeuroCare-Backend/NeuroCare-Backend/uploads/activities/";
// ou, si tu veux un chemin fixe :
            // String uploadDir = "C:/uploads/activities/";

            java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            java.nio.file.Path filePath = uploadPath.resolve(fileName);
            file.transferTo(filePath.toFile());

            // === 2️⃣ Créer l’objet Activity ===
            Activity activity = new Activity();
            activity.setTitle(title);
            activity.setDescription(description);
            activity.setType(type);
            activity.setAgeRange(ageRange);
            activity.setDifficultyLevel(difficultyLevel);
            activity.setCategory(category);
            activity.setPointsReward(pointsReward);
            activity.setAiRecommended(aiRecommended);
            activity.setFileUrl("/uploads/activities/" + fileName);

            Activity created = activityService.createActivity(activity);
            return ResponseEntity.ok(created);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping
    public ResponseEntity<List<Activity>> getAllActivities() {
        return ResponseEntity.ok(activityService.getAllActivities());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Activity> getActivityById(@PathVariable Long id) {
        return activityService.getActivityById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Activity> updateActivity(@PathVariable Long id, @RequestBody Activity activity) {
        Activity updated = activityService.updateActivity(id, activity);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActivity(@PathVariable Long id) {
        activityService.deleteActivity(id);
        return ResponseEntity.noContent().build();
    }
}
