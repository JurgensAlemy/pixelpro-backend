package com.pixelpro.dashboard.service;

import com.pixelpro.dashboard.dto.DashboardStatsDto;

/**
 * Servicio para obtener estadísticas agregadas del dashboard administrativo
 */
public interface DashboardService {

    /**
     * Obtiene todas las estadísticas del dashboard incluyendo:
     * - Contadores de órdenes, productos y clientes
     * - Ingresos totales
     * - Datos para el gráfico de ventas (últimos 14 días)
     * - Top 5 productos más vendidos
     *
     * @return DTO con todas las estadísticas agregadas
     */
    DashboardStatsDto getStats();
}

