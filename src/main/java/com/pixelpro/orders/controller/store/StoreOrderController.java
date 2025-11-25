package com.pixelpro.orders.controller.store;

import com.pixelpro.orders.dto.OrderDto;
import com.pixelpro.orders.entity.enums.OrderStatus;
import com.pixelpro.orders.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Tag(name = "Store - Mis Pedidos", description = "API para que los clientes consulten sus pedidos")
@RestController
@RequestMapping("/api/store/account/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class StoreOrderController {

    private final OrderService orderService;

    @Operation(
            summary = "Obtener mis pedidos",
            description = "Lista todos los pedidos del cliente autenticado con paginación. " +
                    "Opcionalmente puede filtrar por estado (PENDIENTE, CONFIRMADO, PREPARANDO, ENVIADO, ENTREGADO, CANCELADO)."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lista de pedidos obtenida exitosamente",
            content = @Content(schema = @Schema(implementation = Page.class))
    )
    @GetMapping
    public ResponseEntity<Page<OrderDto>> getMyOrders(
            @Parameter(description = "Filtrar por estado del pedido")
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Principal principal
    ) {
        Page<OrderDto> orders = orderService.getMyOrders(principal.getName(), status, pageable);
        return ResponseEntity.ok(orders);
    }

    @Operation(
            summary = "Obtener detalle de mi pedido",
            description = "Obtiene la información completa de un pedido específico del cliente autenticado."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Pedido encontrado exitosamente",
            content = @Content(schema = @Schema(implementation = OrderDto.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Pedido no encontrado",
            content = @Content
    )
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(
            @Parameter(description = "ID del pedido")
            @PathVariable Long id,
            Principal principal
    ) {
        OrderDto order = orderService.getOrderById(id);

        // Validar que el pedido pertenece al cliente autenticado
        if (!order.customer().email().equals(principal.getName())) {
            throw new RuntimeException("No tienes permiso para ver este pedido");
        }

        return ResponseEntity.ok(order);
    }
}

