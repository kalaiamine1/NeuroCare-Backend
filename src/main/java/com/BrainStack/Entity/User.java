package com.BrainStack.Entity;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;


import java.util.List;

@Entity
@Data
@AllArgsConstructor
@Table(name = "users") // "user" est réservé dans certaines bases
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;
    private String email;
    private String role; // PARENT, PROFESSIONNEL, MODERATEUR, etc.

    @OneToMany(mappedBy = "auteur")
    private List<Discussion> discussions;

    @OneToMany(mappedBy = "auteur")
    private List<Message> messages;

    // Constructeurs
    public User() {}

    public User(String nom, String prenom, String email, String role) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.role = role;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public List<Discussion> getDiscussions() { return discussions; }
    public void setDiscussions(List<Discussion> discussions) { this.discussions = discussions; }

    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }
}
