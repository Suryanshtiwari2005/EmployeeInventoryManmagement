package com.inventoryEmployee.demo.entity;

import com.inventoryEmployee.demo.enums.StockMovementReason;
import com.inventoryEmployee.demo.enums.TransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type; // IN, OUT, ADJUSTMENT

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StockMovementReason reason;

    @NotNull
    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer previousQuantity;

    @Column(nullable = false)
    private Integer newQuantity;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(length = 50)
    private String referenceNumber; // Invoice/PO number

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 255)
    private String userAgent;

    @Column(nullable = false, updatable = false)
    private LocalDateTime transactionDate = LocalDateTime.now();

    @Column(length = 50)
    private String performedBy; // Username
}
