package com.pixelpro.catalog.mapper;


import com.pixelpro.catalog.dto.admin.ProductAdminCreateRequest;
import com.pixelpro.catalog.dto.admin.ProductAdminResponse;
import com.pixelpro.catalog.dto.admin.ProductAdminUpdateRequest;
import com.pixelpro.catalog.dto.web.ProductWebResponse;
import com.pixelpro.catalog.entity.CategoryEntity;
import com.pixelpro.catalog.entity.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        uses = {CategoryMapper.class, ProductImageMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductMapper {

    // Admin
    ProductEntity toEntity(ProductAdminCreateRequest request);

    ProductAdminResponse toAdminResponse(ProductEntity entity);

    // PATCH merge update
    void updateEntityFromRequest(ProductAdminUpdateRequest request, @MappingTarget ProductEntity entity);

    // Web
    @Mapping(target = "categoryNames", expression = "java(mapCategoryNames(entity))")
    ProductWebResponse toWebResponse(ProductEntity entity);

    List<ProductWebResponse> toWebResponseList(List<ProductEntity> entities);

    default List<String> mapCategoryNames(ProductEntity entity) {
        if (entity.getCategories() == null) return List.of();
        return entity.getCategories().stream()
                .map(CategoryEntity::getName)
                .collect(Collectors.toList());
    }

}
