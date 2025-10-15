package com.BrainStack.Services;

import com.BrainStack.Dto.ChildRequestDTO;
import com.BrainStack.Dto.ChildResponseDTO;
import com.BrainStack.Entity.Child;
import com.BrainStack.Entity.User;
import com.BrainStack.Exception.ChildNotFoundException;
import com.BrainStack.Exception.ParentNotFoundException;
import com.BrainStack.Repository.ChildRepository;
import com.BrainStack.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implémentation du service pour la gestion des enfants.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ChildServiceImpl implements IChildService {
    private final ChildRepository childRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public ChildResponseDTO addChild(int parentId, ChildRequestDTO dto) {
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new ParentNotFoundException("No parent found with id " + parentId));
        Child child = modelMapper.map(dto, Child.class);
        child.setParent(parent);
        Child saved = childRepository.save(child);
        return toResponseDTO(saved);
    }

    @Override
    public ChildResponseDTO updateChild(int id, ChildRequestDTO dto) {
        Child child = childRepository.findById(id)
                .orElseThrow(() -> new ChildNotFoundException("No child found with id " + id));
        child.setFullName(dto.getFullName());
        child.setBirthDate(dto.getBirthDate());
        child.setGender(dto.getGender());
        child.setDiagnosis(dto.getDiagnosis());
        Child updated = childRepository.save(child);
        return toResponseDTO(updated);
    }

    @Override
    public ChildResponseDTO getChildById(int id) {
        Child child = childRepository.findById(id)
                .orElseThrow(() -> new ChildNotFoundException("No child found with id " + id));
        return toResponseDTO(child);
    }

    @Override
    public List<ChildResponseDTO> getChildrenByParentId(int parentId) {
        if (!userRepository.existsById(parentId)) {
            throw new ParentNotFoundException("No parent found with id " + parentId);
        }
        return childRepository.findByParentId(parentId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteChild(int id) {
        Child child = childRepository.findById(id)
                .orElseThrow(() -> new ChildNotFoundException("No child found with id " + id));
        childRepository.delete(child);
    }

    /**
     * Convertit un Child en ChildResponseDTO.
     */
    private ChildResponseDTO toResponseDTO(Child child) {
        ChildResponseDTO dto = modelMapper.map(child, ChildResponseDTO.class);
        dto.setParentId(child.getParent().getId());
        return dto;
    }
}
