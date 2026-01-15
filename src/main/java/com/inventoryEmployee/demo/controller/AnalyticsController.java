package com.inventoryEmployee.demo.controller;

import com.inventoryEmployee.demo.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // Get dashboard KPIs
    @GetMapping("/dashboard-kpis")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> getDashboardKPIs() {
        Map<String, Object> kpis = analyticsService.getDashboardKPIs();
        return ResponseEntity.ok(kpis);
    }

    // Get inventory statistics
    @GetMapping("/inventory-stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> getInventoryStatistics() {
        Map<String, Object> stats = analyticsService.getInventoryStatistics();
        return ResponseEntity.ok(stats);
    }

    // Get order statistics
    @GetMapping("/order-stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> getOrderStatistics() {
        Map<String, Object> stats = analyticsService.getOrderStatistics();
        return ResponseEntity.ok(stats);
    }
}
