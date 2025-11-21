package com.pixelpro.orders.dto;

import com.pixelpro.billing.dto.InvoiceDto;
import com.pixelpro.billing.dto.PaymentDto;
import com.pixelpro.customers.dto.AddressDto;
import com.pixelpro.customers.dto.CustomerDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Información completa de una orden")
public record OrderDto(
        @Schema(description = "ID de la orden", example = "1")
        Long id,

        @Schema(description = "Código único de la orden", example = "ORD-2025-00001")
        String code,

        @Schema(description = "Estado de la orden", example = "PENDING")
        String status,

        @Schema(description = "Tipo de entrega", example = "HOME_DELIVERY")
        String deliveryType,

        @Schema(description = "Subtotal de la orden", example = "400.00")
        BigDecimal subtotal,

        @Schema(description = "Costo de envío", example = "30.00")
        BigDecimal shippingCost,

        @Schema(description = "Descuento aplicado", example = "20.00")
        BigDecimal discount,

        @Schema(description = "Total de la orden", example = "410.00")
        BigDecimal total,

        @Schema(description = "Fecha de creación")
        LocalDateTime createdAt,

        @Schema(description = "Fecha de última actualización")
        LocalDateTime updatedAt,

        @Schema(description = "Información del cliente")
        CustomerDto customer,

        @Schema(description = "Dirección de envío (puede ser null si es recojo en tienda)")
        AddressDto address,

        @Schema(description = "Items de la orden")
        List<OrderItemDto> items,

        @Schema(description = "Factura asociada (puede ser null si aún no se emite)")
        InvoiceDto invoice,

        @Schema(description = "Pagos asociados a la orden")
        List<PaymentDto> payments
) {
}

