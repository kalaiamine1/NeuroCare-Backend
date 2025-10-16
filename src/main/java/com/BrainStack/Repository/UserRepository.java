package com.BrainStack.Repository;

import com.BrainStack.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    // 🔹 Trouver un utilisateur par email
    Optional<User> findByEmail(String email);

    // 🔹 Trouver un utilisateur via le token de réinitialisation
    Optional<User> findByResetToken(String resetToken);

    // 🔹 Vérifier si un email est déjà utilisé
    boolean existsByEmail(String email);

    // 🔹 Compter les utilisateurs selon le rôle (Spring Security)
    long countByRole_Name(String roleName);

    // ✅ NOUVELLES MÉTHODES PERSONNALISÉES POUR LE NOUVEAU SYSTÈME

    // 🔹 Trouver tous les utilisateurs selon leur type (Parent / Médecin)
    List<User> findByUserType(String userType);

    // 🔹 Trouver tous les médecins par spécialité
    List<User> findBySpecialiteContainingIgnoreCase(String specialite);

    // 🔹 Compter les utilisateurs par type (utile pour statistiques)
    long countByUserType(String userType);
}
