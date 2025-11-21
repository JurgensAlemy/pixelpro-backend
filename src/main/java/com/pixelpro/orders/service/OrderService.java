package com.pixelpro.orders.service;

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
}

