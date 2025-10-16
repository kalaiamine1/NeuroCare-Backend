package com.BrainStack.Controller;

import com.BrainStack.Entity.User;
import com.BrainStack.Repository.UserRepository;
import com.BrainStack.Services.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

@RestController
@RequestMapping("/api/password")
@CrossOrigin(origins = "http://localhost:4200")
public class PasswordResetController {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetController(UserRepository userRepository, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    // === ÉTAPE 1 : Demande de réinitialisation ===
    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("Utilisateur non trouvé");
        }

        // Générer un code temporaire
        String code = String.valueOf(new Random().nextInt(900000) + 100000);

        // Enregistrer le code temporairement (dans la colonne password pour simplifier)
        user.setPassword(passwordEncoder.encode(code));
        userRepository.save(user);

        // Envoyer le code par email
        String subject = "Réinitialisation de votre mot de passe NeuroCare";
        String body = "Bonjour " + user.getFullName() + ",\n\n"
                + "Voici votre code de réinitialisation : " + code + "\n\n"
                + "Veuillez le saisir dans l’application pour changer votre mot de passe.\n\n"
                + "Go Brand Agency - NeuroCare Team";
        emailService.sendEmail(email, subject, body);

        return ResponseEntity.ok("Code envoyé à l'adresse email : " + email);
    }

    // === ÉTAPE 2 : Réinitialisation ===
    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestParam String email,
                                           @RequestParam String newPassword) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("Utilisateur non trouvé");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok("Mot de passe modifié avec succès !");
    }
}
