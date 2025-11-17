package com.pixelpro.orders.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderDto(
        Long id,
        String code,
        String status,
        String deliveryType,
        Long customerId,
        Long shippingAddressId,
        BigDecimal subtotal,
        BigDecimal shippingCost,
        BigDecimal discount,
        BigDecimal total,
        List<OrderItemDto> items
) {
}

