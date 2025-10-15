package com.BrainStack;

import com.BrainStack.Controller.StatisticsController;
import com.BrainStack.Dto.StatsPointDTO;
import com.BrainStack.Dto.TimeSeriesPointDTO;
import com.BrainStack.Services.IStatisticsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatisticsController.class)
@Import(StatisticsControllerTest.TestConfig.class)
class StatisticsControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        IStatisticsService statisticsService() {
            return Mockito.mock(IStatisticsService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IStatisticsService statisticsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getWeeklyStats_success() throws Exception {
        List<StatsPointDTO> payload = List.of(
                StatsPointDTO.builder().date("2025-10-10").averageSleepHours(8.0).averageWeight(25.0).averageHeartRate(80.0).averageSteps(7000.0).build(),
                StatsPointDTO.builder().date("2025-10-11").averageSleepHours(7.5).averageWeight(25.1).averageHeartRate(82.0).averageSteps(8000.0).build()
        );
        Mockito.when(statisticsService.getWeeklyStats(1)).thenReturn(payload);
        mockMvc.perform(get("/api/v1/api/health-records/child/1/weekly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2025-10-10"))
                .andExpect(jsonPath("$[0].averageSleepHours").value(8.0));
    }

    @Test
    void getMonthlyStats_success() throws Exception {
        List<StatsPointDTO> payload = List.of(
                StatsPointDTO.builder().date("2025-07-01").averageSleepHours(8.0).averageWeight(24.9).averageHeartRate(79.0).averageSteps(6500.0).build()
        );
        Mockito.when(statisticsService.getMonthlyStats(1)).thenReturn(payload);
        mockMvc.perform(get("/api/v1/api/health-records/child/1/monthly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2025-07-01"));
    }

    @Test
    void getChartSeries_success() throws Exception {
        List<TimeSeriesPointDTO> payload = List.of(
                TimeSeriesPointDTO.builder().date("2025-10-10").value(25.0).build(),
                TimeSeriesPointDTO.builder().date("2025-10-11").value(25.1).build()
        );
        Mockito.when(statisticsService.getChartSeries(1, "weight", "6m")).thenReturn(payload);
        mockMvc.perform(get("/api/v1/api/health-records/child/1/chart")
                        .param("param", "weight")
                        .param("period", "6m")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].value").value(25.0));
    }

    @Test
    void getChartSeries_invalidParam_returnsBadRequest() throws Exception {
        Mockito.when(statisticsService.getChartSeries(1, "unknown", "7d"))
                .thenThrow(new IllegalArgumentException("Invalid param 'unknown'. Expected one of sleep, weight, heartRate, steps"));
        mockMvc.perform(get("/api/v1/api/health-records/child/1/chart")
                        .param("param", "unknown")
                        .param("period", "7d"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad request"));
    }
}

