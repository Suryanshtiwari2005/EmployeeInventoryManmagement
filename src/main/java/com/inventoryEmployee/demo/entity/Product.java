package com.inventoryEmployee.demo.entity;

import com.inventoryEmployee.demo.enums.ProductStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "SKU is required")
    @Column(unique = true, nullable = false, length = 50)
    private String sku; // Stock Keeping Unit

    @Column(unique = true, length = 13)
    private String barcode; // EAN-13 or UPC

    @DecimalMin(value = "0.0", inclusive = false)
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @DecimalMin(value = "0.0")
    @Column(precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(length = 50)
    private String unit; // e.g., "pieces", "kg", "liters"

    @Column(length = 255)
    private String imageUrl;

    private LocalDate manufacturingDate;

    private LocalDate expiryDate;

    @Column(length = 100)
    private String manufacturer;

    @Column(length = 50)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProductStatus status = ProductStatus.ACTIVE;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL)
    private Inventory inventory;
}

