package com.pixelpro.inventory.entity;

import com.pixelpro.catalog.entity.ProductEntity;
import com.pixelpro.common.entity.AuditableEntity;
import jakarta.persistence.*;
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

    @Column(name = "qty_stock", nullable = false)
    private int qtyStock;

    @OneToOne
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;
}
