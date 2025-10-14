package com.BrainStack.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Child {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String fullName;
    private LocalDate birthDate;
    private String gender; // "M" ou "F"
    private String diagnosis; // Ex : "Autisme", "TDAH", etc.

    // Relation avec le parent
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private User parent;

    // Relation avec le suivi santé
    @OneToMany(mappedBy = "child", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HealthRecord> healthRecords = new ArrayList<>();
}
