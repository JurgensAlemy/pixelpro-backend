package com.pixelpro.billing.entity.enums;

public enum PaymentStatus {
    PENDING,        // recibido intento, en proceso
    AUTHORIZED,     // autorizado por la pasarela
    CAPTURED,       // cobro confirmado/capturado
    FAILED,         // rechazado o error
    CANCELED,       // cancelado antes de capturar
    REFUNDED        // reembolsado total/parcial
}
