package com.pixelpro.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Información de la factura/boleta asociada a la orden")
public record InvoiceDto(
        @Schema(description = "ID de la factura", example = "1")
        Long id,

        @Schema(description = "Tipo de comprobante", example = "BOLETA")
        String type,

        @Schema(description = "Serie del comprobante", example = "B001")
        String serie,

        @Schema(description = "Número del comprobante", example = "00001234")
        String number,

        @Schema(description = "Fecha de emisión")
        LocalDateTime issuedAt,

        @Schema(description = "Monto total", example = "450.00")
        BigDecimal totalAmount,

        @Schema(description = "Código de moneda", example = "PEN")
        String currency,

        @Schema(description = "Estado de la factura", example = "EMITIDO")
        String status,

        @Schema(description = "URL del documento PDF/XML")
        String documentUrl
) {
}

