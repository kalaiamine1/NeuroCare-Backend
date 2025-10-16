package com.BrainStack.Controller;

import com.BrainStack.Entity.Profile;
import com.BrainStack.Entity.User;
import com.BrainStack.Repository.ProfileRepository;
import com.BrainStack.Repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:4200")
public class ProfileController {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public ProfileController(ProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    // === RÉCUPÉRER PROFIL ===
    @GetMapping("/{id}")
    public ResponseEntity<?> getProfile(@PathVariable Long id) {
        Profile profile = profileRepository.findById(id).orElse(null);
        if (profile == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Profil introuvable");
        }
        return ResponseEntity.ok(profile);
    }

    // === MODIFIER PROFIL ===
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @RequestBody Profile updatedProfile) {
        Profile profile = profileRepository.findById(id).orElse(null);
        if (profile == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Profil introuvable");
        }

        profile.setBio(updatedProfile.getBio());
        profile.setAddress(updatedProfile.getAddress());
        profile.setLanguage(updatedProfile.getLanguage());
        profile.setPreferences(updatedProfile.getPreferences());
        profileRepository.save(profile);

        return ResponseEntity.ok(profile);
    }

    // === UPLOAD PHOTO ===
    @PostMapping("/upload-photo/{userId}")
    public ResponseEntity<?> uploadPhoto(@PathVariable int userId, @RequestParam("file") MultipartFile file) throws IOException {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur introuvable");
        }

        user.setAvatarUrl("uploaded-" + file.getOriginalFilename());
        userRepository.save(user);

        return ResponseEntity.ok("Photo enregistrée avec succès");
    }
}
