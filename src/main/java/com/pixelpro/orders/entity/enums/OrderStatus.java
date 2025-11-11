package com.pixelpro.orders.entity.enums;

public enum OrderStatus {
    PENDING,          // creado, esperando pago
    CONFIRMED,        // pagado y validado
    PREPARING,        // en proceso de alistamiento
    SHIPPED,          // ya salió para entrega
    DELIVERED,        // recibido por el cliente
    CANCELED          // anulado antes o después del pago
}
