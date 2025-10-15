package com.BrainStack.Controller;

import com.BrainStack.Dto.ChildRequestDTO;
import com.BrainStack.Dto.ChildResponseDTO;
import com.BrainStack.Services.IChildService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Contrôleur REST pour la gestion des enfants.
 */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/children")
@RequiredArgsConstructor
public class ChildController {
    private final IChildService childService;

    /**
     * Ajoute un enfant à un parent.
     */
    @PostMapping("/{parentId}")
    public ResponseEntity<ChildResponseDTO> addChild(@PathVariable int parentId,
                                                     @Valid @RequestBody ChildRequestDTO dto) {
        ChildResponseDTO response = childService.addChild(parentId, dto);
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Modifie un enfant existant.
     */
    @PutMapping("/{childId}")
    public ResponseEntity<ChildResponseDTO> updateChild(@PathVariable int childId,
                                                       @Valid @RequestBody ChildRequestDTO dto) {
        ChildResponseDTO response = childService.updateChild(childId, dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Affiche un enfant par son id.
     */
    @GetMapping("/{childId}")
    public ResponseEntity<ChildResponseDTO> getChildById(@PathVariable int childId) {
        ChildResponseDTO response = childService.getChildById(childId);
        return ResponseEntity.ok(response);
    }

    /**
     * Affiche tous les enfants d'un parent.
     */
    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<ChildResponseDTO>> getChildrenByParentId(@PathVariable int parentId) {
        List<ChildResponseDTO> responses = childService.getChildrenByParentId(parentId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Supprime un enfant.
     */
    @DeleteMapping("/{childId}")
    public ResponseEntity<Void> deleteChild(@PathVariable int childId) {
        childService.deleteChild(childId);
        return ResponseEntity.noContent().build();
    }
}
