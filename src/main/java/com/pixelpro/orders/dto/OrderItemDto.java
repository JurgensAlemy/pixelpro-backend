package com.pixelpro.orders.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Item de producto en la orden")
public record OrderItemDto(
        @Schema(description = "ID del item", example = "1")
        Long id,

        @Schema(description = "ID del producto", example = "10")
        Long productId,

        @Schema(description = "Nombre del producto", example = "Mouse Gamer Logitech G502")
        String productName,

        @Schema(description = "SKU del producto", example = "LG-G502-X")
        String productSku,

        @Schema(description = "URL de imagen del producto")
        String productImageUrl,

        @Schema(description = "Cantidad", example = "2")
        Short quantity,

        @Schema(description = "Precio unitario", example = "250.00")
        BigDecimal unitPrice
) {
}

