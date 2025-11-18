package com.pixelpro.inventory.mapper;

import com.pixelpro.inventory.dto.InventoryDto;
import com.pixelpro.inventory.entity.InventoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(target = "productId", expression = "java(entity.getProduct() != null ? entity.getProduct().getId() : null)")
    InventoryDto toDto(InventoryEntity entity);

    @Mapping(target = "product", ignore = true)
    InventoryEntity toEntity(InventoryDto dto);
}

