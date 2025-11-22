package com.pixelpro.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Información de un producto más vendido")
public record TopProductDto(
        @Schema(description = "ID del producto", example = "1")
        Long id,

        @Schema(description = "Nombre del producto", example = "Mouse Logitech G502")
        String name,

        @Schema(description = "Cantidad total vendida", example = "125")
        Long qtySold,

        @Schema(description = "Ingresos generados", example = "15625.00")
        BigDecimal revenue
) {
}

