package com.pixelpro.attributes.mapper;

import com.pixelpro.attributes.dto.AttributeRequest;
import com.pixelpro.attributes.dto.AttributeResponse;
import com.pixelpro.attributes.entity.AttributeEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = AttributeValueMapper.class)
public interface AttributeMapper {

    AttributeEntity toEntity(AttributeRequest dto);

    //    @Mapping(target = "values", source = "values")
    AttributeResponse toResponse(AttributeEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(AttributeRequest dto, @MappingTarget AttributeEntity entity);
}
