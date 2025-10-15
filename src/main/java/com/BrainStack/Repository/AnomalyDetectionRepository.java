package com.BrainStack.Repository;

import com.BrainStack.Entity.AnomalyDetection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnomalyDetectionRepository extends JpaRepository<AnomalyDetection, Long> {
    List<AnomalyDetection> findByChildId(int childId);
    List<AnomalyDetection> findByChildIdAndResolvedFalse(int childId);
}
