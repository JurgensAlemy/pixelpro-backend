package com.pixelpro.orders.service;

import com.pixelpro.orders.dto.CheckoutRequestDto;
import com.pixelpro.orders.dto.CheckoutResponseDto;
import com.pixelpro.orders.dto.OrderDto;
import com.pixelpro.orders.entity.enums.DeliveryType;
import com.pixelpro.orders.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    /**
     * Obtiene todas las órdenes con paginación y filtros opcionales
     *
     * @param search       Término de búsqueda (código de orden, nombre o apellido del cliente)
     * @param status       Estado de la orden (filtro exacto, opcional)
     * @param deliveryType Tipo de entrega (filtro exacto, opcional)
     * @param pageable     Configuración de paginación
     * @return Página de órdenes que coinciden con los filtros
     */
    Page<OrderDto> getAllOrders(String search, OrderStatus status, DeliveryType deliveryType, Pageable pageable);

    /**
     * Obtiene una orden por su ID
     *
     * @param id ID de la orden
     * @return DTO de la orden con toda la información anidada
     */
    OrderDto getOrderById(Long id);

    /**
     * Actualiza el estado de una orden
     *
     * @param id        ID de la orden
     * @param newStatus Nuevo estado
     * @return DTO de la orden actualizada
     */
    OrderDto updateStatus(Long id, OrderStatus newStatus);

    /**
     * Obtiene las órdenes de un cliente específico por su email (para Store)
     *
     * @param email    Email del cliente autenticado
     * @param status   Estado de la orden (opcional)
     * @param pageable Configuración de paginación
     * @return Página de órdenes del cliente
     */
    Page<OrderDto> getMyOrders(String email, OrderStatus status, Pageable pageable);

    /**
     * Procesa el checkout y crea una orden según el method de pago elegido.
     * Si es MERCADO_PAGO: crea la preferencia y devuelve el ID para redirección.
     * Si es PAGO_EFECTIVO: confirma la reserva directamente.
     *
     * @param email   Email del cliente autenticado
     * @param request Datos del checkout (items, dirección, method de pago, etc.)
     * @return DTO de respuesta con ID de orden, código y preferenceId (si aplica)
     */
    CheckoutResponseDto processCheckout(String email, CheckoutRequestDto request);
}

