package com.brainstack.controller;

import com.brainstack.entity.User;
import com.brainstack.repository.UserRepository;
import com.brainstack.security.JwtUtil;
import com.brainstack.service.EmailService;
import com.brainstack.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "http://localhost:4200")

@RequestMapping("/api/auth")


public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // ✅ Constructeur complet
    public AuthController(UserService userService,
                          JwtUtil jwtUtil,
                          UserRepository userRepository,
                          EmailService emailService,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    // === INSCRIPTION ===
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User userRequest) {
        System.out.println("📩 [DEBUG] Requête reçue dans /api/auth/register : " + userRequest.getEmail());
        try {
            // ✅ Maintenant on envoie aussi le téléphone au service
            User user = userService.registerUser(
                    userRequest.getFullName(),
                    userRequest.getEmail(),
                    userRequest.getPassword(),
                    userRequest.getRole().getName(),
                    userRequest.getPhone() // ➕ téléphone ajouté
            );

            System.out.println("✅ [DEBUG] Utilisateur enregistré : " + user.getEmail());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            System.err.println("❌ [DEBUG] Erreur dans /register : " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



    // === CONNEXION ===
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        try {
            User user = userService.loginUser(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
            );

            String token = jwtUtil.generateToken(user.getEmail());

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "user", user
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // === RÉCUPÉRER L’UTILISATEUR CONNECTÉ ===
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7);
            String email = jwtUtil.extractUsername(jwt);

            User user = userService.findByEmail(email);
            if (user == null) return ResponseEntity.badRequest().body("Utilisateur introuvable");

            return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "fullName", user.getFullName(),
                    "email", user.getEmail(),
                    "role", user.getRole().getName(),
                    "avatarUrl", user.getAvatarUrl()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Token invalide ou expiré");
        }
    }

    // === MOT DE PASSE OUBLIÉ ===
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        User user = userService.findByEmail(email);
        if (user == null) return ResponseEntity.badRequest().body("Utilisateur introuvable");

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        userRepository.save(user);

        String resetLink = "http://localhost:4200/reset-password?token=" + token;
        String subject = "🔐 Réinitialisation du mot de passe NeuroCare";
        String body = "Bonjour " + user.getFullName() + ",\n\n"
                + "Cliquez sur le lien pour réinitialiser votre mot de passe :\n"
                + resetLink + "\n\n"
                + "Ce lien expirera dans quelques minutes.\n\n"
                + "Équipe NeuroCare.";
        emailService.sendEmail(email, subject, body);

        return ResponseEntity.ok("Email envoyé à : " + email);
    }

    // === RÉINITIALISER MOT DE PASSE ===
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        User user = userRepository.findByResetToken(token).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body("Lien invalide");

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        userRepository.save(user);

        return ResponseEntity.ok("Mot de passe réinitialisé avec succès !");
    }

    // === 2FA POUR MÉDECINS ===
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        User user = userService.findByEmail(email);
        if (user == null) return ResponseEntity.badRequest().body("Utilisateur introuvable");
        if (user.getOtpCode() == null) return ResponseEntity.badRequest().body("Aucun code généré");
        if (LocalDateTime.now().isAfter(user.getOtpExpiration()))
            return ResponseEntity.badRequest().body("Code expiré");

        if (!user.getOtpCode().equals(otp))
            return ResponseEntity.badRequest().body("Code invalide");

        String token = jwtUtil.generateToken(user.getEmail());
        user.setOtpCode(null);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "role", user.getRole().getName(),
                "email", user.getEmail()
        ));
    }
}
