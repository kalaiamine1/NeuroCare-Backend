package com.BrainStack.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bio;        // description ou spécialité (médecin)
    private String address;    // adresse
    private String language;   // langue préférée
    private String preferences; // ex: notifications, thème, etc.

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;         // lien avec la table "users"

    // === Constructeurs ===
    public Profile() {
    }

    public Profile(Long id, String bio, String address, String language, String preferences, User user) {
        this.id = id;
        this.bio = bio;
        this.address = address;
        this.language = language;
        this.preferences = preferences;
        this.user = user;
    }

    // === Getters & Setters ===
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getPreferences() {
        return preferences;
    }

    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
