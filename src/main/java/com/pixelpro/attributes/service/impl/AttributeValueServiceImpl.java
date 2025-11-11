package com.pixelpro.attributes.service.impl;

import com.pixelpro.attributes.dto.AttributeValueRequest;
import com.pixelpro.attributes.dto.AttributeValueResponse;
import com.pixelpro.attributes.entity.AttributeEntity;
import com.pixelpro.attributes.entity.AttributeValueEntity;
import com.pixelpro.attributes.mapper.AttributeValueMapper;
import com.pixelpro.attributes.repository.AttributeRepository;
import com.pixelpro.attributes.repository.AttributeValueRepository;
import com.pixelpro.attributes.service.AttributeValueService;
import com.pixelpro.common.exception.ConflictException;
import com.pixelpro.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AttributeValueServiceImpl implements AttributeValueService {

    private final AttributeValueRepository valueRepository;
    private final AttributeRepository attributeRepository;
    private final AttributeValueMapper mapper;

    @Override
    public AttributeValueResponse create(Long attributeId, AttributeValueRequest request) {
        AttributeEntity attribute = attributeRepository.findById(attributeId)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute with ID " + attributeId + " not found"));

        if (valueRepository.existsByAttribute_IdAndAttributeValueNameIgnoreCase(attributeId, request.attributeValueName())) {
            throw new ConflictException("Value '" + request.attributeValueName() + "' already exists in attribute " + attribute.getName());
        }

        AttributeValueEntity entity = mapper.toEntity(request);
        entity.setAttribute(attribute);
        AttributeValueEntity saved = valueRepository.save(entity);

        return mapper.toResponse(saved);
    }

    @Override
    public AttributeValueResponse update(Long id, AttributeValueRequest request) {
        AttributeValueEntity entity = valueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attribute value with ID " + id + " not found"));

        Long attributeId = entity.getAttribute().getId();

        if (valueRepository.existsByAttribute_IdAndAttributeValueNameIgnoreCase(attributeId, request.attributeValueName())
                && !entity.getAttributeValueName().equalsIgnoreCase(request.attributeValueName())) {
            throw new ConflictException("Value '" + request.attributeValueName() + "' already exists in attribute " + entity.getAttribute().getName());
        }

        mapper.updateEntityFromDto(request, entity);
        AttributeValueEntity updated = valueRepository.save(entity);
        return mapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!valueRepository.existsById(id)) {
            throw new ResourceNotFoundException("Attribute value with ID " + id + " not found");
        }
        valueRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttributeValueResponse> getByAttributeId(Long attributeId) {
        if (!attributeRepository.existsById(attributeId)) {
            throw new ResourceNotFoundException("Attribute with ID " + attributeId + " not found");
        }

        return valueRepository.findByAttribute_Id(attributeId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
