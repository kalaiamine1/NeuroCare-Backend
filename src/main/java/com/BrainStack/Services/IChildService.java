package com.BrainStack.Services;

import com.BrainStack.Dto.ChildRequestDTO;
import com.BrainStack.Dto.ChildResponseDTO;
import java.util.List;

/**
 * Interface du service pour la gestion des enfants.
 */
public interface IChildService {
    /**
     * Ajoute un enfant à un parent.
     */
    ChildResponseDTO addChild(int parentId, ChildRequestDTO dto);

    /**
     * Modifie un enfant existant.
     */
    ChildResponseDTO updateChild(int id, ChildRequestDTO dto);

    /**
     * Récupère un enfant par son id.
     */
    ChildResponseDTO getChildById(int id);

    /**
     * Récupère tous les enfants d'un parent.
     */
    List<ChildResponseDTO> getChildrenByParentId(int parentId);

    /**
     * Supprime un enfant.
     */
    void deleteChild(int id);
}
