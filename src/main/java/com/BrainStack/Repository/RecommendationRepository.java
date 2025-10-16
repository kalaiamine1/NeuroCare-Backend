package com.BrainStack.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.BrainStack.Entity.Recommendation;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    
    List<Recommendation> findByCategoryAndIsActiveTrue(String category);
    
    List<Recommendation> findByThemeAndIsActiveTrue(String theme);
    
    List<Recommendation> findByIsActiveTrue();
    
    List<Recommendation> findByTypeAndIsActiveTrue(String type);
    
    @Query("SELECT r FROM Recommendation r WHERE r.isActive = true AND (r.title LIKE %:keyword% OR r.description LIKE %:keyword% OR r.tags LIKE %:keyword%)")
    List<Recommendation> searchRecommendations(@Param("keyword") String keyword);
    
    @Query("SELECT r FROM Recommendation r WHERE r.isActive = true ORDER BY r.usageCount DESC")
    List<Recommendation> findMostUsedRecommendations();
}


