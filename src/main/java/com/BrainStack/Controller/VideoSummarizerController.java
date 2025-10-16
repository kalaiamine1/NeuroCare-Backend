package com.BrainStack.Controller;

import com.BrainStack.Entity.Activity;
import com.BrainStack.Services.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Map;

@RestController
@RequestMapping("/api/videos")
@CrossOrigin(origins = "*")
public class VideoSummarizerController {

    @Autowired
    private ActivityService activityService;

    private final String FLASK_URL = "http://localhost:5000/summarize";

    /**
     * ✅ Méthode : Générer un résumé pour une activité existante (par son ID)
     */
    @PostMapping("/summarize/{activityId}")
    public ResponseEntity<Map<String, String>> summarizeActivityVideo(@PathVariable Long activityId) {
        try {
            // 1️⃣ Récupérer l'activité depuis la base
            Activity activity = activityService.getActivityById(activityId)
                    .orElse(null);

            if (activity == null || activity.getFileUrl() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Activité introuvable ou sans fichier vidéo."));
            }

            // 2️⃣ Construire le chemin absolu vers le fichier vidéo
            String uploadBasePath = "C:/Users/azizc/Desktop/NeuroCare-Backend/NeuroCare-Backend";
            String filePath = uploadBasePath + activity.getFileUrl().replace("/", File.separator);

            File videoFile = new File(filePath);
            if (!videoFile.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Fichier vidéo introuvable sur le serveur."));
            }

            // 3️⃣ Créer le corps multipart pour l’envoyer à Flask
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("video", new FileSystemResource(videoFile));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 4️⃣ Appeler le service Flask
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.postForEntity(FLASK_URL, requestEntity, Map.class);

            // 5️⃣ Retourner le résumé au frontend
            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur interne : " + e.getMessage()));
        }
    }

    // Ancienne méthode toujours dispo si tu veux envoyer une vidéo directement depuis Angular/Postman
    @PostMapping("/summarize")
    public ResponseEntity<Map<String, String>> summarizeVideo(@RequestParam("file") MultipartFile file) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("video", new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response;
        try {
            response = restTemplate.postForEntity(FLASK_URL, requestEntity, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Le service IA n'est pas disponible"));
        }

        return ResponseEntity.ok(response.getBody());
    }

    // Classe utilitaire
    private static class MultipartInputStreamFileResource extends InputStreamResource {
        private final String filename;

        public MultipartInputStreamFileResource(InputStream inputStream, String filename) {
            super(inputStream);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return this.filename;
        }

        @Override
        public long contentLength() {
            return -1;
        }
    }
}
