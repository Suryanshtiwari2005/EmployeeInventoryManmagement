package com.inventoryEmployee.demo.dto.response;

import com.inventoryEmployee.demo.enums.AlertType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAlertResponse {

    private Long id;

    private Long productId;
    private String productName;
    private String productSku;

    private AlertType alertType;

    private Integer currentQuantity;
    private Integer threshold;

    private Boolean isResolved;
    private Boolean emailSent;

    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    private String resolvedBy;
    private String notes;
}

