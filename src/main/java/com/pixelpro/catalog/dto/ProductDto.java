package com.pixelpro.catalog.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductDto(
        Long id,
        String sku,
        String name,
        String model,
        String description,
        BigDecimal price,
        String imageUrl,
        String status,
        List<Long> categoryIds
) {
}

