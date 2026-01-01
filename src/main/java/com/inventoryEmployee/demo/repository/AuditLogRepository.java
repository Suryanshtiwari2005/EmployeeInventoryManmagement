package com.inventoryEmployee.demo.repository;

import com.inventoryEmployee.demo.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Find by entity name and ID
    List<AuditLog> findByEntityNameAndEntityId(String entityName, Long entityId);
    Page<AuditLog> findByEntityNameAndEntityId(String entityName, Long entityId, Pageable pageable);

    // Find by user ID
    List<AuditLog> findByUserId(Long userId);
    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    // Find by username
    List<AuditLog> findByUsername(String username);
    Page<AuditLog> findByUsername(String username, Pageable pageable);

    // Find by action
    List<AuditLog> findByAction(String action);
    Page<AuditLog> findByAction(String action, Pageable pageable);

    // Find by entity name
    List<AuditLog> findByEntityName(String entityName);
    Page<AuditLog> findByEntityName(String entityName, Pageable pageable);

    // Find by date range
    List<AuditLog> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
    Page<AuditLog> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // Find by IP address
    List<AuditLog> findByIpAddress(String ipAddress);

    // Find recent audit logs
    @Query("SELECT a FROM AuditLog a ORDER BY a.timestamp DESC")
    Page<AuditLog> findRecentAuditLogs(Pageable pageable);

    // Count logs by user
    Long countByUserId(Long userId);

    // Count logs by action
    Long countByAction(String action);

    // Get user activity summary
    @Query("SELECT a.username, a.action, COUNT(a) FROM AuditLog a " +
            "WHERE a.timestamp BETWEEN :startDate AND :endDate " +
            "GROUP BY a.username, a.action")
    List<Object[]> getUserActivitySummary(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    // Advanced search with filters
    @Query("SELECT a FROM AuditLog a WHERE " +
            "(:entityName IS NULL OR a.entityName = :entityName) AND " +
            "(:action IS NULL OR a.action = :action) AND " +
            "(:userId IS NULL OR a.user.id = :userId) AND " +
            "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
            "(:endDate IS NULL OR a.timestamp <= :endDate) " +
            "ORDER BY a.timestamp DESC")
    Page<AuditLog> findByFilters(@Param("entityName") String entityName,
                                 @Param("action") String action,
                                 @Param("userId") Long userId,
                                 @Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate,
                                 Pageable pageable);
}
