package com.pixelpro.billing.controller;

import com.pixelpro.billing.dto.WebhookNotificationDto;
import com.pixelpro.billing.service.PaymentWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Public - Webhooks", description = "Recepción de notificaciones de pasarelas de pago")
public class PaymentWebhookController {

    private final PaymentWebhookService webhookService;

    @Value("${mp.webhook.key}")
    private String webhookKey;

    @Operation(
            summary = "Recibir notificación de Mercado Pago",
            description = "Endpoint público (Webhook) que Mercado Pago llama cuando cambia el estado de un pago y valida firma de seguridad. " +
                    "Valida el pago con la API de MP y actualiza el estado de la orden localmente."
    )
    @PostMapping("/webhook")
    public ResponseEntity<String> receiveNotification(
            @RequestBody WebhookNotificationDto notification,
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestHeader(value = "x-request-id", required = false) String xRequestId
    ) {
        log.info(">>> ¡ALGUIEN TOCÓ LA PUERTA! Headers: Signature={}, RequestId={}", xSignature, xRequestId);
        // 1. Validacion de Seguridad (HMAC)
        if (xSignature == null || xRequestId == null || notification.data() == null) {
            // Si es una prueba simple desde postman sin headers, o falta info
            log.warn("Intento de webhook sin headers de seguridad o datos.");
            return ResponseEntity.badRequest().build();
        }
        // Extraer el ID del pago (es un record, se accede con .id())
        String paymentId = notification.data().id();

        if (!isValidSignature(xSignature, xRequestId, paymentId)) {
            log.error("Firma de Webhook inválida. Posible ataque o configuración errónea.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid Signature");
        }

        log.info("Webhook recibido: Type={}, Action={}, ID={}",
                notification.type(), notification.action(), paymentId);

        // 2. Filtrar solo eventos relevantes
        if ("payment".equals(notification.type()) || "payment.created".equals(notification.action())) {
            // Procesamos la lógica de negocio
            webhookService.processPaymentNotification(paymentId);
        }

        // Siempre responder 200 OK a MP para que deje de enviar la notificación
        return ResponseEntity.ok("OK");
    }

    /**
     * Valida la firma HMAC-SHA256 enviada por Mercado Pago
     */
    private boolean isValidSignature(String xSignature, String xRequestId, String dataId) {
        try {
            // Formato x-signature: "ts=...,v1=..."
            String[] parts = xSignature.split(",");
            String ts = null;
            String v1 = null;

            for (String part : parts) {
                if (part.startsWith("ts=")) ts = part.substring(3);
                if (part.startsWith("v1=")) v1 = part.substring(3);
            }

            if (ts == null || v1 == null) return false;

            // Construir el "Manifest" string según documentación de MP
            // id:[data.id];request-id:[x-request-id];ts:[ts];
            String manifest = String.format("id:%s;request-id:%s;ts:%s;", dataId, xRequestId, ts);

            // Calcular HMAC
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(webhookKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(secretKeySpec);
            byte[] hashBytes = hmac.doFinal(manifest.getBytes(StandardCharsets.UTF_8));

            // Convertir bytes a Hex
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            // Comparar el hash calculado con el que vino en el header (v1)
            return hexString.toString().equals(v1);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error validando firma HMAC", e);
            return false;
        }
    }
}