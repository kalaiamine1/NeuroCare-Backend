package com.BrainStack;

import com.BrainStack.Dto.ChildRequestDTO;
import com.BrainStack.Dto.ChildResponseDTO;
import com.BrainStack.Entity.Child;
import com.BrainStack.Entity.User;
import com.BrainStack.Exception.ChildNotFoundException;
import com.BrainStack.Exception.ParentNotFoundException;
import com.BrainStack.Repository.ChildRepository;
import com.BrainStack.Repository.UserRepository;
import com.BrainStack.Services.ChildServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChildServiceImplTest {
    @Mock
    private ChildRepository childRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ModelMapper modelMapper;
    @InjectMocks
    private ChildServiceImpl childService;

    private User parent;
    private Child child;
    private ChildRequestDTO requestDTO;
    private ChildResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        parent = User.builder().id(1).build();
        child = Child.builder()
                .id(2)
                .fullName("Adam Ben Aissa")
                .birthDate(LocalDate.of(2018, 3, 15))
                .gender("M")
                .diagnosis("Autisme")
                .parent(parent)
                .build();
        requestDTO = ChildRequestDTO.builder()
                .fullName("Adam Ben Aissa")
                .birthDate(LocalDate.of(2018, 3, 15))
                .gender("M")
                .diagnosis("Autisme")
                .build();
        responseDTO = ChildResponseDTO.builder()
                .id(2)
                .fullName("Adam Ben Aissa")
                .birthDate(LocalDate.of(2018, 3, 15))
                .gender("M")
                .diagnosis("Autisme")
                .parentId(1)
                .build();
    }

    @Test
    void addChild_success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(parent));
        when(modelMapper.map(requestDTO, Child.class)).thenReturn(child);
        when(childRepository.save(any(Child.class))).thenReturn(child);
        when(modelMapper.map(child, ChildResponseDTO.class)).thenReturn(responseDTO);
        ChildResponseDTO result = childService.addChild(1, requestDTO);
        assertEquals(responseDTO, result);
    }

    @Test
    void addChild_parentNotFound() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(ParentNotFoundException.class, () -> childService.addChild(1, requestDTO));
    }

    @Test
    void updateChild_success() {
        when(childRepository.findById(2)).thenReturn(Optional.of(child));
        when(childRepository.save(any(Child.class))).thenReturn(child);
        when(modelMapper.map(child, ChildResponseDTO.class)).thenReturn(responseDTO);
        ChildResponseDTO result = childService.updateChild(2, requestDTO);
        assertEquals(responseDTO, result);
    }

    @Test
    void updateChild_childNotFound() {
        when(childRepository.findById(2)).thenReturn(Optional.empty());
        assertThrows(ChildNotFoundException.class, () -> childService.updateChild(2, requestDTO));
    }

    @Test
    void getChildById_success() {
        when(childRepository.findById(2)).thenReturn(Optional.of(child));
        when(modelMapper.map(child, ChildResponseDTO.class)).thenReturn(responseDTO);
        ChildResponseDTO result = childService.getChildById(2);
        assertEquals(responseDTO, result);
    }

    @Test
    void getChildById_childNotFound() {
        when(childRepository.findById(2)).thenReturn(Optional.empty());
        assertThrows(ChildNotFoundException.class, () -> childService.getChildById(2));
    }

    @Test
    void getChildrenByParentId_success() {
        when(userRepository.existsById(1)).thenReturn(true);
        when(childRepository.findByParentId(1)).thenReturn(List.of(child));
        when(modelMapper.map(child, ChildResponseDTO.class)).thenReturn(responseDTO);
        List<ChildResponseDTO> result = childService.getChildrenByParentId(1);
        assertEquals(1, result.size());
        assertEquals(responseDTO, result.get(0));
    }

    @Test
    void getChildrenByParentId_parentNotFound() {
        when(userRepository.existsById(1)).thenReturn(false);
        assertThrows(ParentNotFoundException.class, () -> childService.getChildrenByParentId(1));
    }

    @Test
    void deleteChild_success() {
        when(childRepository.findById(2)).thenReturn(Optional.of(child));
        doNothing().when(childRepository).delete(child);
        assertDoesNotThrow(() -> childService.deleteChild(2));
    }

    @Test
    void deleteChild_childNotFound() {
        when(childRepository.findById(2)).thenReturn(Optional.empty());
        assertThrows(ChildNotFoundException.class, () -> childService.deleteChild(2));
    }
}
