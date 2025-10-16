package com.BrainStack.Entity;

import java.time.LocalDateTime;

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
@AllArgsConstructor
public class Message {
    @Id @GeneratedValue
    private Long id;
    private String contenu;
    private LocalDateTime createdAt;
    private String moderationStatus; // "good", "bad", "pending", "error"
    private String moderationReason;
    private LocalDateTime moderatedAt;

    @ManyToOne
    private User auteur;

    @ManyToOne
    private Discussion discussion;
}