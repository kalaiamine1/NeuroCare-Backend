package com.BrainStack.Repository;

import com.BrainStack.Entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    // Tu peux ajouter des méthodes personnalisées plus tard, ex :
    // List<Activity> findByCategory(String category);
}
