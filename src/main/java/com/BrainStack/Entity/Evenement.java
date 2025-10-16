package com.BrainStack.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Evenement {
    @Id @GeneratedValue
    private Long id;
    
    @Column(length = 500)
    private String titre;
    
    @Column(length = 2000)
    private String description;
    
    private LocalDateTime dateHeure;
    
    @Column(length = 500)
    private String location;
    
    @Column(length = 200)
    private String theme;
    
    private Boolean isOnline = false;
    private Integer maxParticipants;
    
    @Column(length = 50)
    private String status = "pending"; // pending, approved, rejected
    
    @Column(length = 1000)
    private String moderationReason;
    
    private LocalDateTime moderatedAt;
    private LocalDateTime createdAt;
    private Boolean translated = false;
    
    @Column(length = 50)
    private String targetLanguage;
    
    @Column(length = 500)
    private String originalTitle;
    
    @Column(length = 2000)
    private String originalDescription;
    
    @ManyToOne
    private Groupe groupe;
    
    @ManyToOne
    private User organizer;
}