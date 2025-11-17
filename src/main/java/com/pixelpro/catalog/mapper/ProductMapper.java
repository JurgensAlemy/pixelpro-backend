package com.pixelpro.catalog.mapper;

import com.pixelpro.catalog.dto.ProductDto;
import com.pixelpro.catalog.entity.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "categoryIds", expression = "java(entity.getCategories() != null ? entity.getCategories().stream().map(c -> c.getId()).toList() : null)")
    ProductDto toDto(ProductEntity entity);

    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "inventory", ignore = true)
    ProductEntity toEntity(ProductDto dto);
}

