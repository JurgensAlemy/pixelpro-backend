package com.pixelpro.orders.service;

import com.mercadopago.client.preference.PreferenceItemRequest;
import com.pixelpro.billing.entity.PaymentEntity;
import com.pixelpro.billing.entity.enums.CurrencyCode;
import com.pixelpro.billing.entity.enums.PaymentMethod;
import com.pixelpro.billing.entity.enums.PaymentStatus;
import com.pixelpro.billing.service.MpPaymentService;
import com.pixelpro.catalog.entity.ProductEntity;
import com.pixelpro.catalog.repository.ProductRepository;
import com.pixelpro.common.exception.ConflictException;
import com.pixelpro.common.exception.ResourceNotFoundException;
import com.pixelpro.customers.entity.AddressEntity;
import com.pixelpro.customers.entity.CustomerEntity;
import com.pixelpro.customers.repository.AddressRepository;
import com.pixelpro.customers.repository.CustomerRepository;
import com.pixelpro.orders.dto.CheckoutRequestDto;
import com.pixelpro.orders.dto.CheckoutResponseDto;
import com.pixelpro.orders.dto.OrderDto;
import com.pixelpro.orders.entity.OrderEntity;
import com.pixelpro.orders.entity.OrderItemEntity;
import com.pixelpro.orders.entity.enums.DeliveryType;
import com.pixelpro.orders.entity.enums.OrderStatus;
import com.pixelpro.orders.mapper.OrderMapper;
import com.pixelpro.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 1. Regla general: Solo lectura (optimización)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final MpPaymentService mpPaymentService;

    @Value("${app.base-url}")
    private String baseUrl;

    // Constantes para el MVP
    private static final BigDecimal SHIPPING_COST_DELIVERY = new BigDecimal("15.00");
    private static final BigDecimal SHIPPING_COST_PICKUP = BigDecimal.ZERO;

    @Override
    public Page<OrderDto> getAllOrders(String search, OrderStatus status, DeliveryType deliveryType, Pageable pageable) {
        // Normalizar el search (null o vacío se convierte en null)
        String normalizedSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        // Usar el method de filtrado combinado del repository
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

        forceLoadLazyCollections(entity);
        return orderMapper.toDto(entity);
    }

    @Override
    @Transactional // 2. Excepción: Este method SÍ escribe en BD
    public OrderDto updateStatus(Long id, OrderStatus newStatus) {
        OrderEntity entity = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + id));

        validateStatusTransition(entity.getStatus(), newStatus);

        entity.setStatus(newStatus);
        OrderEntity updated = orderRepository.save(entity);

        forceLoadLazyCollections(updated); // <--- Código reutilizado
        return orderMapper.toDto(updated);
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
    public CheckoutResponseDto processCheckout(String email, CheckoutRequestDto request) {
        // 1. VALIDACIONES INICIALES
        validateBusinessRules(request);
        CustomerEntity customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + email));

        // 2. DIRECCIÓN Y COSTO DE ENVÍO
        AddressEntity shippingAddress = null;
        BigDecimal shippingCost;

        if (request.deliveryType() == DeliveryType.A_DOMICILIO) {
            if (request.addressId() == null) {
                throw new ConflictException("La dirección es obligatoria para delivery");
            }
            shippingAddress = addressRepository.findById(request.addressId())
                    .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada"));

            if (!shippingAddress.getCustomer().getId().equals(customer.getId())) {
                throw new ConflictException("La dirección no pertenece al cliente");
            }
            shippingCost = SHIPPING_COST_DELIVERY;
        } else {
            shippingCost = SHIPPING_COST_PICKUP;
        }

        // 3. PROCESAR ITEMS (STOCK Y ENTIDADES)
        List<OrderItemEntity> orderItems = new ArrayList<>();
        // Lista para MP (Solo se llenará si el method es MP)
        List<PreferenceItemRequest> mpItems = (request.paymentMethod() == PaymentMethod.MERCADO_PAGO)
                ? new ArrayList<>() : null;

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CheckoutRequestDto.CartItemDto cartItem : request.items()) {
            ProductEntity product = productRepository.findById(cartItem.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto ID " + cartItem.productId() + " no encontrado"));

            // Validar Stock
            if (product.getQtyStock() < cartItem.quantity()) {
                throw new ConflictException("Stock insuficiente para: " + product.getName());
            }

            // Reducir Stock
            product.setQtyStock(product.getQtyStock() - cartItem.quantity());
            productRepository.save(product);

            // Crear Item de Orden
            BigDecimal itemTotal = product.getPrice().multiply(new BigDecimal(cartItem.quantity()));
            OrderItemEntity orderItem = OrderItemEntity.builder()
                    .product(product)
                    .quantity(cartItem.quantity().shortValue())
                    .unitPrice(product.getPrice())
                    .build();
            orderItems.add(orderItem);
            subtotal = subtotal.add(itemTotal);

            // --- SOLO SI ES MP: Construir Item de Preferencia ---
            if (mpItems != null) {
                // Asegurar URL absoluta para la imagen
                String imgUrl = product.getImageUrl();
                if (imgUrl != null && !imgUrl.startsWith("http")) {
                    imgUrl = baseUrl + imgUrl;
                }

                mpItems.add(PreferenceItemRequest.builder()
                        .id(String.valueOf(product.getId()))
                        .title(product.getName())
                        .description(product.getDescription() != null ?
                                product.getDescription().substring(0, Math.min(product.getDescription().length(), 200)) : "")
                        .categoryId(product.getCategory().getName())
                        .quantity(cartItem.quantity())
                        .unitPrice(product.getPrice())
                        .currencyId("PEN")
                        .pictureUrl(imgUrl)
                        .build());
            }
        }

        // 4. CALCULAR TOTALES
        BigDecimal total = subtotal.add(shippingCost);
        String orderCode = generateOrderCode();

        // Estado Inicial
        OrderStatus initialStatus = (request.paymentMethod() == PaymentMethod.MERCADO_PAGO)
                ? OrderStatus.PENDIENTE : OrderStatus.CONFIRMADO;

        // 5. CREAR ORDEN BASE
        OrderEntity order = OrderEntity.builder()
                .code(orderCode)
                .status(initialStatus)
                .deliveryType(request.deliveryType())
                .customer(customer)
                .shippingAddress(shippingAddress)
                .subtotal(subtotal)
                .shippingCost(shippingCost)
                .discount(BigDecimal.ZERO)
                .total(total)
                .items(orderItems)
                .build();

        // Relación bidireccional
        orderItems.forEach(item -> item.setOrder(order));

        // Guardar Orden (Aquí ya tenemos ID)
        OrderEntity savedOrder = orderRepository.save(order);

        // 6. BIFURCACIÓN DE FLUJO DE PAGO
        if (request.paymentMethod() == PaymentMethod.MERCADO_PAGO) {
            // --- FLUJO A: MERCADO PAGO ---
            try {
                // Agregar envío a MP si existe
                if (shippingCost.compareTo(BigDecimal.ZERO) > 0 && mpItems != null) {
                    mpItems.add(PreferenceItemRequest.builder()
                            .title("Costo de Envío")
                            .quantity(1)
                            .unitPrice(shippingCost)
                            .currencyId("PEN")
                            .build());
                }

                String externalRef = String.valueOf(savedOrder.getId());
                // Llamada segura (mpItems nunca será null aquí por la lógica del if)
                String preferenceId = mpPaymentService.createPreference(mpItems, externalRef);

                return new CheckoutResponseDto(savedOrder.getId(), savedOrder.getCode(), preferenceId);

            } catch (Exception e) {
                // Rollback manual si falla la API externa (aunque Transactional haría rollback general,
                // esto nos permite loguear o manejar errores específicos)
                throw new RuntimeException("Error al conectar con Mercado Pago: " + e.getMessage(), e);
            }

        } else {
            // --- FLUJO B: PAGO EFECTIVO (RECOJO EN TIENDA) ---

            // Crear Payment Entity "PENDIENTE" (A pagar en mostrador)
            PaymentEntity payment = PaymentEntity.builder()
                    .amount(total)
                    .currency(CurrencyCode.PEN)
                    .method(request.paymentMethod())
                    .status(PaymentStatus.PENDIENTE)
                    .transactionId("CASH-" + orderCode) // ID interno para efectivo
                    .order(savedOrder)
                    .build();

            // No creamos Invoice todavía (se emite al pagar en tienda)
            // O si prefieres crearla PENDIENTE:
            /*
            InvoiceEntity invoice = InvoiceEntity.builder() ... status(PENDIENTE) ...
            savedOrder.setInvoice(invoice);
            */

            // Guardamos el pago (Cascade debería funcionar si Order tiene CascadeType.ALL en payments)
            // Pero como payments es OneToMany, lo mejor es guardar el payment directamente
            // O agregarlo a la lista de la orden y volver a guardar la orden

            // Opción segura: Guardar orden con la lista actualizada (si cascade está bien)
            // O guardar el payment repository si lo inyectaste.
            // Asumimos cascade en OrderEntity:
            List<PaymentEntity> payments = new ArrayList<>();
            payments.add(payment);
            savedOrder.setPayments(payments);

            orderRepository.save(savedOrder);

            return new CheckoutResponseDto(savedOrder.getId(), savedOrder.getCode(), null);
        }
    }

    /**
     * Genera un código único de orden
     */
    private String generateOrderCode() {
        return "ORD-" + System.currentTimeMillis();
    }

    /**
     * Verifica las reglas de negocio del checkout
     */
    private void validateBusinessRules(CheckoutRequestDto request) {
        // Regla: Pago contra entrega solo disponible para recojo en tienda
        if (request.deliveryType() == DeliveryType.A_DOMICILIO
                && request.paymentMethod() == PaymentMethod.PAGO_EFECTIVO) {
            throw new ConflictException(
                    "El pago contra entrega solo está disponible para recojo en tienda. " +
                            "Para delivery, debe pagar online."
            );
        }
    }
    
//    private void rollbackStock(List<OrderItemEntity> orderItems) {
//        for (OrderItemEntity item : orderItems) {
//            ProductEntity product = item.getProduct();
//            product.setQtyStock(product.getQtyStock() + item.getQuantity());
//            productRepository.save(product);
//        }
//    }

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

    /**
     * Method helper para inicializar los Proxies de Hibernate
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
}