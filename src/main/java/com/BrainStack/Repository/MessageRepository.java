package com.BrainStack.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.BrainStack.Entity.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
}


