package com.brainstack.entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    // === IDENTIFIANT ===
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // === INFORMATIONS DE BASE ===
    private String fullName;
    private String email;
    private String password;
    private String phone;
    private boolean enabled = true;
    private String avatarUrl;

    // === 🔹 NOUVEAUX CHAMPS POUR LE FORMULAIRE INTELLIGENT ===
    // Champ IA : Spécialité médicale (uniquement pour les médecins)
    private String specialite;

    // Champ IA : Emplacement du cabinet (pour les médecins)
    private String workplace;

    // Champ IA : Type d'utilisateur (PARENT ou MEDECIN)
    private String userType;
    // 💡 Ce champ est utile pour enregistrer automatiquement dans la base
    // si c’est un parent ou un médecin (plus simple que de le déduire du rôle).
    // Exemples :
    // - userType = "PARENT"
    // - userType = "MEDECIN"

    // === SÉCURITÉ ET AUTHENTIFICATION ===
    private String resetToken;          // pour mot de passe oublié
    private String otpCode;             // pour 2FA
    private LocalDateTime otpExpiration; // date d’expiration du code OTP

    // === RÔLE (Spring Security) ===
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    // === CONSTRUCTEURS ===
    public User() {}

    public User(String fullName, String email, String password, Role role) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.enabled = true;
    }

    // === GETTERS & SETTERS ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    // === 🔹 GETTERS & SETTERS DES NOUVEAUX CHAMPS ===
    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    public String getWorkplace() { return workplace; }
    public void setWorkplace(String workplace) { this.workplace = workplace; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    // === SÉCURITÉ ===
    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }

    public LocalDateTime getOtpExpiration() { return otpExpiration; }
    public void setOtpExpiration(LocalDateTime otpExpiration) { this.otpExpiration = otpExpiration; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    // === IMPLÉMENTATION SPRING SECURITY ===
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.getName()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return enabled; }
}
