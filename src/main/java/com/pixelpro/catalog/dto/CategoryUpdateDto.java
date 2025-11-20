package com.pixelpro.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Datos para actualizar una categoría existente")
public record CategoryUpdateDto(
        @Schema(description = "Nuevo nombre de la categoría", example = "Laptops Gaming", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Category name is required")
        String name,

        @Schema(description = "ID de la nueva categoría padre (null para categoría raíz)", example = "2", nullable = true)
        Long parentCategoryId
) {
}
