package com.pixelpro.orders.mapper;

import com.pixelpro.billing.mapper.InvoiceMapper;
import com.pixelpro.billing.mapper.PaymentMapper;
import com.pixelpro.customers.mapper.AddressMapper;
import com.pixelpro.customers.mapper.CustomerMapper;
import com.pixelpro.orders.dto.OrderDto;
import com.pixelpro.orders.entity.OrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
        uses = {OrderItemMapper.class,
                CustomerMapper.class,
                AddressMapper.class,
                InvoiceMapper.class,
                PaymentMapper.class})
public interface OrderMapper {

    @Mapping(target = "status", expression = "java(entity.getStatus().name())")
    @Mapping(target = "deliveryType", expression = "java(entity.getDeliveryType().name())")
    @Mapping(target = "customer", source = "customer")
    @Mapping(target = "address", source = "shippingAddress")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "invoice", source = "invoice")
    @Mapping(target = "payments", source = "payments")
    OrderDto toDto(OrderEntity entity);
}

