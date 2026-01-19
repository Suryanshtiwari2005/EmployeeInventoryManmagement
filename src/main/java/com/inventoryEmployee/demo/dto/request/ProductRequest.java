package com.inventoryEmployee.demo.dto.request;

import com.inventoryEmployee.demo.enums.ProductStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotBlank(message = "SKU is required")
    @Size(max = 50, message = "SKU must not exceed 50 characters")
    private String sku;

    @Size(max = 13, message = "Barcode must not exceed 13 characters")
    private String barcode;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "Cost price must be non-negative")
    private BigDecimal costPrice;

    private String unit;
    private String imageUrl;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    private String manufacturer;
    private String model;

    private ProductStatus status;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private Long supplierId;
}
