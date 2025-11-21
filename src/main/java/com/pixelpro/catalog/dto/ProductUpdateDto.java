package com.pixelpro.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Datos para actualizar un producto existente (todos los campos son opcionales)")
public record ProductUpdateDto(
        @Schema(description = "Nuevo nombre del producto", example = "Laptop HP Pavilion 14 Pro", nullable = true)
        String name,

        @Schema(description = "Nuevo modelo del producto", example = "Pavilion 14-dv2500", nullable = true)
        String model,

        @Schema(description = "Nueva descripción del producto", example = "Laptop ultradelgada mejorada...", nullable = true)
        String description,

        @Schema(description = "Nuevo precio del producto", example = "949.99", nullable = true)
        @Positive(message = "Price must be positive")
        BigDecimal price,

        @Schema(description = "Nueva URL de la imagen del producto", example = "https://example.com/products/hp-pavilion-v2.jpg", nullable = true)
        String imageUrl,

        @Schema(description = "Nuevo estado del producto", example = "INACTIVO", allowableValues = {"ACTIVO", "INACTIVO"}, nullable = true)
        String status,

        @Schema(description = "Nueva cantidad en inventario", example = "75", nullable = true)
        @Min(value = 0, message = "Quantity in stock cannot be negative")
        Integer qtyStock,

        @Schema(description = "Nuevo ID de la categoría del producto", example = "2", nullable = true)
        Long categoryId
) {
}
