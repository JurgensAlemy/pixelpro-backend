package com.pixelpro.orders.entity.enums;

public enum OrderStatus {
    PENDIENTE,          // creado, esperando pago
    CONFIRMADO,        // pagado y validado
    PREPARANDO,        // en proceso de alistamiento
    ENVIADO,          // ya salió para entrega
    ENTREGADO,        // recibido por el cliente
    CANCELADO          // anulado antes o después del pago
}
