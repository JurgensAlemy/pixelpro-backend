package com.pixelpro.inventory.entity;

import com.pixelpro.common.entity.AuditableEntity;
import com.pixelpro.variants.entity.VariantEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "inventory")
public class InventoryEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @PositiveOrZero
    @Column(name = "qty_stock", nullable = false)
    private int quantityOnHand;

    @PositiveOrZero
    @Column(name = "qty_reserved", nullable = false)
    private int reservedQuantity;

    @Positive
    @Column(name = "reorder_point", nullable = false)
    private int reorderPoint;

    @Positive
    @Column(name = "safety_stock", nullable = false)
    private int safetyStock;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private VariantEntity variant;
}
