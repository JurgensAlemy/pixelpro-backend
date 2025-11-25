package com.pixelpro.customers.mapper;

import com.pixelpro.customers.dto.AddressCreateDto;
import com.pixelpro.customers.dto.AddressDto;
import com.pixelpro.customers.entity.AddressEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    AddressDto toDto(AddressEntity entity);

    AddressEntity toEntity(AddressCreateDto dto);
}

