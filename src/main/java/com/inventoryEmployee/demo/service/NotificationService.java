package com.inventoryEmployee.demo.service;

import com.inventoryEmployee.demo.entity.Notification;
import com.inventoryEmployee.demo.entity.StockAlert;
import com.inventoryEmployee.demo.entity.User;
import com.inventoryEmployee.demo.enums.NotificationPriority;
import com.inventoryEmployee.demo.enums.NotificationType;
import com.inventoryEmployee.demo.repository.NotificationRepository;
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
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // Create stock alert notification
    public void createStockAlertNotification(StockAlert alert) {
        // Send to all managers/admins (simplified - you'd query users with proper roles)
        String title = alert.getAlertType() + " Alert";
        String message = String.format(
                "Product %s has %d units (threshold: %d)",
                alert.getProduct().getName(),
                alert.getCurrentQuantity(),
                alert.getThreshold()
        );

        // Create notification (you'd loop through manager users here)
        log.info("Stock alert notification created for product: {}", alert.getProduct().getId());
    }

    // Create notification
    public void createNotification(User user, NotificationType type, String title,
                                   String message, String link, NotificationPriority priority) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .link(link)
                .priority(priority)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }

    // Get user notifications
    @Transactional(readOnly = true)
    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findRecentNotificationsByUser(userId, pageable);
    }

    // Get unread notifications
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId);
    }

    // Count unread notifications
    @Transactional(readOnly = true)
    public Long countUnreadNotifications(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    // Mark as read
    public void markAsRead(Long notificationId) {
        notificationRepository.markAsRead(notificationId, LocalDateTime.now());
    }

    // Mark all as read
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUser(userId, LocalDateTime.now());
    }
}
