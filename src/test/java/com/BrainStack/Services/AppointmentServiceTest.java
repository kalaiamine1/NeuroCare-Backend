package com.BrainStack.Services;

import com.BrainStack.Dto.AppointmentDTO;
import com.BrainStack.Dto.CreateAppointmentRequest;
import com.BrainStack.Dto.UpdateAppointmentRequest;
import com.BrainStack.Entity.Appointment;
import com.BrainStack.Enums.AppointmentStatus;
import com.BrainStack.Enums.AppointmentType;
import com.BrainStack.Exception.ConflictException;
import com.BrainStack.Repository.AppointmentRepository;
import com.BrainStack.Utils.AppointmentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentMapper appointmentMapper;

    @InjectMocks
    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAppointment_happyPath_savesAndReturnsDTO() {
        CreateAppointmentRequest req = CreateAppointmentRequest.builder()
                .title("Checkup")
                .description("desc")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .type("consultation")
                .parentId(1L)
                .professionalId(2L)
                .build();

        Appointment saved = Appointment.builder()
                .id(10L)
                .title(req.getTitle())
                .description(req.getDescription())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .type(AppointmentType.CONSULTATION)
                .status(AppointmentStatus.PENDING)
                .parentId(req.getParentId())
                .professionalId(req.getProfessionalId())
                .notificationSent(false)
                .build();

        when(appointmentRepository.findConflicts(anyLong(), any(), any())).thenReturn(Collections.emptyList());
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(saved);
        when(appointmentMapper.toDTO(saved)).thenReturn(new AppointmentDTO(saved.getId(), saved.getTitle(), saved.getDescription(), saved.getStartTime(), saved.getEndTime(), saved.getType(), saved.getStatus(), saved.getParentId(), saved.getProfessionalId(), saved.getChildId(), saved.getLocation(), saved.getNotes(), saved.getCreatedAt(), saved.getUpdatedAt(), saved.getNotificationSent()));

        AppointmentDTO dto = appointmentService.createAppointment(req);

        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    void createAppointment_conflict_throwsConflictException() {
        CreateAppointmentRequest req = CreateAppointmentRequest.builder()
                .title("Checkup")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .type("consultation")
                .parentId(1L)
                .professionalId(2L)
                .build();

        Appointment existing = Appointment.builder().id(5L).build();
        when(appointmentRepository.findConflicts(eq(2L), any(), any())).thenReturn(List.of(existing));

        assertThrows(ConflictException.class, () -> appointmentService.createAppointment(req));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void getAppointmentById_notFound_throws() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(com.BrainStack.Exception.ResourceNotFoundException.class, () -> appointmentService.getAppointmentById(99L));
    }

    @Test
    void getAppointmentsByParent_returnsPage() {
        Appointment a = Appointment.builder().id(1L).title("t").startTime(LocalDateTime.now().plusDays(1)).endTime(LocalDateTime.now().plusDays(1).plusHours(1)).type(AppointmentType.CONSULTATION).status(AppointmentStatus.PENDING).parentId(3L).professionalId(4L).build();
    when(appointmentRepository.findByParentId(eq(3L), any(org.springframework.data.domain.Pageable.class))).thenReturn(new PageImpl<>(List.of(a)));
        when(appointmentMapper.toDTO(any())).thenReturn(new AppointmentDTO());

        var page = appointmentService.getAppointmentsByParent(3L,0,10);
        assertEquals(1, page.getTotalElements());
    }
}
