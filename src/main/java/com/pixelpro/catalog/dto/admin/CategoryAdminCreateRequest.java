package com.pixelpro.catalog.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryAdminCreateRequest(
        @NotBlank
        @Size(max = 80)
        String name,

        Long parentId
) {
}
