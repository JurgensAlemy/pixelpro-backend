package com.pixelpro.billing.config;

import com.mercadopago.MercadoPagoConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MpIntegrationConfig {

    @Value("${mp.access-token}")
    private String accessToken;

    @PostConstruct
    public void init() {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            System.out.println("✅ [Billing] Mercado Pago SDK inicializado correctamente.");
        } catch (Exception e) {
            System.err.println("❌ Error inicializando Mercado Pago: " + e.getMessage());
        }
    }
}