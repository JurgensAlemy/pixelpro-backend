package com.pixelpro.billing.entity.enums;

public enum InvoiceStatus {
    EMITIDO,             // emitido correctamente
    ANULADO,             // anulado/comunicado a SUNAT
    ENVIO_PENDIENTE  // pendiente de envío a SUNAT
}
