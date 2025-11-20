package com.pixelpro.catalog.mapper;

import com.pixelpro.catalog.dto.CategoryCreateDto;
import com.pixelpro.catalog.dto.CategoryDto;
import com.pixelpro.catalog.dto.CategoryUpdateDto;
import com.pixelpro.catalog.entity.CategoryEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "parentCategoryId", source = "parentCategory.id")
    CategoryDto toDto(CategoryEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentCategory", ignore = true)
    @Mapping(target = "products", ignore = true)
    CategoryEntity toEntity(CategoryCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentCategory", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(CategoryUpdateDto dto, @MappingTarget CategoryEntity entity);
}
