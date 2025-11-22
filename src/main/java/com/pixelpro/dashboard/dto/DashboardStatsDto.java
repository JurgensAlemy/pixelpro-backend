package com.pixelpro.dashboard.dto;

import com.pixelpro.orders.dto.OrderDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Estadísticas agregadas del dashboard administrativo")
public record DashboardStatsDto(
        @Schema(description = "Total de órdenes en el sistema", example = "1250")
        Long totalOrders,

        @Schema(description = "Órdenes pendientes de confirmación", example = "45")
        Long pendingOrders,

        @Schema(description = "Órdenes completadas (entregadas)", example = "980")
        Long completedOrders,

        @Schema(description = "Ingresos totales de órdenes entregadas", example = "125430.50")
        BigDecimal totalRevenue,

        @Schema(description = "Total de productos en catálogo", example = "350")
        Long totalProducts,

        @Schema(description = "Total de clientes registrados", example = "520")
        Long totalCustomers,

        @Schema(description = "Datos para el gráfico de ventas (últimos 14 días)")
        List<ChartDataPointDto> salesChartData,

        @Schema(description = "Top 5 productos más vendidos")
        List<TopProductDto> topProducts,

        @Schema(description = "Últimas 5 órdenes creadas en el sistema")
        List<OrderDto> latestOrders
) {
}

