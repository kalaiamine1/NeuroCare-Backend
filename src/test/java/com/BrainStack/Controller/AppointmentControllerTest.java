package com.BrainStack.Controller;

import com.BrainStack.Dto.ApiResponse;
import com.BrainStack.Dto.AppointmentDTO;
import com.BrainStack.Dto.CreateAppointmentRequest;
import com.BrainStack.Services.AppointmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AppointmentController.class)
@org.springframework.context.annotation.Import(TestJacksonConfig.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    @Test
    void createAppointment_returnsCreated() throws Exception {
        CreateAppointmentRequest req = CreateAppointmentRequest.builder()
                .title("Test")
        .startTime(LocalDateTime.now().plusDays(1))
        .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .type("consultation")
                .parentId(1L)
                .professionalId(2L)
                .build();

        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(12L);
        Mockito.when(appointmentService.createAppointment(any(CreateAppointmentRequest.class))).thenReturn(dto);

    // Ensure Java time module is registered so LocalDateTime serializes correctly
    objectMapper.findAndRegisterModules();

    mockMvc.perform(post("/api/v1/appointments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id").value(12));
    }
}
