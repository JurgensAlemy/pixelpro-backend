package com.pixelpro.customers.mapper;

import com.pixelpro.customers.dto.CustomerDto;
import com.pixelpro.customers.entity.CustomerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "documentType", expression = "java(entity.getDocumentType().name())")
    CustomerDto toDto(CustomerEntity entity);
}

