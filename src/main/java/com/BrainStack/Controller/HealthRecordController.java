package com.BrainStack.Controller;

import com.BrainStack.Dto.HealthRecordRequestDTO;
import com.BrainStack.Dto.HealthRecordResponseDTO;
import com.BrainStack.Services.IHealthRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des enregistrements de santé (HealthRecord).
 */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/health-records")
@RequiredArgsConstructor
public class HealthRecordController {

    private final IHealthRecordService healthRecordService;

    /**
     * Ajoute un enregistrement de santé à un enfant.
     */
    @PostMapping("/{childId}")
    public ResponseEntity<HealthRecordResponseDTO> addHealthRecord(@PathVariable int childId,
                                                                   @Valid @RequestBody HealthRecordRequestDTO dto) {
        HealthRecordResponseDTO response = healthRecordService.addHealthRecord(childId, dto);
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Modifie un enregistrement de santé existant.
     */
    @PutMapping("/{healthRecordId}")
    public ResponseEntity<HealthRecordResponseDTO> updateHealthRecord(@PathVariable Long healthRecordId,
                                                                      @Valid @RequestBody HealthRecordRequestDTO dto) {
        HealthRecordResponseDTO response = healthRecordService.updateHealthRecord(healthRecordId, dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Supprime un enregistrement de santé.
     */
    @DeleteMapping("/{healthRecordId}")
    public ResponseEntity<Void> deleteHealthRecord(@PathVariable Long healthRecordId) {
        healthRecordService.deleteHealthRecord(healthRecordId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Affiche un enregistrement de santé par son id.
     */
    @GetMapping("/{healthRecordId}")
    public ResponseEntity<HealthRecordResponseDTO> getHealthRecordById(@PathVariable Long healthRecordId) {
        HealthRecordResponseDTO response = healthRecordService.getHealthRecordById(healthRecordId);
        return ResponseEntity.ok(response);
    }

    /**
     * Affiche tous les enregistrements de santé d'un enfant.
     */
    @GetMapping("/child/{childId}")
    public ResponseEntity<List<HealthRecordResponseDTO>> getHealthRecordsByChildId(@PathVariable int childId) {
        List<HealthRecordResponseDTO> responses = healthRecordService.getHealthRecordsByChildId(childId);
        return ResponseEntity.ok(responses);
    }
}

