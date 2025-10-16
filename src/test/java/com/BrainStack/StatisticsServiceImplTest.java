package com.BrainStack;

import com.BrainStack.Dto.StatsPointDTO;
import com.BrainStack.Dto.TimeSeriesPointDTO;
import com.BrainStack.Entity.HealthRecord;
import com.BrainStack.Exception.ChildNotFoundException;
import com.BrainStack.Repository.ChildRepository;
import com.BrainStack.Repository.HealthRecordRepository;
import com.BrainStack.Services.StatisticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class StatisticsServiceImplTest {

    @Mock
    private HealthRecordRepository healthRecordRepository;
    @Mock
    private ChildRepository childRepository;
    @InjectMocks
    private StatisticsServiceImpl statisticsService;

    private final int childId = 1;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(childRepository.existsById(childId)).thenReturn(true);
    }

    @Test
    void getWeeklyStats_returns7Points_andAverages() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);
        // Deux enregistrements le même jour pour tester la moyenne
        HealthRecord r1 = HealthRecord.builder().date(end.minusDays(1)).sleepHours(8.0).weight(25.0).heartRate(80.0).steps(7000).build();
        HealthRecord r2 = HealthRecord.builder().date(end.minusDays(1)).sleepHours(6.0).weight(26.0).heartRate(100.0).steps(9000).build();
        // Un jour avec valeurs nulles ne doit pas casser; sera null si toutes valeurs nulles
        HealthRecord r3 = HealthRecord.builder().date(end.minusDays(3)).sleepHours(null).weight(null).heartRate(null).steps(null).build();
        when(healthRecordRepository.findByChildIdAndDateBetween(eq(childId), eq(start), eq(end)))
                .thenReturn(Arrays.asList(r1, r2, r3));

        List<StatsPointDTO> out = statisticsService.getWeeklyStats(childId);
        assertEquals(7, out.size());
        assertEquals(start.toString(), out.get(0).getDate());
        // Vérifier moyenne sur end-1
        StatsPointDTO dayAvg = out.get(6 - 1); // index du jour end-1
        assertEquals(Double.valueOf(7.0), dayAvg.getAverageSleepHours());
        assertEquals(Double.valueOf(25.5), dayAvg.getAverageWeight());
        assertEquals(Double.valueOf(90.0), dayAvg.getAverageHeartRate());
        assertEquals(Double.valueOf(8000.0), dayAvg.getAverageSteps());
        // Jour avec valeurs nulles => moyennes nulles
        StatsPointDTO nullDay = out.get(6 - 3);
        assertNull(nullDay.getAverageSleepHours());
        assertNull(nullDay.getAverageWeight());
        assertNull(nullDay.getAverageHeartRate());
        assertNull(nullDay.getAverageSteps());
    }

    @Test
    void getMonthlyStats_returns6Points_ordered() {
        LocalDate today = LocalDate.now();
        YearMonth currentYm = YearMonth.from(today);
        YearMonth startYm = currentYm.minusMonths(5);
        LocalDate start = startYm.atDay(1);
        LocalDate end = currentYm.atEndOfMonth();
        // Enregistrements répartis sur 2 mois
        HealthRecord r1 = HealthRecord.builder().date(currentYm.atDay(10)).weight(25.0).build();
        HealthRecord r2 = HealthRecord.builder().date(currentYm.atDay(20)).weight(27.0).build();
        HealthRecord r3 = HealthRecord.builder().date(currentYm.minusMonths(1).atDay(5)).weight(23.0).build();
        when(healthRecordRepository.findByChildIdAndDateBetween(eq(childId), eq(start), eq(end)))
                .thenReturn(Arrays.asList(r1, r2, r3));

        List<StatsPointDTO> out = statisticsService.getMonthlyStats(childId);
        assertEquals(6, out.size());
        assertEquals(startYm.atDay(1).toString(), out.get(0).getDate());
        // Vérifier moyenne du mois courant
        StatsPointDTO currentMonthPoint = out.get(5);
        assertEquals(Double.valueOf(26.0), currentMonthPoint.getAverageWeight());
        // Mois sans données => null
        StatsPointDTO emptyMonth = out.get(0); // le plus ancien, supposé vide selon nos données
        assertNull(emptyMonth.getAverageWeight());
    }

    @Test
    void getChartSeries_weight_6m_includesMissingDaysWithNull() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusMonths(6);
        // Deux jours seulement dans la période
        HealthRecord r1 = HealthRecord.builder().date(end.minusDays(2)).weight(25.0).build();
        HealthRecord r2 = HealthRecord.builder().date(end.minusDays(2)).weight(27.0).build();
        HealthRecord r3 = HealthRecord.builder().date(end.minusDays(0)).weight(26.0).build();
        when(healthRecordRepository.findByChildIdAndDateBetween(eq(childId), eq(start), eq(end)))
                .thenReturn(Arrays.asList(r1, r2, r3));

        List<TimeSeriesPointDTO> out = statisticsService.getChartSeries(childId, "weight", "6m");
        assertFalse(out.isEmpty());
        // Trouver le jour end-2
        TimeSeriesPointDTO dayMinus2 = out.stream().filter(p -> p.getDate().equals(end.minusDays(2).toString())).findFirst().orElseThrow();
        assertEquals(Double.valueOf(26.0), dayMinus2.getValue());
        // Un jour sans données doit exister et valeur null (par ex end-1)
        TimeSeriesPointDTO dayMinus1 = out.stream().filter(p -> p.getDate().equals(end.minusDays(1).toString())).findFirst().orElseThrow();
        assertNull(dayMinus1.getValue());
    }

    @Test
    void getChartSeries_invalidParam_throws() {
        assertThrows(IllegalArgumentException.class, () -> statisticsService.getChartSeries(childId, "unknown", "7d"));
    }

    @Test
    void getChartSeries_invalidPeriod_throws() {
        assertThrows(IllegalArgumentException.class, () -> statisticsService.getChartSeries(childId, "sleep", "abc"));
    }

    @Test
    void childNotFound_throws() {
        when(childRepository.existsById(childId)).thenReturn(false);
        assertThrows(ChildNotFoundException.class, () -> statisticsService.getWeeklyStats(childId));
    }
}

