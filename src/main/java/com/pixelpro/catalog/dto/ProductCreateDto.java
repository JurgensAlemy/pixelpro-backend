package com.pixelpro.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Datos para crear un nuevo producto")
public record ProductCreateDto(
        @Schema(description = "Código SKU único del producto", example = "LAP-HP-001", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "SKU is required")
        String sku,

        @Schema(description = "Nombre del producto", example = "Laptop HP Pavilion 14", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Product name is required")
        String name,

        @Schema(description = "Modelo del producto", example = "Pavilion 14-dv2000", nullable = true)
        String model,

        @Schema(description = "Descripción detallada del producto", example = "Laptop ultradelgada con pantalla de 14 pulgadas...", nullable = true)
        String description,

        @Schema(description = "Precio del producto", example = "899.99", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        BigDecimal price,

        @Schema(description = "URL de la imagen principal del producto", example = "https://example.com/products/hp-pavilion.jpg", nullable = true)
        String imageUrl,

        @Schema(description = "Estado del producto", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"}, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Status is required")
        String status,

        @Schema(description = "Cantidad inicial en inventario", example = "50", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Quantity in stock is required")
        @Min(value = 0, message = "Quantity in stock cannot be negative")
        Integer qtyStock,

        @Schema(description = "IDs de las categorías a las que pertenece el producto (mínimo 1)", example = "[1, 3]", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "At least one category is required")
        List<Long> categoryIds
) {
}
