package com.inventoryEmployee.demo.dto.response;

import com.inventoryEmployee.demo.enums.StockMovementReason;
import com.inventoryEmployee.demo.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransactionResponse {

    private Long id;

    private Long productId;
    private String productName;
    private String productSku;

    private Long employeeId;
    private String employeeName;

    private TransactionType type;
    private StockMovementReason reason;

    private Integer quantity;
    private Integer previousQuantity;
    private Integer newQuantity;

    private String notes;
    private String referenceNumber;

    private String ipAddress;
    private String performedBy;

    private LocalDateTime transactionDate;
}
