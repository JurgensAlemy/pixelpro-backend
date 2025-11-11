package com.pixelpro.attributes.service;

import com.pixelpro.attributes.dto.AttributeValueRequest;
import com.pixelpro.attributes.dto.AttributeValueResponse;

import java.util.List;

public interface AttributeValueService {
    AttributeValueResponse create(Long attributeId, AttributeValueRequest request);

    AttributeValueResponse update(Long id, AttributeValueRequest request);

    void delete(Long id);

    List<AttributeValueResponse> getByAttributeId(Long attributeId);
}
