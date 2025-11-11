package com.pixelpro.catalog.mapper;

import com.pixelpro.catalog.dto.admin.CategoryAdminCreateRequest;
import com.pixelpro.catalog.dto.admin.CategoryAdminResponse;
import com.pixelpro.catalog.dto.admin.CategoryAdminUpdateRequest;
import com.pixelpro.catalog.dto.web.CategoryWebResponse;
import com.pixelpro.catalog.entity.CategoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CategoryMapper {

    // Admin
    CategoryEntity toEntity(CategoryAdminCreateRequest request);

    CategoryAdminResponse toAdminResponse(CategoryEntity entity);

    List<CategoryAdminResponse> toAdminResponseList(List<CategoryEntity> entities);

    // Merge update (PATCH)
    void updateEntityFromRequest(CategoryAdminUpdateRequest request, @MappingTarget CategoryEntity entity);

    // Web
    CategoryWebResponse toWebResponse(CategoryEntity entity);

    List<CategoryWebResponse> toWebResponseList(List<CategoryEntity> entities);
}
