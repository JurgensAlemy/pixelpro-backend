package com.pixelpro.orders.repository;

import com.pixelpro.orders.entity.OrderEntity;
import com.pixelpro.orders.entity.enums.DeliveryType;
import com.pixelpro.orders.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    /**
     * Busca órdenes con filtros combinados
     *
     * @param search       Término de búsqueda global (código, nombre o apellido del cliente)
     * @param status       Estado de la orden (puede ser null para no filtrar)
     * @param deliveryType Tipo de entrega (puede ser null para no filtrar)
     * @param pageable     Configuración de paginación
     * @return Página de órdenes que coinciden con los filtros
     */
    @Query("SELECT o FROM OrderEntity o WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            " LOWER(o.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            " LOWER(o.customer.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            " LOWER(o.customer.lastName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:status IS NULL OR o.status = :status) " +
            "AND (:deliveryType IS NULL OR o.deliveryType = :deliveryType)")
    Page<OrderEntity> findAllWithFilters(
            @Param("search") String search,
            @Param("status") OrderStatus status,
            @Param("deliveryType") DeliveryType deliveryType,
            Pageable pageable
    );
}
