package com.pixelpro.orders.dto;

import java.math.BigDecimal;

public record OrderItemDto(
        Long id,
        Long productId,
        Short quantity,
        BigDecimal unitPrice
) {
}

