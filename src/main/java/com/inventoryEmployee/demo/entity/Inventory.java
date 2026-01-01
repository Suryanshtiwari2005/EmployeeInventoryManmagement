package com.inventoryEmployee.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "product_id", unique = true, nullable = false)
    private Product product;

    @Version // Optimistic locking to prevent concurrent updates
    private Long version;

    @Min(value = 0, message = "Quantity cannot be negative")
    @Column(nullable = false)
    private Integer quantityAvailable = 0;

    @Min(value = 0)
    @Column(nullable = false)
    private Integer minStockLevel = 10; // Reorder threshold

    @Min(value = 0)
    @Column(nullable = false)
    private Integer maxStockLevel = 1000;

    @Min(value = 0)
    private Integer reorderPoint = 20;

    @Min(value = 0)
    private Integer reorderQuantity = 50;

    @Column(length = 100)
    private String location; // Warehouse section

    @Column(length = 50)
    private String binNumber; // Shelf/Bin location

    @Column(length = 50)
    private String rackNumber;

    private LocalDateTime lastRestockDate;

    private LocalDateTime lastSaleDate;

    @Column(nullable = false)
    private Boolean lowStockAlertEnabled = true;

    @Column(nullable = false)
    private Boolean isActive = true;
}
