package com.BrainStack.Services;

import com.BrainStack.Entity.Role;
import com.BrainStack.Entity.User;
import com.BrainStack.Repository.RoleRepository;
import com.BrainStack.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository; // ✅ ajouté pour attribuer automatiquement ROLE_USER

    private final RoleService roleService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleService roleService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // === 🧩 INSCRIPTION ADAPTÉE AU FORMULAIRE INTELLIGENT ===
    public User registerUser(User userRequest) {
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new RuntimeException("Email déjà utilisé !");
        }

        // ✅ 1. Déterminer le type d’utilisateur selon la présence d’une spécialité
        if (userRequest.getSpecialite() != null && !userRequest.getSpecialite().isEmpty()) {
            userRequest.setUserType("MEDECIN");
        } else {
            userRequest.setUserType("PARENT");
        }

        // ✅ 2. Attribuer le rôle Spring Security par défaut
        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleService.getOrCreateRole("ROLE_USER"));
        userRequest.setRole(defaultRole);

        // ✅ 3. Encodage du mot de passe
        userRequest.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        // ✅ 4. Champs par défaut
        userRequest.setEnabled(true);
        userRequest.setAvatarUrl(null);
        userRequest.setOtpCode(null);
        userRequest.setOtpExpiration(null);
        userRequest.setResetToken(null);

        // ✅ 5. Sauvegarde dans la base
        return userRepository.save(userRequest);
    }

    // === UTILISATEUR PAR EMAIL ===
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    // === CONNEXION ===
    public User loginUser(String email, String password) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            throw new RuntimeException("Utilisateur introuvable");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        return user;
    }

    // === COMPTE D'UTILISATEURS PAR RÔLE ===
    public long countUsersByRole(String roleName) {
        return userRepository.countByRole_Name(roleName.toUpperCase());
    }
}
