package com.pixelpro.orders.mapper;

import com.pixelpro.orders.dto.OrderItemDto;
import com.pixelpro.orders.entity.OrderItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "productId", expression = "java(entity.getProduct() != null ? entity.getProduct().getId() : null)")
    @Mapping(target = "productName", expression = "java(entity.getProduct() != null ? entity.getProduct().getName() : null)")
    @Mapping(target = "productSku", expression = "java(entity.getProduct() != null ? entity.getProduct().getSku() : null)")
    @Mapping(target = "productImageUrl", expression = "java(entity.getProduct() != null ? entity.getProduct().getImageUrl() : null)")
    OrderItemDto toDto(OrderItemEntity entity);

    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", ignore = true)
    OrderItemEntity toEntity(OrderItemDto dto);
}

