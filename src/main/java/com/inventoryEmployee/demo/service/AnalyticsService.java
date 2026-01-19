package com.inventoryEmployee.demo.service;

import com.inventoryEmployee.demo.dto.response.DashboardKPIResponse;
import com.inventoryEmployee.demo.enums.EmployeeStatus;
import com.inventoryEmployee.demo.enums.OrderStatus;
import com.inventoryEmployee.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final CategoryRepository categoryRepository;
    private final DepartmentRepository departmentRepository;
    private final StockTransactionRepository stockTransactionRepository;

    // Get dashboard KPIs
    public DashboardKPIResponse getDashboardKPIs() {
        Double totalValue = inventoryRepository.calculateTotalInventoryValue();
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);
        LocalDate lastDayOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay(); // e.g., 2023-10-27 00:00:00
        LocalDateTime nowT = LocalDateTime.now();

        return DashboardKPIResponse.builder()
                // Inventory metrics
                .totalInventoryValue(totalValue != null ? totalValue : 0.0)
                .lowStockCount(inventoryRepository.countLowStockItems())
                .outOfStockCount(inventoryRepository.countOutOfStockItems())
                .totalProducts(productRepository.count())
                .totalCategories(categoryRepository.count()) // You missed this in your snippet

                // Employee metrics
                .activeEmployees(employeeRepository.countByStatusAndDeletedFalse(EmployeeStatus.ACTIVE))
                // OPTIMIZATION: Use count() instead of fetching the whole list just to size() it
                .newHiresThisMonth(employeeRepository.countEmployeesHiredThisMonth(firstDayOfMonth,lastDayOfMonth))
                .totalDepartments(departmentRepository.count())

                // Order metrics
                .pendingOrders(orderRepository.countByStatusAndDeletedFalse(OrderStatus.PENDING))
                .completedOrders(orderRepository.countByStatusAndDeletedFalse(OrderStatus.DELIVERED))
                .totalOrders(orderRepository.count())

                // Alert metrics
                .unresolvedAlerts(stockAlertRepository.countByIsResolvedFalse())

                // Transaction metrics (if you need them)
                .transactionsToday(stockTransactionRepository.countByTransactionDateBetween(startOfDay,nowT))

                .build();
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
