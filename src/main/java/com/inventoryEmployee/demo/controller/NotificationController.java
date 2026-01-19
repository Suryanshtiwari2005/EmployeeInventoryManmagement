package com.inventoryEmployee.demo.controller;

import com.inventoryEmployee.demo.dto.response.NotificationResponse;
import com.inventoryEmployee.demo.entity.Notification;
import com.inventoryEmployee.demo.entity.User;
import com.inventoryEmployee.demo.repository.UserRepository;
import com.inventoryEmployee.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    private Long getCurrentUserId(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    // Get user notifications
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Page<NotificationResponse>> getUserNotifications(
            Authentication authentication,
            Pageable pageable) {

        Long userId = getCurrentUserId(authentication);
        Page<Notification> notifications = notificationService.getUserNotifications(userId, pageable);
        return ResponseEntity.ok(notifications.map(this::mapToResponse));
    }

    // Get unread notifications
    @GetMapping("/unread")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);// Replace with actual user ID from auth
        List<Notification> notifications = notificationService.getUnreadNotifications(userId);

        List<NotificationResponse> responseList = notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
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

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .link(notification.getLink())
                .priority(notification.getPriority())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}
