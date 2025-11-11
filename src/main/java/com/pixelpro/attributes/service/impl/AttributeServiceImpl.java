package com.pixelpro.attributes.service.impl;

import com.pixelpro.attributes.dto.AttributeRequest;
import com.pixelpro.attributes.dto.AttributeResponse;
import com.pixelpro.attributes.entity.AttributeEntity;
import com.pixelpro.attributes.mapper.AttributeMapper;
import com.pixelpro.attributes.repository.AttributeRepository;
import com.pixelpro.attributes.service.AttributeService;
import com.pixelpro.common.exception.ConflictException;
import com.pixelpro.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AttributeServiceImpl implements AttributeService {

    private final AttributeRepository attributeRepository;
    private final AttributeMapper attributeMapper;

    @Override
    public AttributeResponse create(AttributeRequest request) {
        if (attributeRepository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("Attribute with name '" + request.name() + "' already exists");
        }

        AttributeEntity entity = attributeMapper.toEntity(request);
        AttributeEntity saved = attributeRepository.save(entity);
        return attributeMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttributeResponse> getAll() {
        return attributeRepository.findAll()
                .stream()
                .map(attributeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AttributeResponse getById(Long id) {
        AttributeEntity entity = attributeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute with ID " + id + " not found"));
        return attributeMapper.toResponse(entity);
    }

    @Override
    public AttributeResponse update(Long id, AttributeRequest request) {
        AttributeEntity entity = attributeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute with ID " + id + " not found"));

        // Validar nombre duplicado
        if (attributeRepository.existsByNameIgnoreCase(request.name())
                && !entity.getName().equalsIgnoreCase(request.name())) {
            throw new ConflictException("Attribute with name '" + request.name() + "' already exists");
        }

        attributeMapper.updateEntityFromDto(request, entity);
        AttributeEntity updated = attributeRepository.save(entity);
        return attributeMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!attributeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Attribute with ID " + id + " not found");
        }
        attributeRepository.deleteById(id);
    }
}
