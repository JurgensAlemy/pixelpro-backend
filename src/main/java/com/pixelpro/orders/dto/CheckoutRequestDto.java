package com.pixelpro.orders.dto;

import com.pixelpro.billing.entity.enums.PaymentMethod;
import com.pixelpro.orders.entity.enums.DeliveryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

@Schema(description = "DTO para procesar un pedido (checkout)")
public record CheckoutRequestDto(
        @Schema(description = "ID de la dirección de envío (obligatorio solo para A_DOMICILIO)", example = "1")
        Long addressId,

        @Schema(description = "Tipo de entrega", example = "A_DOMICILIO", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El tipo de entrega es obligatorio")
        DeliveryType deliveryType,

        @Schema(description = "Método de pago", example = "YAPE", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El método de pago es obligatorio")
        PaymentMethod paymentMethod,

        @Schema(description = "Items del carrito", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "El carrito no puede estar vacío")
        @Valid
        List<CartItemDto> items
) {
    @Schema(description = "Item del carrito")
    public record CartItemDto(
            @Schema(description = "ID del producto", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "El ID del producto es obligatorio")
            Long productId,

            @Schema(description = "Cantidad", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "La cantidad es obligatoria")
            @Positive(message = "La cantidad debe ser mayor a 0")
            Integer quantity
    ) {
    }
}

