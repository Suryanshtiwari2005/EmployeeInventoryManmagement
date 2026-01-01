package com.inventoryEmployee.demo.entity;

import com.inventoryEmployee.demo.enums.AlertType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AlertType alertType;

    @Column(nullable = false)
    private Integer currentQuantity;

    @Column(nullable = false)
    private Integer threshold;

    @Column(nullable = false)
    private Boolean isResolved = false;

    @Column(nullable = false)
    private Boolean emailSent = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime resolvedAt;

    @Column(length = 50)
    private String resolvedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
