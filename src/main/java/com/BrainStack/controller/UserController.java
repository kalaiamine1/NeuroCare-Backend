package com.brainstack.controller;

import com.brainstack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}
