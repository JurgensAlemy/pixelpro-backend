package com.pixelpro.orders.controller.store;

import com.pixelpro.orders.dto.CheckoutRequestDto;
import com.pixelpro.orders.dto.CheckoutResponseDto;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Tag(name = "Store - Mis Pedidos", description = "API para que los clientes gestionen sus pedidos y realicen compras")
@RestController
@RequestMapping("/api/store")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class StoreOrderController {

    private final OrderService orderService;

    @Operation(
            summary = "Procesar checkout",
            description = "Crea un nuevo pedido procesando el carrito de compra. Valida stock, calcula totales. " +
                    "Si el método de pago es MERCADO_PAGO, retorna un preferenceId para redirección. " +
                    "Si es PAGO_EFECTIVO (solo para recojo en tienda), confirma la reserva directamente. " +
                    "REGLA: El pago contra entrega NO está disponible para delivery a domicilio."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Pedido creado exitosamente. Si paymentMethod=MERCADO_PAGO, incluye preferenceId.",
            content = @Content(schema = @Schema(implementation = CheckoutResponseDto.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos o regla de negocio violada " +
                    "(ej: pago contra entrega con delivery a domicilio)",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "Cliente, producto o dirección no encontrado",
            content = @Content
    )
    @ApiResponse(
            responseCode = "409",
            description = "Stock insuficiente, dirección no pertenece al cliente, o error al crear preferencia de Mercado Pago",
            content = @Content
    )
    @PostMapping("/orders")
    public ResponseEntity<CheckoutResponseDto> processCheckout(
            @Valid @RequestBody CheckoutRequestDto request,
            Principal principal
    ) {
        CheckoutResponseDto response = orderService.processCheckout(principal.getName(), request);
        return ResponseEntity.status(201).body(response);
    }

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
    @GetMapping("/account/orders")
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
    @GetMapping("/account/orders/{id}")
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

