package com.inventoryEmployee.demo.service;

import com.inventoryEmployee.demo.enums.EmployeeStatus;
import com.inventoryEmployee.demo.enums.OrderStatus;
import com.inventoryEmployee.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AnalyticsService {

    private final InventoryRepository inventoryRepository;
    private final EmployeeRepository employeeRepository;
    private final OrderRepository orderRepository;
    private final StockAlertRepository stockAlertRepository;
    private final ProductRepository productRepository;

    // Get dashboard KPIs
    public Map<String, Object> getDashboardKPIs() {
        Map<String, Object> kpis = new HashMap<>();

        // Inventory metrics
        kpis.put("totalInventoryValue", inventoryRepository.calculateTotalInventoryValue());
        kpis.put("lowStockCount", inventoryRepository.countLowStockItems());
        kpis.put("outOfStockCount", inventoryRepository.countOutOfStockItems());
        kpis.put("totalProducts", productRepository.count());

        // Employee metrics
        kpis.put("activeEmployees", employeeRepository.countByStatusAndDeletedFalse(EmployeeStatus.ACTIVE));
        kpis.put("newHiresThisMonth", employeeRepository.findEmployeesHiredThisMonth().size());

        // Order metrics
        kpis.put("pendingOrders", orderRepository.countByStatusAndDeletedFalse(OrderStatus.PENDING));
        kpis.put("completedOrders", orderRepository.countByStatusAndDeletedFalse(OrderStatus.DELIVERED));

        // Alert metrics
        kpis.put("unresolvedAlerts", stockAlertRepository.countByIsResolvedFalse());

        return kpis;
    }

    // Get inventory statistics
    public Map<String, Object> getInventoryStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalValue", inventoryRepository.calculateTotalInventoryValue());
        stats.put("lowStockItems", inventoryRepository.findLowStockItems().size());
        stats.put("outOfStockItems", inventoryRepository.findOutOfStockItems().size());
        stats.put("overstockedItems", inventoryRepository.findOverstockedItems().size());

        return stats;
    }

    // Get order statistics
    public Map<String, Object> getOrderStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalPending", orderRepository.countByStatusAndDeletedFalse(OrderStatus.PENDING));
        stats.put("totalConfirmed", orderRepository.countByStatusAndDeletedFalse(OrderStatus.CONFIRMED));
        stats.put("totalDelivered", orderRepository.countByStatusAndDeletedFalse(OrderStatus.DELIVERED));

        BigDecimal pendingValue = orderRepository.getTotalOrderValueByStatus(OrderStatus.PENDING);
        BigDecimal deliveredValue = orderRepository.getTotalOrderValueByStatus(OrderStatus.DELIVERED);

        stats.put("pendingOrderValue", pendingValue != null ? pendingValue : BigDecimal.ZERO);
        stats.put("deliveredOrderValue", deliveredValue != null ? deliveredValue : BigDecimal.ZERO);

        return stats;
    }
}
