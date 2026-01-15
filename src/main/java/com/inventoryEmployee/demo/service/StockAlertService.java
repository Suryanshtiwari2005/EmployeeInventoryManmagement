package com.inventoryEmployee.demo.service;

import com.inventoryEmployee.demo.entity.Inventory;
import com.inventoryEmployee.demo.entity.StockAlert;
import com.inventoryEmployee.demo.enums.AlertType;
import com.inventoryEmployee.demo.repository.StockAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StockAlertService {

    private final StockAlertRepository stockAlertRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    // Check and create alerts if needed
    public void checkAndCreateAlerts(Inventory inventory) {
        if (!inventory.getLowStockAlertEnabled()) {
            return;
        }

        int currentQuantity = inventory.getQuantityAvailable();
        int minLevel = inventory.getMinStockLevel();
        int maxLevel = inventory.getMaxStockLevel();

        // Check if already has unresolved alert for this product
        List<StockAlert> unresolvedAlerts = stockAlertRepository
                .findUnresolvedAlertsByProduct(inventory.getProduct().getId());

        if (!unresolvedAlerts.isEmpty()) {
            return; // Alert already exists
        }

        AlertType alertType = null;

        if (currentQuantity == 0) {
            alertType = AlertType.OUT_OF_STOCK;
        } else if (currentQuantity <= minLevel) {
            alertType = AlertType.LOW_STOCK;
        } else if (currentQuantity > maxLevel) {
            alertType = AlertType.OVERSTOCKED;
        }

        if (alertType != null) {
            createAlert(inventory, alertType, currentQuantity,
                    alertType == AlertType.OVERSTOCKED ? maxLevel : minLevel);
        }
    }

    // Create alert
    private void createAlert(Inventory inventory, AlertType alertType,
                             int currentQuantity, int threshold) {
        log.info("Creating {} alert for product: {}", alertType, inventory.getProduct().getId());

        StockAlert alert = StockAlert.builder()
                .product(inventory.getProduct())
                .alertType(alertType)
                .currentQuantity(currentQuantity)
                .threshold(threshold)
                .isResolved(false)
                .emailSent(false)
                .createdAt(LocalDateTime.now())
                .build();

        StockAlert savedAlert = stockAlertRepository.save(alert);

        // Send email notification (async)
        emailService.sendLowStockAlert(savedAlert);

        // Create in-app notification
        notificationService.createStockAlertNotification(savedAlert);
    }

    // Resolve alerts when stock is replenished
    public void checkAndResolveAlerts(Inventory inventory) {
        List<StockAlert> unresolvedAlerts = stockAlertRepository
                .findUnresolvedAlertsByProduct(inventory.getProduct().getId());

        for (StockAlert alert : unresolvedAlerts) {
            boolean shouldResolve = false;

            switch (alert.getAlertType()) {
                case OUT_OF_STOCK:
                case LOW_STOCK:
                    if (inventory.getQuantityAvailable() > inventory.getMinStockLevel()) {
                        shouldResolve = true;
                    }
                    break;
                case OVERSTOCKED:
                    if (inventory.getQuantityAvailable() <= inventory.getMaxStockLevel()) {
                        shouldResolve = true;
                    }
                    break;
            }

            if (shouldResolve) {
                resolveAlert(alert.getId(), "Stock level normalized");
            }
        }
    }

    // Resolve alert manually
    public void resolveAlert(Long alertId, String notes) {
        StockAlert alert = stockAlertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found"));

        log.info("Resolving alert: {}", alertId);

        alert.setIsResolved(true);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setNotes(notes);

        stockAlertRepository.save(alert);
    }

    // Get unresolved alerts
    @Transactional(readOnly = true)
    public List<StockAlert> getUnresolvedAlerts() {
        return stockAlertRepository.findByIsResolvedFalse();
    }

    // Get unresolved alerts with pagination
    @Transactional(readOnly = true)
    public Page<StockAlert> getUnresolvedAlerts(Pageable pageable) {
        return stockAlertRepository.findByIsResolvedFalse(pageable);
    }

    // Count unresolved alerts
    @Transactional(readOnly = true)
    public Long countUnresolvedAlerts() {
        return stockAlertRepository.countByIsResolvedFalse();
    }

    // Get recent unresolved alerts
    @Transactional(readOnly = true)
    public Page<StockAlert> getRecentUnresolvedAlerts(Pageable pageable) {
        return stockAlertRepository.findRecentUnresolvedAlerts(pageable);
    }

    // Search alerts with filters
    @Transactional(readOnly = true)
    public Page<StockAlert> searchAlertsWithFilters(AlertType alertType, Boolean isResolved,
                                                    Long productId, Pageable pageable) {
        return stockAlertRepository.findByFilters(alertType, isResolved, productId, pageable);
    }
}
