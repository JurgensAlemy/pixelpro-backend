package com.pixelpro.orders.repository;

import com.pixelpro.dashboard.projection.TopProductProjection;
import com.pixelpro.orders.entity.OrderItemEntity;
import com.pixelpro.orders.entity.enums.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {

    /**
     * Obtiene los productos más vendidos basado en cantidad vendida
     * Considera órdenes con estados pagados/confirmados (excluye PENDIENTE y CANCELADO)
     *
     * @param statuses Lista de estados de orden a considerar
     * @param pageable Configuración de paginación (usar para limitar a Top N)
     * @return Lista de productos con sus estadísticas de ventas
     */
    @Query("SELECT i.product.id as productId, " +
            "i.product.name as productName, " +
            "SUM(i.quantity) as qtySold, " +
            "SUM(i.unitPrice * i.quantity) as revenue " +
            "FROM OrderItemEntity i " +
            "WHERE i.order.status IN :statuses " +
            "GROUP BY i.product.id, i.product.name " +
            "ORDER BY SUM(i.quantity) DESC")
    List<TopProductProjection> findTopSellingProducts(
            @Param("statuses") List<OrderStatus> statuses,
            Pageable pageable
    );
}

