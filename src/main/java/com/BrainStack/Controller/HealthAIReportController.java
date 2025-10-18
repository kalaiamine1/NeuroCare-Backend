package com.BrainStack.Controller;

import com.BrainStack.Dto.AnomalyReportDTO;
import com.BrainStack.Services.IAnomalyDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/v1/anomalies")
@RequiredArgsConstructor
public class HealthAIReportController {

    private final IAnomalyDetectionService anomalyDetectionService;

    @GetMapping("/child/{childId}/report")
    public ResponseEntity<AnomalyReportDTO> getReport(@PathVariable int childId) {
        AnomalyReportDTO report = anomalyDetectionService.getReportByChildId(childId);
        return ResponseEntity.ok(report);
    }
}

