package com.pixelpro.orders.mapper;

import com.pixelpro.orders.dto.OrderDto;
import com.pixelpro.orders.entity.OrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {

    @Mapping(target = "customerId", expression = "java(entity.getCustomer() != null ? entity.getCustomer().getId() : null)")
    @Mapping(target = "shippingAddressId", expression = "java(entity.getShippingAddress() != null ? entity.getShippingAddress().getId() : null)")
    OrderDto toDto(OrderEntity entity);
}

