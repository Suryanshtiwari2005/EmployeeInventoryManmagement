package com.inventoryEmployee.demo.controller;

import com.inventoryEmployee.demo.entity.Notification;
import com.inventoryEmployee.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    // Get user notifications
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Page<Notification>> getUserNotifications(
            Authentication authentication,
            Pageable pageable) {
        // In real implementation, get userId from authentication
        Long userId = 1L; // Replace with actual user ID from auth
        Page<Notification> notifications = notificationService.getUserNotifications(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    // Get unread notifications
    @GetMapping("/unread")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<Notification>> getUnreadNotifications(Authentication authentication) {
        Long userId = 1L; // Replace with actual user ID from auth
        List<Notification> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    // Count unread notifications
    @GetMapping("/count/unread")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Long> countUnreadNotifications(Authentication authentication) {
        Long userId = 1L; // Replace with actual user ID from auth
        Long count = notificationService.countUnreadNotifications(userId);
        return ResponseEntity.ok(count);
    }

    // Mark notification as read
    @PutMapping("/{notificationId}/read")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    // Mark all notifications as read
    @PutMapping("/read-all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        Long userId = 1L; // Replace with actual user ID from auth
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
}
