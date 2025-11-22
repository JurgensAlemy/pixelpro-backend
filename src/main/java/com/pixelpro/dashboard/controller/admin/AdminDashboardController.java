package com.pixelpro.dashboard.controller.admin;

import com.pixelpro.dashboard.dto.DashboardStatsDto;
import com.pixelpro.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin - Dashboard", description = "Estadísticas y métricas del panel administrativo")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @Operation(
            summary = "Obtener estadísticas del dashboard",
            description = "Retorna todas las métricas agregadas necesarias para el dashboard administrativo: " +
                    "contadores de órdenes/productos/clientes, ingresos totales, gráfico de ventas (últimos 14 días), " +
                    "y los 5 productos más vendidos. Todas las consultas se realizan mediante agregaciones en base de datos " +
                    "para máxima performance."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estadísticas obtenidas exitosamente",
                    content = @Content(schema = @Schema(implementation = DashboardStatsDto.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error al calcular las estadísticas"
            )
    })
    @GetMapping
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        DashboardStatsDto stats = dashboardService.getStats();
        return ResponseEntity.ok(stats);
    }
}

