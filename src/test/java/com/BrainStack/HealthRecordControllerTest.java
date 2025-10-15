package com.BrainStack;

import com.BrainStack.Controller.HealthRecordController;
import com.BrainStack.Dto.HealthRecordRequestDTO;
import com.BrainStack.Dto.HealthRecordResponseDTO;
import com.BrainStack.Exception.ChildNotFoundException;
import com.BrainStack.Exception.HealthRecordNotFoundException;
import com.BrainStack.Services.IHealthRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HealthRecordController.class)
class HealthRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IHealthRecordService healthRecordService;

    @Autowired
    private ObjectMapper objectMapper;

    private HealthRecordRequestDTO buildRequestDTO() {
        return HealthRecordRequestDTO.builder()
                .date(LocalDate.of(2025, 1, 15))
                .sleepHours(8.0)
                .steps(7500)
                .mood("Heureux")
                .dietQuality("Équilibré")
                .weight(25.5)
                .heartRate(80.0)
                .build();
    }

    private HealthRecordResponseDTO buildResponseDTO() {
        return HealthRecordResponseDTO.builder()
                .id(10L)
                .date(LocalDate.of(2025, 1, 15))
                .sleepHours(8.0)
                .steps(7500)
                .mood("Heureux")
                .dietQuality("Équilibré")
                .weight(25.5)
                .heartRate(80.0)
                .childId(1)
                .build();
    }

    @Test
    void addHealthRecord_success() throws Exception {
        HealthRecordRequestDTO requestDTO = buildRequestDTO();
        HealthRecordResponseDTO responseDTO = buildResponseDTO();

        Mockito.when(healthRecordService.addHealthRecord(eq(1), any(HealthRecordRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/api/health-records/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.childId").value(1))
                .andExpect(jsonPath("$.mood").value("Heureux"));
    }

    @Test
    void addHealthRecord_childNotFound() throws Exception {
        HealthRecordRequestDTO requestDTO = buildRequestDTO();

        Mockito.when(healthRecordService.addHealthRecord(eq(1), any(HealthRecordRequestDTO.class)))
                .thenThrow(new ChildNotFoundException("No child found with id 1"));

        mockMvc.perform(post("/api/health-records/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Child not found"));
    }

    @Test
    void updateHealthRecord_success() throws Exception {
        HealthRecordRequestDTO requestDTO = buildRequestDTO();
        HealthRecordResponseDTO responseDTO = buildResponseDTO();

        Mockito.when(healthRecordService.updateHealthRecord(eq(10L), any(HealthRecordRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(put("/api/health-records/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.mood").value("Heureux"));
    }

    @Test
    void updateHealthRecord_healthRecordNotFound() throws Exception {
        HealthRecordRequestDTO requestDTO = buildRequestDTO();

        Mockito.when(healthRecordService.updateHealthRecord(eq(10L), any(HealthRecordRequestDTO.class)))
                .thenThrow(new HealthRecordNotFoundException("No health record found with id 10"));

        mockMvc.perform(put("/api/health-records/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Health record not found"));
    }

    @Test
    void deleteHealthRecord_success() throws Exception {
        Mockito.doNothing().when(healthRecordService).deleteHealthRecord(10L);

        mockMvc.perform(delete("/api/health-records/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteHealthRecord_healthRecordNotFound() throws Exception {
        Mockito.doThrow(new HealthRecordNotFoundException("No health record found with id 10"))
                .when(healthRecordService).deleteHealthRecord(10L);

        mockMvc.perform(delete("/api/health-records/10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Health record not found"));
    }

    @Test
    void getHealthRecordById_success() throws Exception {
        HealthRecordResponseDTO responseDTO = buildResponseDTO();
        Mockito.when(healthRecordService.getHealthRecordById(10L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/health-records/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.childId").value(1));
    }

    @Test
    void getHealthRecordById_healthRecordNotFound() throws Exception {
        Mockito.when(healthRecordService.getHealthRecordById(10L))
                .thenThrow(new HealthRecordNotFoundException("No health record found with id 10"));

        mockMvc.perform(get("/api/health-records/10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Health record not found"));
    }

    @Test
    void getHealthRecordsByChildId_success() throws Exception {
        HealthRecordResponseDTO responseDTO = buildResponseDTO();
        Mockito.when(healthRecordService.getHealthRecordsByChildId(1))
                .thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/health-records/child/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].childId").value(1));
    }

    @Test
    void getHealthRecordsByChildId_childNotFound() throws Exception {
        Mockito.when(healthRecordService.getHealthRecordsByChildId(1))
                .thenThrow(new ChildNotFoundException("No child found with id 1"));

        mockMvc.perform(get("/api/health-records/child/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Child not found"));
    }
}
