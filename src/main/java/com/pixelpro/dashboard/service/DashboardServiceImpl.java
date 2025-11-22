package com.pixelpro.dashboard.service;

import com.pixelpro.catalog.repository.ProductRepository;
import com.pixelpro.customers.repository.CustomerRepository;
import com.pixelpro.dashboard.dto.ChartDataPointDto;
import com.pixelpro.dashboard.dto.DashboardStatsDto;
import com.pixelpro.dashboard.dto.TopProductDto;
import com.pixelpro.dashboard.projection.TopProductProjection;
import com.pixelpro.orders.dto.OrderDto;
import com.pixelpro.orders.entity.OrderEntity;
import com.pixelpro.orders.entity.enums.OrderStatus;
import com.pixelpro.orders.mapper.OrderMapper;
import com.pixelpro.orders.repository.OrderItemRepository;
import com.pixelpro.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderMapper orderMapper;

    private static final DateTimeFormatter CHART_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM");
    private static final int CHART_DAYS = 14;
    private static final int TOP_PRODUCTS_LIMIT = 5;
    private static final int LATEST_ORDERS_LIMIT = 5;

    /**
     * Estados de orden que se consideran como "ventas confirmadas/pagadas".
     * Excluye PENDIENTE (no pagada) y CANCELADO (anulada).
     */
    private static final List<OrderStatus> REVENUE_STATUSES = List.of(
            OrderStatus.CONFIRMADO,
            OrderStatus.PREPARANDO,
            OrderStatus.ENVIADO,
            OrderStatus.ENTREGADO
    );

    @Override
    public DashboardStatsDto getStats() {
        log.info("Calculando estadísticas del dashboard");

        // 1. Contadores simples
        Long totalOrders = orderRepository.count();
        Long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDIENTE);
        Long completedOrders = orderRepository.countByStatus(OrderStatus.ENTREGADO);
        Long totalProducts = productRepository.count();
        Long totalCustomers = customerRepository.count();

        // 2. Ingresos totales (órdenes confirmadas, preparando, enviadas y entregadas)
        BigDecimal totalRevenue = orderRepository.sumTotalByStatuses(REVENUE_STATUSES);
        // COALESCE en la query garantiza que nunca sea null, pero por seguridad:
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        // 3. Datos para el gráfico de ventas (últimos 14 días)
        List<ChartDataPointDto> salesChartData = getSalesChartData();

        // 4. Top 5 productos más vendidos
        List<TopProductDto> topProducts = getTopProducts();

        // 5. Últimas 5 órdenes
        List<OrderDto> latestOrders = getLatestOrders();

        log.info("Estadísticas calculadas: {} órdenes, {} clientes, ingresos: {}",
                totalOrders, totalCustomers, totalRevenue);

        return new DashboardStatsDto(
                totalOrders,
                pendingOrders,
                completedOrders,
                totalRevenue,
                totalProducts,
                totalCustomers,
                salesChartData,
                topProducts,
                latestOrders
        );
    }

    /**
     * Obtiene los datos del gráfico de ventas para los últimos N días
     * Rellena con ceros los días sin ventas
     */
    private List<ChartDataPointDto> getSalesChartData() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(CHART_DAYS - 1).withHour(0).withMinute(0).withSecond(0);

        // Obtener ventas desde la BD (todas las órdenes pagadas/confirmadas)
        List<Object[]> salesData = orderRepository.getSalesByDateRange(
                REVENUE_STATUSES,
                startDate,
                endDate
        );

        // Crear un mapa para acceso rápido por fecha
        Map<LocalDate, BigDecimal> salesMap = new HashMap<>();
        for (Object[] row : salesData) {
            LocalDate date = (LocalDate) row[0];
            BigDecimal total = (BigDecimal) row[1]; // JPA SUM() retorna BigDecimal
            salesMap.put(date, total != null ? total : BigDecimal.ZERO);
        }

        // Generar lista completa de los últimos N días (rellenando vacíos con 0)
        List<ChartDataPointDto> chartData = new ArrayList<>();
        LocalDate currentDate = startDate.toLocalDate();
        LocalDate end = endDate.toLocalDate();

        while (!currentDate.isAfter(end)) {
            String label = currentDate.format(CHART_DATE_FORMATTER);
            BigDecimal value = salesMap.getOrDefault(currentDate, BigDecimal.ZERO);
            chartData.add(new ChartDataPointDto(label, value));
            currentDate = currentDate.plusDays(1);
        }

        return chartData;
    }

    /**
     * Obtiene los N productos más vendidos (basado en cantidad)
     * Considera todas las órdenes pagadas/confirmadas
     */
    private List<TopProductDto> getTopProducts() {
        List<TopProductProjection> projections = orderItemRepository.findTopSellingProducts(
                REVENUE_STATUSES,
                PageRequest.of(0, TOP_PRODUCTS_LIMIT)
        );

        return projections.stream()
                .map(p -> new TopProductDto(
                        p.getProductId(),
                        p.getProductName(),
                        p.getQtySold(),
                        p.getRevenue() != null ? p.getRevenue() : BigDecimal.ZERO
                ))
                .toList();
    }

    /**
     * Obtiene las últimas N órdenes creadas en el sistema
     * Ordena por fecha de creación descendente
     */
    private List<OrderDto> getLatestOrders() {
        PageRequest pageRequest = PageRequest.of(
                0,
                LATEST_ORDERS_LIMIT,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<OrderEntity> ordersPage = orderRepository.findAll(pageRequest);

        return ordersPage.getContent().stream()
                .map(entity -> {
                    // Forzar la carga de colecciones lazy para evitar LazyInitializationException
                    forceLoadLazyCollections(entity);
                    return orderMapper.toDto(entity);
                })
                .toList();
    }

    /**
     * Método helper para inicializar los Proxies de Hibernate
     * Evita LazyInitializationException al acceder a relaciones lazy fuera del contexto transaccional
     */
    private void forceLoadLazyCollections(OrderEntity entity) {
        if (entity.getCustomer() != null) {
            entity.getCustomer().getFirstName();
        }
        if (entity.getShippingAddress() != null) {
            entity.getShippingAddress().getAddressLine();
        }
        if (entity.getItems() != null) {
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
            entity.getPayments().forEach(payment -> payment.getAmount());
        }
    }
}

