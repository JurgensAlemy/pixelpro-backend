package com.pixelpro.customers.entity;

import com.pixelpro.common.entity.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "addresses")
public class AddressEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 20)
    @Column(name = "type", nullable = false, length = 20)
    private String addressType;

    @NotBlank
    @Size(max = 50)
    @Column(name = "department", nullable = false, length = 50)
    private String department;

    @NotBlank
    @Size(max = 50)
    @Column(name = "province", nullable = false, length = 50)
    private String province;

    @NotBlank
    @Size(max = 50)
    @Column(name = "district", nullable = false, length = 50)
    private String district;

    @NotBlank
    @Size(max = 120)
    @Column(name = "address_line", nullable = false, length = 120)
    private String addressLine;

    @Size(max = 120)
    @Column(name = "reference", length = 120)
    private String addressReference;

    @Size(max = 9)
    @Column(name = "phone", length = 9)
    private String addressPhone;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;
}
