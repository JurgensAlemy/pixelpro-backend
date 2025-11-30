package com.pixelpro.billing.service;

import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MpPaymentService {

    // Inyectamos la URL base desde application.properties (o variable de entorno)
    // En DEV será tu Ngrok, en PROD será tu dominio real.
    @Value("${mp.webhook.base-url}")
    private String notificationBaseUrl;

    // Method principal que llamará tu controlador y creará la preferencia
    public String createPreference(List<PreferenceItemRequest> items, String externalReference) {
        try {
//            // 1. Preferencia de Venta (¿Qué estás vendiendo?)
//            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
//                    .title(titulo)
//                    .quantity(cantidad)
//                    .unitPrice(precio)
//                    .currencyId("PEN")
//                    .build();
//
//            List<PreferenceItemRequest> items = new ArrayList<>();
//            items.add(itemRequest);

            // 2. Back URLs (Redirección del navegador)
            // Esto es necesario para que MP sepa a dónde redirigir al usuario.
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(notificationBaseUrl + "/checkout/success") // Éxito en el pago
                    .failure(notificationBaseUrl + "/checkout/failure") // Fallo de tarjeta, etc.
                    .pending(notificationBaseUrl + "/checkout/pending") // Pago en proceso de validación
                    .build();

            // 3. Ensamblar la solicitud completa
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    .autoReturn("approved")  // Regresa automáticamente en éxito
                    // VITAL: Enlazamos la orden de nuestra BD con el pago de MP
                    .externalReference(externalReference)
                    // VITAL: A dónde avisará MP cuando el pago cambie de estado
//                    .notificationUrl("https://bettina-tactile-ogrishly.ngrok-free.dev/api/payments/webhook")
                    .build();

            // 4. Crear la preferencia en Mercado Pago
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);
//            System.out.println(preference.getSandboxInitPoint());

            // 5. Retornamos el ID de la preferencia
            return preference.getId();

        } catch (MPApiException apiException) {
            System.err.println("Status Code: " + apiException.getStatusCode());
            System.err.println("Response: " + apiException.getApiResponse().getContent());
            throw new RuntimeException("Error API MP: " + apiException.getApiResponse().getContent());
        } catch (MPException mpException) {
            System.err.println("Error general de Mercado Pago (Posiblemente conexión):");
            throw new RuntimeException("Error MP General " + mpException.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error desconocido");
        }
    }
}