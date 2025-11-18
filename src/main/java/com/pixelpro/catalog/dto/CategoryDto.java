package com.pixelpro.catalog.dto;
public record CategoryDto(
        Long id,
        String name,
        Long parentCategoryId
) {
}
