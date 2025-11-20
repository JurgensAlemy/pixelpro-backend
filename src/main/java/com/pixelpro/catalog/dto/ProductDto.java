package com.pixelpro.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Producto del catálogo")
public record ProductDto(
        @Schema(description = "ID único del producto", example = "1")
        Long id,

        @Schema(description = "Código SKU único del producto", example = "LAP-DEL-001")
        String sku,

        @Schema(description = "Nombre del producto", example = "Laptop Dell Inspiron 15")
        String name,

        @Schema(description = "Modelo del producto", example = "Inspiron 15 3000")
        String model,

        @Schema(description = "Descripción detallada del producto", example = "Laptop con procesador Intel Core i5...")
        String description,

        @Schema(description = "Precio del producto", example = "799.99")
        BigDecimal price,

        @Schema(description = "URL de la imagen del producto", example = "https://example.com/images/laptop.jpg")
        String imageUrl,

        @Schema(description = "Estado del producto", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
        String status,

        @Schema(description = "Cantidad disponible en inventario", example = "25")
        int qtyStock,

        @Schema(description = "Lista de categorías asignadas al producto")
        List<CategoryDto> categories,

        @Schema(description = "Fecha de creación del producto", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt,

        @Schema(description = "Fecha de última actualización del producto", example = "2024-01-15T10:30:00")
        LocalDateTime updatedAt
) {
}
