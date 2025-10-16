package com.BrainStack.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 IMPORTANT : ce champ est utilisé par Spring Security
    // Les valeurs typiques sont : ROLE_USER, ROLE_ADMIN
    // et NON pas "PARENT" ou "MEDECIN" (ça c’est dans userType)
    @Column(unique = true, nullable = false)
    private String name;

    // === CONSTRUCTEURS ===
    public Role() {}

    public Role(String name) {
        this.name = name;
    }

    public Role(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // === GETTERS & SETTERS ===
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
