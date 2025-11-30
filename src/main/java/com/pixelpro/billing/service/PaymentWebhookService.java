package com.pixelpro.billing.service;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.pixelpro.billing.entity.InvoiceEntity;
import com.pixelpro.billing.entity.PaymentEntity;
import com.pixelpro.billing.entity.enums.*;
import com.pixelpro.billing.repository.InvoiceRepository;
import com.pixelpro.billing.repository.PaymentRepository;
import com.pixelpro.orders.entity.OrderEntity;
import com.pixelpro.orders.entity.enums.OrderStatus;
import com.pixelpro.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    @Transactional
    public void processPaymentNotification(String paymentId) {
        try {
            // 1. Consultar a Mercado Pago el estado real del pago
            PaymentClient client = new PaymentClient();
            Payment payment = client.get(Long.parseLong(paymentId));

            log.info("Procesando pago MP ID: {} | Status: {}", paymentId, payment.getStatus());

            // 2. Verificar si está aprobado
            if ("approved".equals(payment.getStatus())) {
                handleApprovedPayment(payment);
            } else {
                log.warn("Pago no aprobado o pendiente. Status: {}", payment.getStatus());
            }

        } catch (MPApiException apiException) {
            // MANEJO ROBUSTO DE ERRORES
            if (apiException.getStatusCode() == 404) {
                log.warn("Webhook recibido con ID de pago no encontrado en MP: {} (Probablemente una notificación de prueba)", paymentId);
            } else {
                log.error("Error de API Mercado Pago al consultar pago {}: {}", paymentId, apiException.getMessage());
            }
        } catch (MPException mpException) {
            log.error("Error de conexión/SDK Mercado Pago: {}", mpException.getMessage());
        } catch (Exception e) {
            log.error("Error procesando webhook de MP", e);
            // No lanzamos excepción para que MP no siga reintentando infinitamente si es un error lógico nuestro
        }
    }

    private void handleApprovedPayment(Payment mpPayment) {
        // 1. Obtener nuestra Order ID desde external_reference
        String externalRef = mpPayment.getExternalReference();

        // Validación extra por si es una prueba manual sin referencia
        if (externalRef == null || externalRef.isEmpty()) {
            log.warn("Pago aprobado sin external_reference (Order ID). No se puede vincular. ID Pago: {}", mpPayment.getId());
            return;
        }

        long orderId;
        try {
            orderId = Long.parseLong(externalRef);
        } catch (NumberFormatException e) {
            log.error("External reference inválido (no es un ID numérico): {}", externalRef);
            return;
        }

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order no encontrado " + orderId));

        // 2. Idempotencia: Si ya está confirmada, no hacer nada
        if (order.getStatus() == OrderStatus.CONFIRMADO || order.getStatus() == OrderStatus.PREPARANDO) {
            log.info("La orden {} ya estaba confirmada. Ignorando evento.", order.getCode());
            return;
        }

        // 3. Crear entidad Payment real
        PaymentEntity paymentEntity = PaymentEntity.builder()
                .amount(mpPayment.getTransactionAmount())
                .currency(CurrencyCode.PEN) // O mapear desde mpPayment.getCurrencyId()
                .method(PaymentMethod.TARJETA) // Simplificado, o mapear mpPayment.getPaymentMethodId()
                .status(PaymentStatus.CONFIRMADO)
                .transactionId(mpPayment.getId().toString())
                .paidAt(mpPayment.getDateApproved() != null ? mpPayment.getDateApproved().toLocalDateTime() : LocalDateTime.now())
                .order(order)
                .build();
        paymentRepository.save(paymentEntity);

        // 4. Crear factura electrónica (simulada, pero vinculada a la orden)
        InvoiceEntity invoice = InvoiceEntity.builder()
                .type(InvoiceType.BOLETA)
                .serie("F001")
                .number(String.format("%08d", order.getId()))
                .issuedAt(LocalDateTime.now())
                .totalAmount(order.getTotal())
                .currency(CurrencyCode.PEN)
                .status(InvoiceStatus.EMITIDO)
                .hashValue(UUID.randomUUID().toString())
                .documentUrl("https://pixelpro.com/invoices/" + order.getCode() + ".pdf")
                .order(order)
                .build();
        invoiceRepository.save(invoice);

        // 5. Actualizar Orden
        order.setStatus(OrderStatus.CONFIRMADO);
        log.info("Pago APROBADO. Actualizando orden {} a CONFIRMADO.", order.getCode());

        orderRepository.save(order);
    }
}