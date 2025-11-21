package com.pixelpro.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Información del pago asociado a la orden")
public record PaymentDto(
        @Schema(description = "ID del pago", example = "1")
        Long id,

        @Schema(description = "Monto del pago", example = "450.00")
        BigDecimal amount,

        @Schema(description = "Código de moneda", example = "PEN")
        String currency,

        @Schema(description = "Método de pago", example = "CREDIT_CARD")
        String method,

        @Schema(description = "Estado del pago", example = "COMPLETED")
        String status,

        @Schema(description = "ID de transacción de la pasarela", example = "TXN-123456789")
        String transactionId,

        @Schema(description = "Fecha y hora del pago")
        LocalDateTime paidAt
) {
}

