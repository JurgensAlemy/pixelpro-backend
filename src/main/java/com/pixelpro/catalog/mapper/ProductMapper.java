package com.pixelpro.catalog.mapper;

import com.pixelpro.catalog.dto.ProductCreateDto;
import com.pixelpro.catalog.dto.ProductDto;
import com.pixelpro.catalog.dto.ProductUpdateDto;
import com.pixelpro.catalog.entity.ProductEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface ProductMapper {
    ProductDto toDto(ProductEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categories", ignore = true)
    ProductEntity toEntity(ProductCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(ProductUpdateDto dto, @MappingTarget ProductEntity entity);
}
