package com.pixelpro.attributes.entity;

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
@Table(name = "attributes")
public class AttributeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 30)
    @Column(name = "attribute_name", length = 30, nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "attribute",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default
    private List<AttributeValueEntity> values = new ArrayList<>();

    public void addValue(AttributeValueEntity value) {
        values.add(value);
        value.setAttribute(this);   // Sincroniza la relación en el lado dueño (FK)
    }

    public void removeValue(AttributeValueEntity value) {
        values.remove(value);
        value.setAttribute(null);   // Rompe la relación en el lado dueño
    }
}
