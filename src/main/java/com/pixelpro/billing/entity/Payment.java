package com.pixelpro.billing.entity;

import com.pixelpro.billing.entity.enums.CurrencyCode;
import com.pixelpro.billing.entity.enums.PaymentMethod;
import com.pixelpro.billing.entity.enums.PaymentStatus;
import com.pixelpro.common.entity.AuditableEntity;
import com.pixelpro.orders.entity.OrderEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payments")
public class Payment extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Positive
    @Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private CurrencyCode currency;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    // ID de la pasarela
    @Size(max = 100)
    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @PastOrPresent
    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;
}
