package com.brainstack.repository;

import com.brainstack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByResetToken(String resetToken);

    boolean existsByEmail(String email);

    // ✅ NOUVELLE MÉTHODE POUR COMPTER LES UTILISATEURS SELON LE NOM DU RÔLE
    long countByRole_Name(String roleName);
}
