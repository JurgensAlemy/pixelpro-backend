package com.pixelpro.orders.dto;

import com.pixelpro.orders.entity.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DTO para actualizar el estado de una orden")
public record OrderStatusUpdateDto(
        @NotNull(message = "El estado es obligatorio")
        @Schema(description = "Nuevo estado de la orden", example = "CONFIRMADO")
        OrderStatus status
) {
}

