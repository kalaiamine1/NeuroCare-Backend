package com.BrainStack.Repository;

import com.BrainStack.Entity.HealthRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository JPA pour l'entité HealthRecord.
 */
@Repository
public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {
    /**
     * Récupère tous les enregistrements de santé d'un enfant donné.
     * @param childId l'identifiant de l'enfant
     * @return liste des enregistrements
     */
    List<HealthRecord> findByChildId(int childId);

    /**
     * Récupère les enregistrements d'un enfant dans une plage de dates inclusive.
     */
    List<HealthRecord> findByChildIdAndDateBetween(int childId, LocalDate start, LocalDate end);
}
