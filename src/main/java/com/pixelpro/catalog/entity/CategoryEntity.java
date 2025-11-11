package com.pixelpro.catalog.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "categories")
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 80)
    @Column(name = "category_name", nullable = false, length = 80)
    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_category_id")
    private CategoryEntity parentCategory;

    @ManyToMany(mappedBy = "categories")
    @Builder.Default
    private List<ProductEntity> products = new ArrayList<>();

    @OneToMany(mappedBy = "parentCategory",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default
    private List<CategoryEntity> subCategories = new ArrayList<>();
}
