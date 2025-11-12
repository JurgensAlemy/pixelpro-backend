package com.pixelpro.variants.entity;

import com.pixelpro.catalog.entity.ProductEntity;
import com.pixelpro.common.entity.AuditableEntity;
import com.pixelpro.inventory.entity.InventoryEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;


/**
 * VariantEntity representa una opción comprable de ProductEntity
 * (ejem. Bikini — Color: Rojo, Talla: M) con su SKU y precio.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "variants")
public class VariantEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 30)
    @Column(length = 30, nullable = false, unique = true)
    private String sku;

    @NotNull
    @Digits(integer = 8, fraction = 2)
    @PositiveOrZero
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Digits(integer = 8, fraction = 2)
    @PositiveOrZero
    @Column(name = "compare_at_price", precision = 10, scale = 2)
    private BigDecimal compareAtPrice;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;


    @OneToOne(mappedBy = "variant")
    private InventoryEntity inventory;
}
