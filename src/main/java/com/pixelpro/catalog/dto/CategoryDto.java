package com.pixelpro.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Categoría de producto")
public record CategoryDto(
        @Schema(description = "ID único de la categoría", example = "1")
        Long id,

        @Schema(description = "Nombre de la categoría", example = "Electrónica")
        String name,

        @Schema(description = "ID de la categoría padre (para jerarquías)", example = "null", nullable = true)
        Long parentCategoryId,

        @Schema(description = "Fecha de creación", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt,

        @Schema(description = "Fecha de última actualización", example = "2024-01-15T10:30:00")
        LocalDateTime updatedAt
) {
}
