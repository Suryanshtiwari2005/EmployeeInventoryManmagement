package com.inventoryEmployee.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;

    private Integer quantityAvailable;
    private Integer minStockLevel;
    private Integer maxStockLevel;
    private Integer reorderPoint;
    private Integer reorderQuantity;

    private String location;
    private String binNumber;
    private String rackNumber;

    private Boolean lowStockAlertEnabled;
    private Boolean isActive;

    private LocalDateTime lastRestockDate;
    private LocalDateTime lastSaleDate;

    // Calculated fields
    private Boolean isLowStock;
    private Boolean isOutOfStock;
    private Boolean isOverstocked;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
