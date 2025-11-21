package com.pixelpro.orders.service;

import com.pixelpro.billing.entity.PaymentEntity;
import com.pixelpro.common.exception.ResourceNotFoundException;
import com.pixelpro.orders.dto.OrderDto;
import com.pixelpro.orders.entity.OrderEntity;
import com.pixelpro.orders.entity.enums.DeliveryType;
import com.pixelpro.orders.entity.enums.OrderStatus;
import com.pixelpro.orders.mapper.OrderMapper;
import com.pixelpro.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 1. Regla general: Solo lectura (optimización)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    public Page<OrderDto> getAllOrders(String search, OrderStatus status, DeliveryType deliveryType, Pageable pageable) {
        // Normalizar el search (null o vacío se convierte en null)
        String normalizedSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        // Usar el método de filtrado combinado del repository
        Page<OrderEntity> orders = orderRepository.findAllWithFilters(normalizedSearch, status, deliveryType, pageable);

        // Mapeamos y forzamos la carga de colecciones lazy
        return orders.map(entity -> {
            forceLoadLazyCollections(entity);
            return orderMapper.toDto(entity);
        });
    }

    @Override
    public OrderDto getOrderById(Long id) {
        OrderEntity entity = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + id));

        forceLoadLazyCollections(entity); // <--- Código reutilizado
        return orderMapper.toDto(entity);
    }

    @Override
    @Transactional // 2. Excepción: Este método SÍ escribe en BD
    public OrderDto updateStatus(Long id, OrderStatus newStatus) {
        OrderEntity entity = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + id));

        validateStatusTransition(entity.getStatus(), newStatus);

        entity.setStatus(newStatus);
        OrderEntity updated = orderRepository.save(entity);

        forceLoadLazyCollections(updated); // <--- Código reutilizado
        return orderMapper.toDto(updated);
    }

    /**
     * Método helper para inicializar los Proxies de Hibernate
     * Centraliza la lógica de "hidratación" para evitar duplicidad.
     */
    private void forceLoadLazyCollections(OrderEntity entity) {
        if (entity.getCustomer() != null) {
            entity.getCustomer().getFirstName();
        }
        if (entity.getShippingAddress() != null) {
            entity.getShippingAddress().getAddressLine();
        }
        if (entity.getItems() != null) {
            // Inicializamos la colección y también accedemos al producto interno
            entity.getItems().forEach(item -> {
                if (item.getProduct() != null) {
                    item.getProduct().getName();
                }
            });
        }
        if (entity.getInvoice() != null) {
            entity.getInvoice().getSerie();
        }
        if (entity.getPayments() != null) {
            entity.getPayments().forEach(PaymentEntity::getAmount);
        }
    }

    /**
     * Valida la transición de estados según el flujo de negocio del Ecommerce.
     * Regla general: El flujo es progresivo y no se puede retroceder, excepto para cancelar.
     */
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // 1. Si el estado es el mismo, no hay nada que validar
        if (currentStatus == newStatus) {
            return;
        }

        // 2. Regla de Estados Finales: Una vez terminado, no se toca.
        if (currentStatus == OrderStatus.CANCELADO || currentStatus == OrderStatus.ENTREGADO) {
            throw new IllegalStateException(
                    String.format("La orden ya está finalizada (%s) y no se puede modificar.", currentStatus));
        }

        // 3. Máquina de Estados: Definimos qué caminos son válidos
        boolean isValid = switch (currentStatus) {
            case PENDIENTE ->
                // De Pendiente solo puede pasar a Confirmado (pagó) o Cancelado (se arrepintió)
                    (newStatus == OrderStatus.CONFIRMADO || newStatus == OrderStatus.CANCELADO);
            case CONFIRMADO ->
                // De Confirmado sigue Preparando (almacén) o Cancelado (reembolso)
                    (newStatus == OrderStatus.PREPARANDO || newStatus == OrderStatus.CANCELADO);
            case PREPARANDO ->
                // De Preparando sigue Enviado (courier) o Cancelado (restock necesario)
                    (newStatus == OrderStatus.ENVIADO || newStatus == OrderStatus.CANCELADO);
            case ENVIADO ->
                // De Enviado solo puede pasar a Entregado.
                // (Opcional: Se podría permitir Cancelado si el paquete se perdió, depende de tu regla)
                    (newStatus == OrderStatus.ENTREGADO || newStatus == OrderStatus.CANCELADO);
            default -> false;
        };

        if (!isValid) {
            throw new IllegalArgumentException(
                    String.format("Transición de estado inválida: No se puede pasar de %s a %s.",
                            currentStatus, newStatus));
        }
    }
}