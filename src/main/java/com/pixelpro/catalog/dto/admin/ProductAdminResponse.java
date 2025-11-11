package com.pixelpro.catalog.dto.admin;

import java.util.List;

public record ProductAdminResponse(
        Long id,
        String name,
        String description,
        List<CategoryAdminResponse> categories,
        List<ProductImageAdminResponse> images
) {
}
