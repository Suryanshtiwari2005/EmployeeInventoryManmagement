package com.inventoryEmployee.demo.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantityAvailable;

    @Min(value = 0, message = "Min stock level cannot be negative")
    private Integer minStockLevel;

    @Min(value = 0, message = "Max stock level cannot be negative")
    private Integer maxStockLevel;

    @Min(value = 0, message = "Reorder point cannot be negative")
    private Integer reorderPoint;

    @Min(value = 0, message = "Reorder quantity cannot be negative")
    private Integer reorderQuantity;

    private String location;
    private String binNumber;
    private String rackNumber;

    private Boolean lowStockAlertEnabled;
    private Boolean isActive;
}
