package com.pixelpro.catalog.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProductAdminCreateRequest(
        @NotBlank
        @Size(max = 120)
        String name,

        @NotBlank
        String description,

        List<Long> categoryIds,

        List<ProductImageAdminRequest> images
) {
}
