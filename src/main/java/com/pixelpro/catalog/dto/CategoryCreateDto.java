package com.pixelpro.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Datos para crear una nueva categoría")
public record CategoryCreateDto(
        @Schema(description = "Nombre de la categoría", example = "Laptops", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Category name is required")
        String name,

        @Schema(description = "ID de la categoría padre (opcional, para crear subcategorías)", example = "1", nullable = true)
        Long parentCategoryId
) {
}
