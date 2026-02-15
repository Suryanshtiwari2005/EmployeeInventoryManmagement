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
    Page<AuditLog> findByEntityNameAndEntityId(String entityName, Long entityId, Pageable pageable);

    // Find by user ID
    List<AuditLog> findByUserId(Long userId);
    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    // Find recent audit logs
    @Query("SELECT a FROM AuditLog a ORDER BY a.timestamp DESC")
    Page<AuditLog> findRecentAuditLogs(Pageable pageable);


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
