package com.BrainStack.Entity;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Discussion {
    @Id @GeneratedValue
    private Long id;
    private String titre;   
    private String theme; // TDAH, Autisme, etc.
    private LocalDateTime createdAt;

    @ManyToOne
    private User auteur;
    @JsonIgnore
    @OneToMany(mappedBy = "discussion")
    private List<Message> messages;
}