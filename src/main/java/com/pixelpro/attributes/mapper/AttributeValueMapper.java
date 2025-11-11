package com.pixelpro.attributes.mapper;

import com.pixelpro.attributes.dto.AttributeValueRequest;
import com.pixelpro.attributes.dto.AttributeValueResponse;
import com.pixelpro.attributes.entity.AttributeValueEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AttributeValueMapper {

    AttributeValueEntity toEntity(AttributeValueRequest dto);

    AttributeValueResponse toResponse(AttributeValueEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(AttributeValueRequest dto, @MappingTarget AttributeValueEntity entity);
}
