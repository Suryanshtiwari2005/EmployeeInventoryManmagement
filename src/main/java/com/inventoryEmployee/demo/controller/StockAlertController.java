package com.inventoryEmployee.demo.controller;

import com.inventoryEmployee.demo.entity.StockAlert;
import com.inventoryEmployee.demo.enums.AlertType;
import com.inventoryEmployee.demo.service.StockAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StockAlertController {

    private final StockAlertService stockAlertService;

    // Get unresolved alerts
    @GetMapping("/unresolved")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<StockAlert>> getUnresolvedAlerts(Pageable pageable) {
        Page<StockAlert> alerts = stockAlertService.getUnresolvedAlerts(pageable);
        return ResponseEntity.ok(alerts);
    }

    // Get recent unresolved alerts
    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<StockAlert>> getRecentUnresolvedAlerts(Pageable pageable) {
        Page<StockAlert> alerts = stockAlertService.getRecentUnresolvedAlerts(pageable);
        return ResponseEntity.ok(alerts);
    }

    // Resolve alert
    @PutMapping("/{alertId}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> resolveAlert(
            @PathVariable Long alertId,
            @RequestBody Map<String, String> request) {
        String notes = request.get("notes");
        stockAlertService.resolveAlert(alertId, notes);
        return ResponseEntity.ok().build();
    }

    // Count unresolved alerts
    @GetMapping("/count/unresolved")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Long> countUnresolvedAlerts() {
        Long count = stockAlertService.countUnresolvedAlerts();
        return ResponseEntity.ok(count);
    }

    // Search alerts with filters
    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<StockAlert>> filterAlerts(
            @RequestParam(required = false) AlertType alertType,
            @RequestParam(required = false) Boolean isResolved,
            @RequestParam(required = false) Long productId,
            Pageable pageable) {
        Page<StockAlert> alerts = stockAlertService.searchAlertsWithFilters(
                alertType, isResolved, productId, pageable);
        return ResponseEntity.ok(alerts);
    }
}
