package com.pixelpro.billing.mapper;

import com.pixelpro.billing.dto.PaymentDto;
import com.pixelpro.billing.entity.PaymentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "currency", expression = "java(entity.getCurrency().name())")
    @Mapping(target = "method", expression = "java(entity.getMethod().name())")
    @Mapping(target = "status", expression = "java(entity.getStatus().name())")
    PaymentDto toDto(PaymentEntity entity);
}

