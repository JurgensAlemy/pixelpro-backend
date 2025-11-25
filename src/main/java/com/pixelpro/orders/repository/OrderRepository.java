package com.pixelpro.orders.repository;

import com.pixelpro.orders.entity.OrderEntity;
import com.pixelpro.orders.entity.enums.DeliveryType;
import com.pixelpro.orders.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
            " LOWER(o.customer.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR" +
            " LOWER(o.customer.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:status IS NULL OR o.status = :status) " +
            "AND (:deliveryType IS NULL OR o.deliveryType = :deliveryType)")
    Page<OrderEntity> findAllWithFilters(
            @Param("search") String search,
            @Param("status") OrderStatus status,
            @Param("deliveryType") DeliveryType deliveryType,
            Pageable pageable
    );

    // ==================== MÉTODOS PARA DASHBOARD ====================

    /**
     * Cuenta órdenes por estado
     */
    long countByStatus(OrderStatus status);

    /**
     * Suma el total de órdenes por lista de estados
     *
     * @param statuses Lista de estados de las órdenes a sumar
     * @return Suma total o 0 si no hay órdenes (BigDecimal para preservar precisión)
     */
    @Query("SELECT COALESCE(SUM(o.total), 0) FROM OrderEntity o WHERE o.status IN :statuses")
    BigDecimal sumTotalByStatuses(@Param("statuses") List<OrderStatus> statuses);

    /**
     * Obtiene las ventas agrupadas por fecha en un rango específico
     *
     * @param statuses Lista de estados de orden a considerar
     * @param start    Fecha inicial
     * @param end      Fecha final
     * @return Lista de arrays [fecha, total] ordenados por fecha
     */
    @Query("SELECT CAST(o.createdAt AS LocalDate), SUM(o.total) " +
            "FROM OrderEntity o " +
            "WHERE o.status IN :statuses AND o.createdAt BETWEEN :start AND :end " +
            "GROUP BY CAST(o.createdAt AS LocalDate) " +
            "ORDER BY CAST(o.createdAt AS LocalDate) ASC")
    List<Object[]> getSalesByDateRange(
            @Param("statuses") List<OrderStatus> statuses,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // ==================== MÉTODOS PARA STORE (CLIENTES) ====================

    /**
     * Busca órdenes de un cliente específico por su email
     *
     * @param email    Email del cliente
     * @param pageable Configuración de paginación
     * @return Página de órdenes del cliente
     */
    Page<OrderEntity> findByCustomer_Email(String email, Pageable pageable);

    /**
     * Busca órdenes de un cliente específico por su email y estado
     *
     * @param email    Email del cliente
     * @param status   Estado de la orden
     * @param pageable Configuración de paginación
     * @return Página de órdenes del cliente con el estado especificado
     */
    Page<OrderEntity> findByCustomer_EmailAndStatus(String email, OrderStatus status, Pageable pageable);
}
