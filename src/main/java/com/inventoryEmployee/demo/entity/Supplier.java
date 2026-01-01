package com.inventoryEmployee.demo.entity;

import com.inventoryEmployee.demo.enums.SupplierStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "suppliers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Supplier name is required")
    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 100)
    private String contactPerson;

    @Email(message = "Invalid email format")
    @Column(unique = true)
    private String email;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Invalid phone number")
    @Column(length = 15)
    private String phone;

    @Column(length = 15)
    private String alternatePhone;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 50)
    private String state;

    @Column(length = 100)
    private String country;

    @Column(length = 10)
    private String zipCode;

    @Column(length = 50)
    private String taxId; // GST/VAT number

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SupplierStatus status = SupplierStatus.ACTIVE;

    // Relationships
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL)
    private List<Product> products;
}
