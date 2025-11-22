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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

    // Inyectamos EntityManager para manipular la BD directamente y saltarnos el Auditing
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        if (orderRepository.count() > 0) {
            System.out.println("⚠️ Órdenes ya inicializadas. Omitiendo carga...");
            return;
        }

        System.out.println("🚀 Cargando Órdenes Históricas (Últimos 14 días)...");

        List<CustomerEntity> customers = customerRepository.findAll();
        List<ProductEntity> products = productRepository.findAll();

        if (customers.isEmpty() || products.isEmpty()) return;

        Random random = new Random();
        int totalOrders = 0;

        // Procesamos una por una para poder aplicar el "hack" de fechas inmediatamente
        for (CustomerEntity customer : customers) {
            // Entre 1 y 3 órdenes por cliente para variedad
            int ordersCount = random.nextInt(3) + 1;

            for (int i = 0; i < ordersCount; i++) {
                int scenario = random.nextInt(10);
                createAndHackOrder(customer, products, random, scenario);
                totalOrders++;
            }
        }

        System.out.println("✅ Carga completa: " + totalOrders + " órdenes creadas con fechas distribuidas.");
    }

    private void createAndHackOrder(CustomerEntity customer, List<ProductEntity> products, Random random, int scenario) {
        // --- 1. GENERACIÓN DE FECHA RETROACTIVA ---
        // Generamos una fecha aleatoria dentro de los últimos 14 días
        int daysAgo = random.nextInt(14);
        int hoursAgo = random.nextInt(20) + 1;

        LocalDateTime date = LocalDateTime.now()
                .minusDays(daysAgo)
                .minusHours(hoursAgo);

        // --- 2. LÓGICA DE NEGOCIO (Construcción de Entidades) ---
        boolean isDelivery = random.nextBoolean();
        DeliveryType deliveryType = isDelivery ? DeliveryType.A_DOMICILIO : DeliveryType.RECOJO_EN_TIENDA;
        AddressEntity address = (isDelivery && !customer.getAddresses().isEmpty())
                ? customer.getAddresses().get(random.nextInt(customer.getAddresses().size()))
                : null;

        if (address == null) deliveryType = DeliveryType.RECOJO_EN_TIENDA;
        BigDecimal shippingCost = (deliveryType == DeliveryType.A_DOMICILIO) ? new BigDecimal("15.00") : BigDecimal.ZERO;

        OrderStatus orderStatus;
        PaymentStatus paymentStatus;
        boolean generateInvoice;

        if (scenario == 0 || scenario == 1) { // 20% Fallo/Cancelado
            orderStatus = OrderStatus.CANCELADO;
            paymentStatus = PaymentStatus.RECHAZADO;
            generateInvoice = false;
        } else if (scenario == 2) { // 10% Pendiente
            orderStatus = OrderStatus.PENDIENTE;
            paymentStatus = PaymentStatus.PENDIENTE;
            generateInvoice = false;
        } else { // 70% Éxito
            orderStatus = getRandomSuccessStatus(random);
            paymentStatus = PaymentStatus.CONFIRMADO;
            generateInvoice = true;
        }

        // 2.1 Construir Orden
        OrderEntity order = OrderEntity.builder()
                .code("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .status(orderStatus)
                .deliveryType(deliveryType)
                .customer(customer)
                .shippingAddress(address)
                .shippingCost(shippingCost)
                .discount(BigDecimal.ZERO)
                .build();
        // Nota: order.setCreatedAt(date) sería ignorado por JPA aquí, por eso usamos el hack más abajo.

        // 2.2 Items
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

        // 2.3 Pago
        PaymentEntity payment = PaymentEntity.builder()
                .amount(order.getTotal())
                .currency(CurrencyCode.PEN)
                .method(getRandomPaymentMethod(random))
                .status(paymentStatus)
                .transactionId(UUID.randomUUID().toString())
                .order(order)
                .build();

        if (paymentStatus == PaymentStatus.CONFIRMADO) {
            // Usamos LocalDateTime directo (2 minutos después de la fecha generada)
            payment.setPaidAt(date.plusMinutes(2));
        }
        order.setPayments(new ArrayList<>(List.of(payment)));

        // 2.4 Factura
        if (generateInvoice) {
            InvoiceEntity invoice = InvoiceEntity.builder()
                    .type(InvoiceType.BOLETA)
                    .serie("B001")
                    .number(String.format("%08d", random.nextInt(999999)))
                    // Usamos LocalDateTime directo (5 minutos después de la fecha generada)
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

        // --- 3. PERSISTENCIA Y HACK DE FECHAS (TIME TRAVEL) ---

        // A. Guardar con JPA (Pondrá created_at = HOY por el @CreatedDate)
        orderRepository.save(order);

        // B. UPDATE Nativo para sobrescribir la fecha en la tabla 'orders'
        entityManager.createNativeQuery("UPDATE orders SET created_at = :date WHERE id = :id")
                .setParameter("date", date)
                .setParameter("id", order.getId())
                .executeUpdate();

        // C. UPDATE Nativo para sobrescribir la fecha en 'invoices' (si existe)
        if (order.getInvoice() != null) {
            entityManager.createNativeQuery("UPDATE invoices SET created_at = :date WHERE id = :id")
                    .setParameter("date", date)
                    .setParameter("id", order.getInvoice().getId())
                    .executeUpdate();
        }

        // D. UPDATE Nativo para sobrescribir la fecha en 'payments'
        for (PaymentEntity p : order.getPayments()) {
            entityManager.createNativeQuery("UPDATE payments SET created_at = :date WHERE id = :id")
                    .setParameter("date", date)
                    .setParameter("id", p.getId())
                    .executeUpdate();
        }
    }

    private OrderStatus getRandomSuccessStatus(Random random) {
        OrderStatus[] successStatuses = {OrderStatus.CONFIRMADO, OrderStatus.PREPARANDO, OrderStatus.ENVIADO, OrderStatus.ENTREGADO};
        return successStatuses[random.nextInt(successStatuses.length)];
    }

    private PaymentMethod getRandomPaymentMethod(Random random) {
        return PaymentMethod.values()[random.nextInt(PaymentMethod.values().length)];
    }
}