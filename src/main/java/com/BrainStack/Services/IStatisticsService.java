package com.BrainStack.Services;

import com.BrainStack.Dto.StatsPointDTO;
import com.BrainStack.Dto.TimeSeriesPointDTO;

import java.util.List;

public interface IStatisticsService {
    /**
     * Retourne les moyennes journalières pour les 7 derniers jours (inclus aujourd'hui).
     */
    List<StatsPointDTO> getWeeklyStats(int childId);

    /**
     * Retourne les moyennes mensuelles pour les 6 derniers mois (inclus le mois courant).
     */
    List<StatsPointDTO> getMonthlyStats(int childId);

    /**
     * Retourne la série journalière pour un paramètre donné sur une période (ex: 6m, 30d).
     * @param param valeurs acceptées: sleep|weight|heartRate|steps
     * @param period format n + (d|m)
     */
    List<TimeSeriesPointDTO> getChartSeries(int childId, String param, String period);
}

