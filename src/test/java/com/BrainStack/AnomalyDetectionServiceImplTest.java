package com.BrainStack;

import com.BrainStack.Entity.Child;
import com.BrainStack.Entity.HealthRecord;
import com.BrainStack.Entity.AnomalyDetection;
import com.BrainStack.Repository.AnomalyDetectionRepository;
import com.BrainStack.Services.AnomalyDetectionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.anyList;

class AnomalyDetectionServiceImplTest {

    @Mock
    private AnomalyDetectionRepository anomalyDetectionRepository;

    @InjectMocks
    private AnomalyDetectionServiceImpl anomalyDetectionService;

    private Child child;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        child = Child.builder().id(1).fullName("Test Child").build();
    }

    @Test
    void detectAnomaliesForRecord_detectsAllThree() {
        HealthRecord record = HealthRecord.builder()
                .date(LocalDate.now())
                .sleepHours(5.5)
                .steps(2000)
                .mood("Stressé")
                .child(child)
                .build();

        ArgumentCaptor<List<AnomalyDetection>> captor = ArgumentCaptor.forClass(List.class);
        when(anomalyDetectionRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        anomalyDetectionService.detectAnomaliesForRecord(record);

        verify(anomalyDetectionRepository, times(1)).saveAll(captor.capture());
        List<AnomalyDetection> saved = captor.getValue();
        assertEquals(3, saved.size());
        assertTrue(saved.stream().anyMatch(a -> "Sommeil insuffisant".equals(a.getType())));
        assertTrue(saved.stream().anyMatch(a -> "Baisse d’activité".equals(a.getType())));
        assertTrue(saved.stream().anyMatch(a -> "Stress élevé".equals(a.getType())));
        assertTrue(saved.stream().allMatch(a -> a.getChild() != null && a.getResolved() != null && !a.getResolved()));
    }

    @Test
    void detectAnomaliesForRecord_noAnomalies_noSave() {
        HealthRecord record = HealthRecord.builder()
                .date(LocalDate.now())
                .sleepHours(8.0)
                .steps(7500)
                .mood("Heureux")
                .child(child)
                .build();

        anomalyDetectionService.detectAnomaliesForRecord(record);

        verify(anomalyDetectionRepository, never()).saveAll(anyList());
    }

    @Test
    void detectAnomaliesForRecord_nullChild_noSave() {
        HealthRecord record = HealthRecord.builder()
                .date(LocalDate.now())
                .sleepHours(5.0)
                .steps(2000)
                .mood("Stressé")
                .build();

        anomalyDetectionService.detectAnomaliesForRecord(record);
        verify(anomalyDetectionRepository, never()).saveAll(anyList());
    }
}
