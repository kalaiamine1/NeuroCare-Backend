package com.brainstack.service;

import com.brainstack.entity.Role;
import com.brainstack.entity.User;
import com.brainstack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;


    private final RoleService roleService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleService roleService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // === INSCRIPTION ===
    public User registerUser(String fullName, String email, String password, String roleName, String phone) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email déjà utilisé !");
        }

        Role role = roleService.getOrCreateRole(roleName);

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setEnabled(true);

        // ✅ Ajout du téléphone
        user.setPhone(phone);

        // Champs optionnels (null ou valeurs par défaut)
        user.setAvatarUrl(null);
        user.setOtpCode(null);
        user.setOtpExpiration(null);
        user.setResetToken(null);

        return userRepository.save(user);
    }



    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
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

    public long countUsersByRole(String roleName) {
        return userRepository.countByRole_Name(roleName.toUpperCase());
    }


}
