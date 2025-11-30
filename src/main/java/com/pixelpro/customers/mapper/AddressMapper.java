package com.pixelpro.customers.mapper;

import com.pixelpro.customers.dto.AddressCreateDto;
import com.pixelpro.customers.dto.AddressDto;
import com.pixelpro.customers.entity.AddressEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    AddressDto toDto(AddressEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    AddressEntity toEntity(AddressCreateDto dto);
}

