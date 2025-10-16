package com.BrainStack.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.BrainStack.Entity.Discussion;

public interface DiscussionRepository extends JpaRepository<Discussion, Long> {
}


