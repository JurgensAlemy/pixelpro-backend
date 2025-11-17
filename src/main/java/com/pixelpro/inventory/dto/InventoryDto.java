package com.pixelpro.inventory.dto;

public record InventoryDto(
        Long id,
        int qtyStock,
        Long productId
) {
}

