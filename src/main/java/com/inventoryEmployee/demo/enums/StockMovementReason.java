package com.inventoryEmployee.demo.enums;

public enum StockMovementReason {
    PURCHASE,           // Received from supplier
    SALES,              // Sold to customer
    DAMAGED,            // Item damaged/broken
    LOST,               // Item lost/stolen
    ISSUED_TO_EMPLOYEE, // Given to employee
    RETURNED,           // Returned from employee/customer
    EXPIRED,            // Item expired
    ADJUSTMENT,         // Manual correction
    TRANSFER,           // Transfer between locations
    SAMPLE              // Given as sample
}
