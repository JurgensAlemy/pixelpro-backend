package com.pixelpro.catalog.dto.admin;

import java.util.List;

public record ProductAdminUpdateRequest(
        String name,
        String description,
        List<Long> categoryIds,
        List<ProductImageAdminRequest> images
) {
}
