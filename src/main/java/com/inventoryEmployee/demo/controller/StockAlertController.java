package com.inventoryEmployee.demo.controller;

import com.inventoryEmployee.demo.dto.response.StockAlertResponse;
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
    public ResponseEntity<Page<StockAlertResponse>> getUnresolvedAlerts(Pageable pageable) {
        Page<StockAlert> alerts = stockAlertService.getUnresolvedAlerts(pageable);
        return ResponseEntity.ok(alerts.map(this::mapToResponse));
    }

    // Get recent unresolved alerts
    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<StockAlertResponse>> getRecentUnresolvedAlerts(Pageable pageable) {
        Page<StockAlert> alerts = stockAlertService.getRecentUnresolvedAlerts(pageable);
        return ResponseEntity.ok(alerts.map(this::mapToResponse));
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
    public ResponseEntity<Page<StockAlertResponse>> filterAlerts(
            @RequestParam(required = false) AlertType alertType,
            @RequestParam(required = false) Boolean isResolved,
            @RequestParam(required = false) Long productId,
            Pageable pageable) {
        Page<StockAlert> alerts = stockAlertService.searchAlertsWithFilters(
                alertType, isResolved, productId, pageable);
        return ResponseEntity.ok(alerts.map(this::mapToResponse));
    }

    private StockAlertResponse mapToResponse(StockAlert alert) {
        return StockAlertResponse.builder()
                .id(alert.getId())

                // Flatten Product Details safely
                .productId(alert.getProduct() != null ? alert.getProduct().getId() : null)
                .productName(alert.getProduct() != null ? alert.getProduct().getName() : "Unknown Product")
                .productSku(alert.getProduct() != null ? alert.getProduct().getSku() : null)

                .alertType(alert.getAlertType())
                .currentQuantity(alert.getCurrentQuantity())
                .threshold(alert.getThreshold())

                .isResolved(alert.getIsResolved())
                .emailSent(alert.getEmailSent())

                .createdAt(alert.getCreatedAt())
                .resolvedAt(alert.getResolvedAt())
                .resolvedBy(alert.getResolvedBy()) // Assuming this is a String name in Entity
                .notes(alert.getNotes())
                .build();
    }
}
