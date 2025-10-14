package com.BrainStack.Repository;

import com.BrainStack.Entity.Child;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository JPA pour l'entité Child.
 */
@Repository
public interface ChildRepository extends JpaRepository<Child, Integer> {
    /**
     * Récupère tous les enfants d'un parent donné.
     * @param parentId l'identifiant du parent
     * @return liste des enfants
     */
    List<Child> findByParentId(int parentId);
}

