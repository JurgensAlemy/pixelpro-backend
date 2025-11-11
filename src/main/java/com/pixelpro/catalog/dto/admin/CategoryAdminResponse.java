package com.pixelpro.catalog.dto.admin;

import java.util.List;

public record CategoryAdminResponse(
        Long id,
        String name,
        Long parentId,
        List<CategoryAdminResponse> subCategories
) {
}
