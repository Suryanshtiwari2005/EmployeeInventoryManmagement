package com.inventoryEmployee.demo.enums;

public enum AlertType {
    LOW_STOCK,       // Below min level
    OUT_OF_STOCK,    // Zero quantity
    OVERSTOCKED,     // Above max level
    EXPIRING_SOON,   // About to expire
    EXPIRED          // Already expired
}
