package com.pixelpro.orders.controller.admin;

import com.pixelpro.orders.dto.OrderDto;
import com.pixelpro.orders.dto.OrderStatusUpdateDto;
import com.pixelpro.orders.entity.enums.DeliveryType;
import com.pixelpro.orders.entity.enums.OrderStatus;
import com.pixelpro.orders.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@Tag(name = "Admin - Órdenes", description = "Gestión de órdenes del panel de administración")
public class AdminOrderController {

    private final OrderService orderService;

    @Operation(
            summary = "Listar todas las órdenes con filtros",
            description = "Obtiene un listado paginado de órdenes con capacidad de búsqueda y filtrado combinado. " +
                    "Permite buscar por código de orden, nombre o apellido del cliente (búsqueda global), " +
                    "y filtrar específicamente por estado de la orden y tipo de entrega."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de órdenes obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = Page.class))
            )
    })
    @GetMapping
    public ResponseEntity<Page<OrderDto>> getAllOrders(
            @Parameter(
                    description = "Término de búsqueda global: código de orden, nombre o apellido del cliente",
                    example = "ORD-2025-00001"
            )
            @RequestParam(required = false) String search,

            @Parameter(
                    description = "Filtrar por estado específico de la orden",
                    example = "CONFIRMADO"
            )
            @RequestParam(required = false) OrderStatus status,

            @Parameter(
                    description = "Filtrar por tipo de entrega",
                    example = "A_DOMICILIO"
            )
            @RequestParam(required = false) DeliveryType deliveryType,

            @Parameter(
                    description = "Parámetros de paginación y ordenamiento. Por defecto: página 0, tamaño 10, ordenado por createdAt DESC",
                    example = "page=0&size=10&sort=code,asc"
            )
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<OrderDto> orders = orderService.getAllOrders(search, status, deliveryType, pageable);
        return ResponseEntity.ok(orders);
    }

    @Operation(
            summary = "Obtener orden por ID",
            description = "Obtiene los detalles completos de una orden específica incluyendo items, cliente, dirección, factura y pagos"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Orden encontrada exitosamente",
                    content = @Content(schema = @Schema(implementation = OrderDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Orden no encontrada"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(
            @Parameter(description = "ID de la orden", required = true)
            @PathVariable Long id
    ) {
        OrderDto order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @Operation(
            summary = "Actualizar estado de una orden",
            description = "Permite actualizar el estado de una orden existente (PENDIENTE, CONFIRMADO, PREPARANDO, ENVIADO, ENTREGADO, CANCELADO)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = OrderDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Orden no encontrada"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Transición de estado no válida"
            )
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderDto> updateStatus(
            @Parameter(description = "ID de la orden", required = true)
            @PathVariable Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nuevo estado de la orden",
                    required = true
            )
            @Valid @RequestBody OrderStatusUpdateDto statusUpdate
    ) {
        OrderDto updated = orderService.updateStatus(id, statusUpdate.status());
        return ResponseEntity.ok(updated);
    }
}

