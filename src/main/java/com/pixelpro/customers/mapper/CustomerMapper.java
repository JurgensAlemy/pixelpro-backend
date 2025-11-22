package com.pixelpro.customers.mapper;

import com.pixelpro.customers.dto.CustomerDto;
import com.pixelpro.customers.dto.CustomerUpdateDto;
import com.pixelpro.customers.entity.CustomerEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {AddressMapper.class})
public interface CustomerMapper {

    @Mapping(target = "documentType", expression = "java(entity.getDocumentType().name())")
    @Mapping(target = "customerType", expression = "java(entity.getCustomerType().name())")
    CustomerDto toDto(CustomerEntity entity);

    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
            unmappedTargetPolicy = ReportingPolicy.IGNORE
    )
    void updateEntityFromDto(CustomerUpdateDto dto, @MappingTarget CustomerEntity entity);
}

