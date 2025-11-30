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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final MpPaymentService mpPaymentService;

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
        // PASO 1: VALIDAR REGLAS DE NEGOCIO
        validateBusinessRules(request);

        // PASO 2: BUSCAR CLIENTE
        CustomerEntity customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        // PASO 3: VALIDAR Y OBTENER DIRECCIÓN DE ENVÍO
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

        // PASO 4: PROCESAR ITEMS Y VALIDAR STOCK
        List<OrderItemEntity> orderItems = new ArrayList<>();
        List<PreferenceItemRequest> mpItems = new ArrayList<>(); // <--- Lista para Mercado Pago
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

            // Actualizar stock (Reducir stock para ambos casos)
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

            // --- CONSTRUIR ITEM PARA MERCADO PAGO ---
            // Si es pago online, agregamos a la lista
            if (request.paymentMethod() == PaymentMethod.MERCADO_PAGO) {
                mpItems.add(PreferenceItemRequest.builder()
                        .id(String.valueOf(product.getId()))
                        .title(product.getName())
                        .description(product.getDescription())
                        .categoryId(product.getCategory().getName())
                        .quantity(cartItem.quantity())
                        .unitPrice(product.getPrice()) // Precio unitario real
                        .currencyId("PEN")
                        .pictureUrl("https://bettina-tactile-ogrishly.ngrok-free.dev" + product.getImageUrl())
                        .build());
            }
        }

        // 4. AGREGAR COSTO DE ENVÍO A MP (Si aplica)
        if (request.paymentMethod() == PaymentMethod.MERCADO_PAGO && shippingCost.compareTo(BigDecimal.ZERO) > 0) {
            mpItems.add(PreferenceItemRequest.builder()
                    .title("Costo de Envío")
                    .quantity(1)
                    .unitPrice(shippingCost)
                    .currencyId("PEN")
                    .build());
        }

        // PASO 5: CONSTRUIR ORDEN
        BigDecimal total = subtotal.add(shippingCost);
        String orderCode = generateOrderCode();

        // Determinar el estado inicial según el method de pago
        OrderStatus initialStatus = (request.paymentMethod() == PaymentMethod.MERCADO_PAGO)
                ? OrderStatus.PENDIENTE
                : OrderStatus.CONFIRMADO;

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

        // Establecer la relación bidireccional con items
        orderItems.forEach(item -> item.setOrder(order));
        // Guardamos primero para tener el ID
        OrderEntity savedOrder = orderRepository.save(order);

        // PASO 6: MANEJAR FLUJO SEGÚN METHOD DE PAGO
        if (request.paymentMethod() == PaymentMethod.MERCADO_PAGO) {
            // FLUJO MERCADO PAGO: Crear preferencia y NO crear pago ni invoice aún
            // El webhook de MP se encargará de confirmar el pago y generar la invoice
            // Creamos la preferencia en Mercado Pago
            try {
                // Llamar a MP pasando la LISTA DE ITEMS y el ID de la orden
                String externalRef = String.valueOf(savedOrder.getId());
                System.out.println("DEBUG MP ITEMS: " + mpItems.size());
                mpItems.forEach(i -> System.out.println(" - " + i.getTitle() + ": " + i.getUnitPrice()));
                String preferenceId = mpPaymentService.createPreference(mpItems, externalRef);

                return new CheckoutResponseDto(savedOrder.getId(), savedOrder.getCode(), preferenceId);
            } catch (Exception e) {
                // Rollback manual si falla MP (aunque @Transactional debería cubrirlo, es bueno para logs)
                rollbackStock(orderItems);
                throw new ConflictException("Error al procesar el pago con Mercado Pago: " + e.getMessage());
            }

        } else {
            // FLUJO PAGO EFECTIVO: Confirmar reserva inmediatamente
            PaymentEntity payment = PaymentEntity.builder()
                    .amount(total)
                    .currency(CurrencyCode.PEN)
                    .method(request.paymentMethod())
                    .status(PaymentStatus.PENDIENTE) // El pago se confirmará al entregar
                    .transactionId(UUID.randomUUID().toString())
                    .order(order)
                    .build();
            savedOrder.setPayments(List.of(payment));
            orderRepository.save(savedOrder);

            // Retornar respuesta sin preferenceId
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

    /**
     * Revierte el stock de productos en caso de error en el proceso de checkout
     */
    private void rollbackStock(List<OrderItemEntity> orderItems) {
        for (OrderItemEntity item : orderItems) {
            ProductEntity product = item.getProduct();
            product.setQtyStock(product.getQtyStock() + item.getQuantity());
            productRepository.save(product);
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