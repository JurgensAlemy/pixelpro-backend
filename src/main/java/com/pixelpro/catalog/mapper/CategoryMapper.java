package com.pixelpro.catalog.mapper;

import com.pixelpro.catalog.dto.CategoryDto;
import com.pixelpro.catalog.entity.CategoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "parentCategoryId", expression = "java(entity.getParentCategory() != null ? entity.getParentCategory().getId() : null)")
    CategoryDto toDto(CategoryEntity entity);

    @Mapping(target = "parentCategory", ignore = true)
    @Mapping(target = "products", ignore = true)
    CategoryEntity toEntity(CategoryDto dto);
}
