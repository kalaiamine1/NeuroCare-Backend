package com.BrainStack.Controller;

import com.BrainStack.Entity.User;
import com.BrainStack.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200") // Autoriser Angular local
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // ✅ Compter le nombre d’utilisateurs selon le rôle
    @GetMapping("/count")
    public long countByRole(@RequestParam String role) {
        return userRepository.countByRole_Name(role.toUpperCase());
    }

    // ✅ Supprimer un utilisateur (Médecin, Parent, etc.)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("Utilisateur introuvable");
        }

        userRepository.deleteById(id);
        return ResponseEntity.ok("Utilisateur supprimé avec succès");
    }

    // === Récupérer tous les utilisateurs ===
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

}
