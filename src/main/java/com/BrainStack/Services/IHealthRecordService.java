package com.BrainStack.Services;

import com.BrainStack.Dto.HealthRecordRequestDTO;
import com.BrainStack.Dto.HealthRecordResponseDTO;

import java.util.List;

/**
 * Interface du service pour la gestion des enregistrements de santé (HealthRecord).
 */
public interface IHealthRecordService {
    /**
     * Ajoute un enregistrement de santé à un enfant.
     */
    HealthRecordResponseDTO addHealthRecord(int childId, HealthRecordRequestDTO dto);

    /**
     * Modifie un enregistrement de santé existant.
     */
    HealthRecordResponseDTO updateHealthRecord(Long healthRecordId, HealthRecordRequestDTO dto);

    /**
     * Supprime un enregistrement de santé.
     */
    void deleteHealthRecord(Long healthRecordId);

    /**
     * Récupère un enregistrement de santé par son id.
     */
    HealthRecordResponseDTO getHealthRecordById(Long healthRecordId);

    /**
     * Récupère tous les enregistrements de santé d'un enfant.
     */
    List<HealthRecordResponseDTO> getHealthRecordsByChildId(int childId);
}

