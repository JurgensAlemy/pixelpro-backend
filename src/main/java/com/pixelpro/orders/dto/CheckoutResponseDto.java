package com.pixelpro.orders.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO de respuesta después de procesar el checkout")
public record CheckoutResponseDto(
        @Schema(description = "ID de la orden creada", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
        Long orderId,

        @Schema(description = "Código único de la orden", example = "ORD-1732819200000", requiredMode = Schema.RequiredMode.REQUIRED)
        String orderCode,

        @Schema(
                description = "ID de preferencia de Mercado Pago (solo para pagos online). " +
                        "Null cuando el método de pago es PAGO_EFECTIVO.",
                example = "1234567890-abcd-efgh-ijkl-123456789012",
                nullable = true
        )
        String preferenceId
) {
}

