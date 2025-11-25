package com.pixelpro.orders.service;

import com.pixelpro.billing.entity.InvoiceEntity;
import com.pixelpro.billing.entity.PaymentEntity;
import com.pixelpro.billing.entity.enums.CurrencyCode;
import com.pixelpro.billing.entity.enums.InvoiceStatus;
import com.pixelpro.billing.entity.enums.InvoiceType;
import com.pixelpro.billing.entity.enums.PaymentStatus;
import com.pixelpro.catalog.entity.ProductEntity;
import com.pixelpro.catalog.repository.ProductRepository;
import com.pixelpro.common.exception.ConflictException;
import com.pixelpro.common.exception.ResourceNotFoundException;
import com.pixelpro.customers.entity.AddressEntity;
import com.pixelpro.customers.entity.CustomerEntity;
import com.pixelpro.customers.repository.AddressRepository;
import com.pixelpro.customers.repository.CustomerRepository;
import com.pixelpro.orders.dto.CheckoutRequestDto;
import com.pixelpro.orders.dto.OrderDto;
import com.pixelpro.orders.entity.OrderEntity;
import com.pixelpro.orders.entity.OrderItemEntity;
import com.pixelpro.orders.entity.enums.DeliveryType;
import com.pixelpro.orders.entity.enums.OrderStatus;
import com.pixelpro.orders.mapper.OrderMapper;
import com.pixelpro.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 1. Regla general: Solo lectura (optimización)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;

    // Constantes para el MVP
    private static final BigDecimal SHIPPING_COST_DELIVERY = new BigDecimal("15.00");
    private static final BigDecimal SHIPPING_COST_PICKUP = BigDecimal.ZERO;

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

    @Override
    public Page<OrderDto> getMyOrders(String email, OrderStatus status, Pageable pageable) {
        Page<OrderEntity> orders;

        if (status != null) {
            orders = orderRepository.findByCustomer_EmailAndStatus(email, status, pageable);
        } else {
            orders = orderRepository.findByCustomer_Email(email, pageable);
        }

        return orders.map(entity -> {
            forceLoadLazyCollections(entity);
            return orderMapper.toDto(entity);
        });
    }

    @Override
    @Transactional
    public OrderDto processCheckout(String email, CheckoutRequestDto request) {
        // 1. BUSCAR CLIENTE
        CustomerEntity customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        // 2. VALIDAR Y OBTENER DIRECCIÓN DE ENVÍO
        AddressEntity shippingAddress = null;
        BigDecimal shippingCost;

        if (request.deliveryType() == DeliveryType.A_DOMICILIO) {
            // Validar que se proporcionó addressId
            if (request.addressId() == null) {
                throw new ConflictException("La dirección de envío es obligatoria para entregas a domicilio");
            }

            // Buscar dirección
            shippingAddress = addressRepository.findById(request.addressId())
                    .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada"));

            // Validar que la dirección pertenece al cliente
            if (!shippingAddress.getCustomer().getId().equals(customer.getId())) {
                throw new ResourceNotFoundException("Dirección no encontrada o no pertenece al cliente");
            }

            shippingCost = SHIPPING_COST_DELIVERY;
        } else {
            // RECOJO_EN_TIENDA
            shippingCost = SHIPPING_COST_PICKUP;
        }

        // 3. PROCESAR ITEMS Y VALIDAR STOCK
        List<OrderItemEntity> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CheckoutRequestDto.CartItemDto cartItem : request.items()) {
            // Buscar producto
            ProductEntity product = productRepository.findById(cartItem.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + cartItem.productId()));

            // Validar stock
            if (product.getQtyStock() < cartItem.quantity()) {
                throw new ConflictException("Sin stock suficiente para: " + product.getName() +
                        " (disponible: " + product.getQtyStock() + ", solicitado: " + cartItem.quantity() + ")");
            }

            // Actualizar stock
            product.setQtyStock(product.getQtyStock() - cartItem.quantity());
            productRepository.save(product);

            // Crear item de orden con precio actual
            BigDecimal itemTotal = product.getPrice().multiply(new BigDecimal(cartItem.quantity()));
            OrderItemEntity orderItem = OrderItemEntity.builder()
                    .product(product)
                    .quantity(cartItem.quantity().shortValue())
                    .unitPrice(product.getPrice())
                    .build();

            orderItems.add(orderItem);
            subtotal = subtotal.add(itemTotal);
        }

        // 4. CONSTRUIR ORDEN
        BigDecimal total = subtotal.add(shippingCost);
        String orderCode = generateOrderCode();

        OrderEntity order = OrderEntity.builder()
                .code(orderCode)
                .status(OrderStatus.CONFIRMADO) // MVP: siempre confirmado
                .deliveryType(request.deliveryType())
                .customer(customer)
                .shippingAddress(shippingAddress)
                .subtotal(subtotal)
                .shippingCost(shippingCost)
                .discount(BigDecimal.ZERO)
                .total(total)
                .items(orderItems)
                .build();

        // Establecer la relación bidireccional con items
        orderItems.forEach(item -> item.setOrder(order));

        // 5. SIMULAR PAGO (MVP)
        PaymentEntity payment = PaymentEntity.builder()
                .amount(total)
                .currency(CurrencyCode.PEN)
                .method(request.paymentMethod())
                .status(PaymentStatus.CONFIRMADO) // MVP: siempre confirmado
                .transactionId(UUID.randomUUID().toString())
                .paidAt(LocalDateTime.now())
                .order(order)
                .build();

        order.setPayments(List.of(payment));

        // 6. SIMULAR INVOICE (MVP)
        InvoiceEntity invoice = InvoiceEntity.builder()
                .type(InvoiceType.BOLETA) // MVP: siempre boleta
                .serie("B001")
                .number(String.format("%08d", System.currentTimeMillis() % 100000000))
                .issuedAt(LocalDateTime.now())
                .totalAmount(total)
                .currency(CurrencyCode.PEN)
                .status(InvoiceStatus.EMITIDO) // MVP: siempre emitido
                .hashValue(UUID.randomUUID().toString())
                .documentUrl("https://storage.example.com/invoices/" + orderCode + ".pdf")
                .order(order)
                .build();

        order.setInvoice(invoice);

        // 7. GUARDAR ORDEN (cascade guarda items, payment, invoice)
        OrderEntity savedOrder = orderRepository.save(order);

        // 8. FORZAR CARGA DE LAZY COLLECTIONS Y RETORNAR DTO
        forceLoadLazyCollections(savedOrder);
        return orderMapper.toDto(savedOrder);
    }

    /**
     * Genera un código único de orden
     */
    private String generateOrderCode() {
        return "ORD-" + System.currentTimeMillis();
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