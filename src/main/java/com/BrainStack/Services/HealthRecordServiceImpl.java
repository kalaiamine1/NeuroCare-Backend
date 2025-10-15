package com.BrainStack.Services;

import com.BrainStack.Dto.HealthRecordRequestDTO;
import com.BrainStack.Dto.HealthRecordResponseDTO;
import com.BrainStack.Entity.Child;
import com.BrainStack.Entity.HealthRecord;
import com.BrainStack.Exception.ChildNotFoundException;
import com.BrainStack.Exception.HealthRecordNotFoundException;
import com.BrainStack.Repository.ChildRepository;
import com.BrainStack.Repository.HealthRecordRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implémentation du service pour la gestion des enregistrements de santé (HealthRecord).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class HealthRecordServiceImpl implements IHealthRecordService {

    private final HealthRecordRepository healthRecordRepository;
    private final ChildRepository childRepository;
    private final ModelMapper modelMapper;
    // Injection du service de détection d'anomalies
    private final IAnomalyDetectionService anomalyDetectionService;

    @Override
    public HealthRecordResponseDTO addHealthRecord(int childId, HealthRecordRequestDTO dto) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ChildNotFoundException("No child found with id " + childId));
        HealthRecord record = modelMapper.map(dto, HealthRecord.class);
        record.setChild(child);
        HealthRecord saved = addHealthRecord(record); // délégation à la méthode entité
        return toResponseDTO(saved);
    }

    /**
     * Ajoute un enregistrement de santé et déclenche la détection d'anomalies.
     * Ne modifie pas l'API publique de l'interface, mais peut être utilisée en interne.
     */
    public HealthRecord addHealthRecord(HealthRecord record) {
        HealthRecord saved = healthRecordRepository.save(record);
        // Déclencher la détection d'anomalies immédiatement après la sauvegarde
        anomalyDetectionService.detectAnomaliesForRecord(saved);
        return saved;
    }

    @Override
    public HealthRecordResponseDTO updateHealthRecord(Long healthRecordId, HealthRecordRequestDTO dto) {
        HealthRecord record = healthRecordRepository.findById(healthRecordId)
                .orElseThrow(() -> new HealthRecordNotFoundException("No health record found with id " + healthRecordId));
        record.setDate(dto.getDate());
        record.setSleepHours(dto.getSleepHours());
        record.setSteps(dto.getSteps());
        record.setMood(dto.getMood());
        record.setDietQuality(dto.getDietQuality());
        record.setWeight(dto.getWeight());
        record.setHeartRate(dto.getHeartRate());
        HealthRecord updated = healthRecordRepository.save(record);
        return toResponseDTO(updated);
    }

    @Override
    public void deleteHealthRecord(Long healthRecordId) {
        HealthRecord record = healthRecordRepository.findById(healthRecordId)
                .orElseThrow(() -> new HealthRecordNotFoundException("No health record found with id " + healthRecordId));
        healthRecordRepository.delete(record);
    }

    @Override
    public HealthRecordResponseDTO getHealthRecordById(Long healthRecordId) {
        HealthRecord record = healthRecordRepository.findById(healthRecordId)
                .orElseThrow(() -> new HealthRecordNotFoundException("No health record found with id " + healthRecordId));
        return toResponseDTO(record);
    }

    @Override
    public List<HealthRecordResponseDTO> getHealthRecordsByChildId(int childId) {
        if (!childRepository.existsById(childId)) {
            throw new ChildNotFoundException("No child found with id " + childId);
        }
        return healthRecordRepository.findByChildId(childId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    private HealthRecordResponseDTO toResponseDTO(HealthRecord record) {
        HealthRecordResponseDTO dto = modelMapper.map(record, HealthRecordResponseDTO.class);
        dto.setChildId(record.getChild().getId());
        return dto;
    }
}
