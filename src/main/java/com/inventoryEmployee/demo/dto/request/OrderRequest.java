package com.inventoryEmployee.demo.dto.request;

import com.inventoryEmployee.demo.enums.OrderStatus;
import com.inventoryEmployee.demo.enums.OrderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    @NotBlank(message = "Order number is required")
    @Size(max = 50, message = "Order number cannot exceed 50 characters")
    private String orderNumber;

    // Optional: If null, Service sets to LocalDateTime.now()
    private LocalDateTime orderDate;

    // Optional: If null, Service sets to PENDING
    private OrderStatus status;

    @NotNull(message = "Order type is required (PURCHASE, SALES, INTERNAL)")
    private OrderType orderType;

    // Optional: Used mostly for PURCHASE orders
    private Long supplierId;

    @DecimalMin(value = "0.0", message = "Tax amount cannot be negative")
    private BigDecimal taxAmount;

    @DecimalMin(value = "0.0", message = "Discount amount cannot be negative")
    private BigDecimal discountAmount;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    private LocalDateTime expectedDeliveryDate;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid // Validates the nested list of items
    private List<OrderItemRequest> items;

    // Nested DTO for Items
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {

        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.0", message = "Unit price cannot be negative")
        private BigDecimal unitPrice;

        @DecimalMin(value = "0.0", message = "Discount percent cannot be negative")
        @DecimalMax(value = "100.0", message = "Discount percent cannot exceed 100")
        private BigDecimal discountPercent;
    }
}
