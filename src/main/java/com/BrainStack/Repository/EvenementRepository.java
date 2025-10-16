package com.BrainStack.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.BrainStack.Entity.Evenement;

public interface EvenementRepository extends JpaRepository<Evenement, Long> {
}


