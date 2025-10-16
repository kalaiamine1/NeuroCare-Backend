package com.BrainStack.Controller;

import com.BrainStack.Dto.AnomalyDetectionDTO;
import com.BrainStack.Services.IAnomalyDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/anomalies")
@RequiredArgsConstructor
public class AnomalyDetectionController {

    private final IAnomalyDetectionService anomalyDetectionService;

    @GetMapping("/child/{childId}")
    public ResponseEntity<List<AnomalyDetectionDTO>> getAnomaliesByChild(@PathVariable int childId) {
        List<AnomalyDetectionDTO> anomalies = anomalyDetectionService.getAnomaliesByChildId(childId);
        return ResponseEntity.ok(anomalies);

    }

    @GetMapping("/child/{childId}/unresolved")
    public ResponseEntity<List<AnomalyDetectionDTO>> getUnresolvedAnomaliesByChild(@PathVariable int childId) {
        List<AnomalyDetectionDTO> anomalies = anomalyDetectionService.getUnresolvedAnomaliesByChildId(childId);
        return ResponseEntity.ok(anomalies);
    }

    @PostMapping("/resolve/{anomalyId}")
    public ResponseEntity<Void> resolveAnomaly(@PathVariable Long anomalyId) {
        anomalyDetectionService.resolveAnomaly(anomalyId);
        return ResponseEntity.noContent().build();
    }
}

