package com.pixelpro.billing.entity;

import com.pixelpro.billing.entity.enums.CurrencyCode;
import com.pixelpro.billing.entity.enums.InvoiceStatus;
import com.pixelpro.billing.entity.enums.InvoiceType;
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
@Table(name = "invoices")
public class Invoice extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceType type;

    @NotBlank
    @Size(max = 10)
    @Column(length = 10)
    private String serie;

    @NotBlank
    @Size(max = 20)
    @Column(length = 20)
    private String number;

    @PastOrPresent
    @Column(name = "issued_at")
    private OffsetDateTime issuedAt;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 10, fraction = 2)
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private CurrencyCode currency;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InvoiceStatus status; // ISSUED | VOIDED | PENDING_SUBMISSION

    @Lob
    @Column(name = "hash_value")
    private String hashValue; // hash firmado / digest del XML/PDF

    @Lob
    @Column(name = "document_url")
    private String documentUrl; // URL del PDF/XML en tu storage

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;
}
