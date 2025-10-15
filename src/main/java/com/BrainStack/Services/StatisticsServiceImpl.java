package com.BrainStack.Services;

import com.BrainStack.Dto.StatsPointDTO;
import com.BrainStack.Dto.TimeSeriesPointDTO;
import com.BrainStack.Entity.HealthRecord;
import com.BrainStack.Exception.ChildNotFoundException;
import com.BrainStack.Repository.ChildRepository;
import com.BrainStack.Repository.HealthRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements IStatisticsService {

    private final HealthRecordRepository healthRecordRepository;
    private final ChildRepository childRepository;

    @Override
    public List<StatsPointDTO> getWeeklyStats(int childId) {
        ensureChildExists(childId);
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6); // 7 jours incluant aujourd'hui
        List<HealthRecord> records = healthRecordRepository.findByChildIdAndDateBetween(childId, start, end);

        Map<LocalDate, List<HealthRecord>> byDate = records.stream()
                .collect(Collectors.groupingBy(HealthRecord::getDate));

        List<StatsPointDTO> result = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            List<HealthRecord> dayRecords = byDate.getOrDefault(d, Collections.emptyList());
            result.add(StatsPointDTO.builder()
                    .date(d.toString())
                    .averageSleepHours(avgDouble(dayRecords, HealthRecord::getSleepHours))
                    .averageWeight(avgDouble(dayRecords, HealthRecord::getWeight))
                    .averageHeartRate(avgDouble(dayRecords, HealthRecord::getHeartRate))
                    .averageSteps(avgInteger(dayRecords, HealthRecord::getSteps))
                    .build());
        }
        return result;
    }

    @Override
    public List<StatsPointDTO> getMonthlyStats(int childId) {
        ensureChildExists(childId);
        LocalDate today = LocalDate.now();
        YearMonth currentYm = YearMonth.from(today);
        YearMonth startYm = currentYm.minusMonths(5); // 6 derniers mois (incluant le courant)
        LocalDate start = startYm.atDay(1);
        LocalDate end = currentYm.atEndOfMonth();

        List<HealthRecord> records = healthRecordRepository.findByChildIdAndDateBetween(childId, start, end);
        Map<YearMonth, List<HealthRecord>> byMonth = records.stream()
                .collect(Collectors.groupingBy(r -> YearMonth.from(r.getDate())));

        List<StatsPointDTO> result = new ArrayList<>();
        for (YearMonth ym = startYm; !ym.isAfter(currentYm); ym = ym.plusMonths(1)) {
            List<HealthRecord> monthRecords = byMonth.getOrDefault(ym, Collections.emptyList());
            result.add(StatsPointDTO.builder()
                    .date(ym.atDay(1).toString())
                    .averageSleepHours(avgDouble(monthRecords, HealthRecord::getSleepHours))
                    .averageWeight(avgDouble(monthRecords, HealthRecord::getWeight))
                    .averageHeartRate(avgDouble(monthRecords, HealthRecord::getHeartRate))
                    .averageSteps(avgInteger(monthRecords, HealthRecord::getSteps))
                    .build());
        }
        return result;
    }

    @Override
    public List<TimeSeriesPointDTO> getChartSeries(int childId, String param, String period) {
        ensureChildExists(childId);
        ParamSelector selector = ParamSelector.from(param);
        Range range = parsePeriod(period);

        List<HealthRecord> records = healthRecordRepository.findByChildIdAndDateBetween(childId, range.start, range.end);
        Map<LocalDate, List<HealthRecord>> byDate = records.stream()
                .collect(Collectors.groupingBy(HealthRecord::getDate));

        List<TimeSeriesPointDTO> result = new ArrayList<>();
        for (LocalDate d = range.start; !d.isAfter(range.end); d = d.plusDays(1)) {
            List<HealthRecord> dayRecords = byDate.getOrDefault(d, Collections.emptyList());
            Double value = selector.average(dayRecords);
            result.add(TimeSeriesPointDTO.builder().date(d.toString()).value(value).build());
        }
        return result;
    }

    private void ensureChildExists(int childId) {
        if (!childRepository.existsById(childId)) {
            throw new ChildNotFoundException("No child found with id " + childId);
        }
    }

    private Double avgDouble(List<HealthRecord> list, Function<HealthRecord, Double> getter) {
        List<Double> values = list.stream().map(getter).filter(Objects::nonNull).collect(Collectors.toList());
        if (values.isEmpty()) return null;
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
    }

    private Double avgInteger(List<HealthRecord> list, Function<HealthRecord, Integer> getter) {
        List<Integer> values = list.stream().map(getter).filter(Objects::nonNull).collect(Collectors.toList());
        if (values.isEmpty()) return null;
        return values.stream().mapToInt(Integer::intValue).average().orElse(Double.NaN);
    }

    private Range parsePeriod(String period) {
        if (period == null || period.isBlank()) {
            throw new IllegalArgumentException("Period is required, examples: 7d, 30d, 6m");
        }
        String trimmed = period.trim().toLowerCase();
        char unit = trimmed.charAt(trimmed.length() - 1);
        int amount;
        try {
            amount = Integer.parseInt(trimmed.substring(0, trimmed.length() - 1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid period format. Expected <n><d|m> like 7d or 6m");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Period amount must be > 0");
        }
        LocalDate end = LocalDate.now();
        LocalDate start;
        switch (unit) {
            case 'd' -> start = end.minusDays(amount - 1); // inclut aujourd'hui
            case 'm' -> start = end.minusMonths(amount).plusDays(0); // sur n mois calendrier glissants
            default -> throw new IllegalArgumentException("Invalid period unit. Use 'd' for days or 'm' for months");
        }
        return new Range(start, end);
    }

    private enum ParamSelector {
        SLEEP("sleep") {
            @Override
            Double average(List<HealthRecord> records) { return avg(records, HealthRecord::getSleepHours); }
        },
        WEIGHT("weight") {
            @Override
            Double average(List<HealthRecord> records) { return avg(records, HealthRecord::getWeight); }
        },
        HEART_RATE("heartRate") {
            @Override
            Double average(List<HealthRecord> records) { return avg(records, HealthRecord::getHeartRate); }
        },
        STEPS("steps") {
            @Override
            Double average(List<HealthRecord> records) { return avgInt(records, HealthRecord::getSteps); }
        };

        private final String key;
        ParamSelector(String key) { this.key = key; }

        abstract Double average(List<HealthRecord> records);

        static ParamSelector from(String key) {
            if (key == null) throw new IllegalArgumentException("param is required: one of sleep, weight, heartRate, steps");
            String k = key.trim();
            for (ParamSelector ps : values()) {
                if (ps.key.equalsIgnoreCase(k)) return ps;
            }
            throw new IllegalArgumentException("Invalid param '" + key + "'. Expected one of sleep, weight, heartRate, steps");
        }

        private static Double avg(List<HealthRecord> list, Function<HealthRecord, Double> getter) {
            List<Double> values = list.stream().map(getter).filter(Objects::nonNull).collect(Collectors.toList());
            if (values.isEmpty()) return null;
            return values.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
        }

        private static Double avgInt(List<HealthRecord> list, Function<HealthRecord, Integer> getter) {
            List<Integer> values = list.stream().map(getter).filter(Objects::nonNull).collect(Collectors.toList());
            if (values.isEmpty()) return null;
            return values.stream().mapToInt(Integer::intValue).average().orElse(Double.NaN);
        }
    }

    private record Range(LocalDate start, LocalDate end) {}
}

