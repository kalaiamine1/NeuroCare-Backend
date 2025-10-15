package com.BrainStack.Repository;

import com.BrainStack.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository JPA pour l'entité User (Parent).
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
}
