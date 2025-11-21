package com.pixelpro.billing.entity.enums;

public enum PaymentStatus {
    PENDIENTE,        // recibido intento, en proceso
    AUTORIZADO,     // autorizado por la pasarela
    CONFIRMADO,       // cobro confirmado/capturado
    RECHAZADO,         // rechazado o error
    CANCELADO,       // cancelado antes de capturar
    REEMBOLSADO        // reembolsado total/parcial
}
