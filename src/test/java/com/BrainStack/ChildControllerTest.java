package com.BrainStack;

import com.BrainStack.Controller.ChildController;
import com.BrainStack.Dto.ChildRequestDTO;
import com.BrainStack.Dto.ChildResponseDTO;
import com.BrainStack.Exception.ChildNotFoundException;
import com.BrainStack.Exception.ParentNotFoundException;
import com.BrainStack.Services.IChildService;
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

@WebMvcTest(ChildController.class)
class ChildControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private IChildService childService;
    @Autowired
    private ObjectMapper objectMapper;

    private final ChildResponseDTO responseDTO = ChildResponseDTO.builder()
            .id(3)
            .fullName("Adam Ben Aissa")
            .birthDate(LocalDate.of(2018, 3, 15))
            .gender("M")
            .diagnosis("Autisme")
            .parentId(1)
            .build();

    @Test
    void addChild_success() throws Exception {
        ChildRequestDTO requestDTO = ChildRequestDTO.builder()
                .fullName("Adam Ben Aissa")
                .birthDate(LocalDate.of(2018, 3, 15))
                .gender("M")
                .diagnosis("Autisme")
                .build();
        Mockito.when(childService.addChild(eq(1), any(ChildRequestDTO.class))).thenReturn(responseDTO);
        mockMvc.perform(post("/api/children/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.parentId").value(1));
    }

    @Test
    void addChild_parentNotFound() throws Exception {
        ChildRequestDTO requestDTO = ChildRequestDTO.builder()
                .fullName("Adam Ben Aissa")
                .birthDate(LocalDate.of(2018, 3, 15))
                .gender("M")
                .diagnosis("Autisme")
                .build();
        Mockito.when(childService.addChild(eq(1), any(ChildRequestDTO.class))).thenThrow(new ParentNotFoundException("No parent found with id 1"));
        mockMvc.perform(post("/api/children/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Parent not found"));
    }

    @Test
    void updateChild_success() throws Exception {
        ChildRequestDTO requestDTO = ChildRequestDTO.builder()
                .fullName("Adam Ben Aissa")
                .birthDate(LocalDate.of(2018, 3, 15))
                .gender("M")
                .diagnosis("Autisme")
                .build();
        Mockito.when(childService.updateChild(eq(3), any(ChildRequestDTO.class))).thenReturn(responseDTO);
        mockMvc.perform(put("/api/children/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    void updateChild_childNotFound() throws Exception {
        ChildRequestDTO requestDTO = ChildRequestDTO.builder()
                .fullName("Adam Ben Aissa")
                .birthDate(LocalDate.of(2018, 3, 15))
                .gender("M")
                .diagnosis("Autisme")
                .build();
        Mockito.when(childService.updateChild(eq(3), any(ChildRequestDTO.class))).thenThrow(new ChildNotFoundException("No child found with id 3"));
        mockMvc.perform(put("/api/children/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Child not found"));
    }

    @Test
    void getChildById_success() throws Exception {
        Mockito.when(childService.getChildById(3)).thenReturn(responseDTO);
        mockMvc.perform(get("/api/children/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    void getChildById_childNotFound() throws Exception {
        Mockito.when(childService.getChildById(3)).thenThrow(new ChildNotFoundException("No child found with id 3"));
        mockMvc.perform(get("/api/children/3"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Child not found"));
    }

    @Test
    void getChildrenByParentId_success() throws Exception {
        Mockito.when(childService.getChildrenByParentId(1)).thenReturn(List.of(responseDTO));
        mockMvc.perform(get("/api/children/parent/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3));
    }

    @Test
    void getChildrenByParentId_parentNotFound() throws Exception {
        Mockito.when(childService.getChildrenByParentId(1)).thenThrow(new ParentNotFoundException("No parent found with id 1"));
        mockMvc.perform(get("/api/children/parent/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Parent not found"));
    }

    @Test
    void deleteChild_success() throws Exception {
        Mockito.doNothing().when(childService).deleteChild(3);
        mockMvc.perform(delete("/api/children/3"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteChild_childNotFound() throws Exception {
        Mockito.doThrow(new ChildNotFoundException("No child found with id 3")).when(childService).deleteChild(3);
        mockMvc.perform(delete("/api/children/3"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Child not found"));
    }
}
