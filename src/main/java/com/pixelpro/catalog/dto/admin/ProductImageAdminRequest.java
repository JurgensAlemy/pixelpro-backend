package com.pixelpro.catalog.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductImageAdminRequest(
        @NotBlank String url,
        @NotNull @Positive Byte position
) {
}
