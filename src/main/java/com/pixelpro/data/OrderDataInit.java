package com.pixelpro.data;

import com.pixelpro.billing.entity.InvoiceEntity;
import com.pixelpro.billing.entity.PaymentEntity;
import com.pixelpro.billing.entity.enums.*;
import com.pixelpro.catalog.entity.ProductEntity;
import com.pixelpro.catalog.repository.ProductRepository;
import com.pixelpro.customers.entity.AddressEntity;
import com.pixelpro.customers.entity.CustomerEntity;
import com.pixelpro.customers.repository.CustomerRepository;
import com.pixelpro.orders.entity.OrderEntity;
import com.pixelpro.orders.entity.OrderItemEntity;
import com.pixelpro.orders.entity.enums.DeliveryType;
import com.pixelpro.orders.entity.enums.OrderStatus;
import com.pixelpro.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Order(3)
@RequiredArgsConstructor
public class OrderDataInit implements CommandLineRunner {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (orderRepository.count() > 0) {
            System.out.println("⚠️ Órdenes ya inicializadas. Omitiendo carga...");
            return;
        }

        System.out.println("🚀 Cargando Órdenes (Últimos 14 días)...");

        List<CustomerEntity> customers = customerRepository.findAll();
        List<ProductEntity> products = productRepository.findAll();

        if (customers.isEmpty() || products.isEmpty()) return;

        List<OrderEntity> ordersToSave = new ArrayList<>();
        Random random = new Random();

        for (CustomerEntity customer : customers) {
            int ordersCount = random.nextInt(3) + 1;

            for (int i = 0; i < ordersCount; i++) {
                int scenario = random.nextInt(10);
                ordersToSave.add(createOrderScenario(customer, products, random, scenario));
            }
        }

        orderRepository.saveAll(ordersToSave);
        System.out.println("✅ Carga completa: " + ordersToSave.size() + " órdenes creadas.");
    }

    private OrderEntity createOrderScenario(CustomerEntity customer, List<ProductEntity> products, Random random, int scenario) {
        // --- CORRECCIÓN DE FECHAS ---
        // 1. Generamos días atrás entre 0 y 13 (para estar dentro de los 14 días)
        int daysAgo = random.nextInt(14);

        // 2. Generamos horas atrás (mínimo 1 hora para dar margen a la factura)
        // Esto evita que si sale "hoy", la factura (hoy + 5min) caiga en el futuro.
        int hoursAgo = random.nextInt(20) + 1;

        LocalDateTime date = LocalDateTime.now()
                .minusDays(daysAgo)
                .minusHours(hoursAgo); // Siempre restamos al menos 1 hora respecto a "ahora"

        // Configurar Envío
        boolean isDelivery = random.nextBoolean();
        DeliveryType deliveryType = isDelivery ? DeliveryType.A_DOMICILIO : DeliveryType.RECOJO_EN_TIENDA;
        AddressEntity address = (isDelivery && !customer.getAddresses().isEmpty())
                ? customer.getAddresses().get(random.nextInt(customer.getAddresses().size()))
                : null;

        if (address == null) deliveryType = DeliveryType.RECOJO_EN_TIENDA;
        BigDecimal shippingCost = (deliveryType == DeliveryType.A_DOMICILIO) ? new BigDecimal("15.00") : BigDecimal.ZERO;

        // Determinar Estados
        OrderStatus orderStatus;
        PaymentStatus paymentStatus;
        boolean generateInvoice;

        if (scenario == 0 || scenario == 1) {
            // Fallo (20%)
            orderStatus = OrderStatus.CANCELADO;
            paymentStatus = PaymentStatus.RECHAZADO;
            generateInvoice = false;
        } else if (scenario == 2) {
            // Pendiente (10%)
            orderStatus = OrderStatus.PENDIENTE;
            paymentStatus = PaymentStatus.PENDIENTE;
            generateInvoice = false;
        } else {
            // Éxito (70%)
            orderStatus = getRandomSuccessStatus(random);
            paymentStatus = PaymentStatus.CONFIRMADO;
            generateInvoice = true;
        }

        // Construir Orden
        OrderEntity order = OrderEntity.builder()
                .code("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .status(orderStatus)
                .deliveryType(deliveryType)
                .customer(customer)
                .shippingAddress(address)
                .shippingCost(shippingCost)
                .discount(BigDecimal.ZERO)
                .build();
        order.setCreatedAt(date);

        // Items
        List<OrderItemEntity> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        List<ProductEntity> shuffled = new ArrayList<>(products);
        Collections.shuffle(shuffled);

        int itemCount = random.nextInt(3) + 1;
        for (int k = 0; k < itemCount; k++) {
            ProductEntity p = shuffled.get(k);
            short qty = (short) (random.nextInt(2) + 1);
            OrderItemEntity item = OrderItemEntity.builder()
                    .quantity(qty)
                    .unitPrice(p.getPrice())
                    .product(p)
                    .order(order)
                    .build();
            items.add(item);
            subtotal = subtotal.add(p.getPrice().multiply(BigDecimal.valueOf(qty)));
        }
        order.setItems(items);
        order.setSubtotal(subtotal);
        order.setTotal(subtotal.add(shippingCost));

        // Pago
        PaymentEntity payment = PaymentEntity.builder()
                .amount(order.getTotal())
                .currency(CurrencyCode.PEN)
                .method(getRandomPaymentMethod(random))
                .status(paymentStatus)
                .transactionId(UUID.randomUUID().toString())
                .order(order)
                .build();

        if (paymentStatus == PaymentStatus.CONFIRMADO) {
            // Pago ocurre 2 mins después de la orden
            payment.setPaidAt(date.plusMinutes(2));
        }

        order.setPayments(new ArrayList<>(List.of(payment)));

        // Factura
        if (generateInvoice) {
            InvoiceEntity invoice = InvoiceEntity.builder()
                    .type(InvoiceType.BOLETA)
                    .serie("B001")
                    .number(String.format("%08d", random.nextInt(999999)))
                    // Factura 5 mins después de la orden.
                    // Como 'date' tiene al menos 1 hora de antigüedad, esto SIEMPRE es pasado.
                    .issuedAt(date.plusMinutes(5))
                    .totalAmount(order.getTotal())
                    .currency(CurrencyCode.PEN)
                    .status(InvoiceStatus.EMITIDO)
                    .hashValue(UUID.randomUUID().toString())
                    .documentUrl("https://pixelpro-cdn.com/invoices/" + order.getCode() + ".pdf")
                    .order(order)
                    .build();
            order.setInvoice(invoice);
        }

        return order;
    }

    private OrderStatus getRandomSuccessStatus(Random random) {
        OrderStatus[] successStatuses = {OrderStatus.CONFIRMADO, OrderStatus.PREPARANDO, OrderStatus.ENVIADO, OrderStatus.ENTREGADO};
        return successStatuses[random.nextInt(successStatuses.length)];
    }

    private PaymentMethod getRandomPaymentMethod(Random random) {
        return PaymentMethod.values()[random.nextInt(PaymentMethod.values().length)];
    }
}