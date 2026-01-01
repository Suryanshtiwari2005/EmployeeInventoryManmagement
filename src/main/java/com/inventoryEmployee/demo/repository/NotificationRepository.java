package com.inventoryEmployee.demo.repository;

import com.inventoryEmployee.demo.entity.Notification;
import com.inventoryEmployee.demo.enums.NotificationPriority;
import com.inventoryEmployee.demo.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Find by user ID
    List<Notification> findByUserId(Long userId);
    Page<Notification> findByUserId(Long userId, Pageable pageable);

    // Find unread notifications by user
    List<Notification> findByUserIdAndIsReadFalse(Long userId);
    Page<Notification> findByUserIdAndIsReadFalse(Long userId, Pageable pageable);

    // Find read notifications by user
    List<Notification> findByUserIdAndIsReadTrue(Long userId);

    // Find by notification type
    List<Notification> findByType(NotificationType type);
    Page<Notification> findByType(NotificationType type, Pageable pageable);

    // Find by priority
    List<Notification> findByPriority(NotificationPriority priority);
    Page<Notification> findByPriority(NotificationPriority priority, Pageable pageable);

    // Find by user and type
    List<Notification> findByUserIdAndType(Long userId, NotificationType type);

    // Find by date range
    List<Notification> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Count unread notifications by user
    Long countByUserIdAndIsReadFalse(Long userId);

    // Count urgent unread notifications
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId " +
            "AND n.isRead = false AND n.priority = 'URGENT'")
    Long countUrgentUnreadNotifications(@Param("userId") Long userId);

    // Find recent notifications for user
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId " +
            "ORDER BY n.createdAt DESC")
    Page<Notification> findRecentNotificationsByUser(@Param("userId") Long userId, Pageable pageable);

    // Mark notification as read
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readTime " +
            "WHERE n.id = :notificationId")
    void markAsRead(@Param("notificationId") Long notificationId,
                    @Param("readTime") LocalDateTime readTime);

    // Mark all user notifications as read
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readTime " +
            "WHERE n.user.id = :userId AND n.isRead = false")
    void markAllAsReadByUser(@Param("userId") Long userId,
                             @Param("readTime") LocalDateTime readTime);

    // Delete old read notifications
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.isRead = true " +
            "AND n.readAt < :cutoffDate")
    void deleteOldReadNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Advanced search with filters
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND " +
            "(:type IS NULL OR n.type = :type) AND " +
            "(:priority IS NULL OR n.priority = :priority) AND " +
            "(:isRead IS NULL OR n.isRead = :isRead) " +
            "ORDER BY n.createdAt DESC")
    Page<Notification> findByFilters(@Param("userId") Long userId,
                                     @Param("type") NotificationType type,
                                     @Param("priority") NotificationPriority priority,
                                     @Param("isRead") Boolean isRead,
                                     Pageable pageable);
}
