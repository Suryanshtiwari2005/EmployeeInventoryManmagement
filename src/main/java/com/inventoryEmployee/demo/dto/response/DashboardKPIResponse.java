package com.inventoryEmployee.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardKPIResponse {

    // Inventory metrics
    private Double totalInventoryValue;
    private Long lowStockCount;
    private Long outOfStockCount;
    private Long totalProducts;
    private Long totalCategories;

    // Employee metrics
    private Long activeEmployees;
    private Long newHiresThisMonth;
    private Long totalDepartments;

    // Order metrics
    private Long pendingOrders;
    private Long completedOrders;
    private Long totalOrders;

    // Alert metrics
    private Long unresolvedAlerts;

    // Transaction metrics
    private Long transactionsToday;
    private Long transactionsThisWeek;
    private Long transactionsThisMonth;
}
