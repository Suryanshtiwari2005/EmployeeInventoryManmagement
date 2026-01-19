package com.inventoryEmployee.demo.dto.response;

import com.inventoryEmployee.demo.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private String sku;
    private String barcode;
    private BigDecimal price;
    private BigDecimal costPrice;
    private String unit;
    private String imageUrl;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    private String manufacturer;
    private String model;
    private ProductStatus status;

    private String categoryName;
    private Long categoryId;

    private String supplierName;
    private Long supplierId;

    // Inventory info
    private Integer quantityAvailable;
    private Boolean isLowStock;
    private Boolean isOutOfStock;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
