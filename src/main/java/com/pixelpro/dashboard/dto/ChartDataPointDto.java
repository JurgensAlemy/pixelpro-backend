package com.pixelpro.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Punto de datos para el gráfico de ventas")
public record ChartDataPointDto(
        @Schema(description = "Etiqueta (fecha)", example = "21/11")
        String label,

        @Schema(description = "Valor de ventas", example = "1250.50")
        BigDecimal value
) {
}

