package com.inventoryEmployee.demo.repository;

import com.inventoryEmployee.demo.entity.StockAlert;
import com.inventoryEmployee.demo.enums.AlertType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockAlertRepository extends JpaRepository<StockAlert, Long> {


    // Find unresolved alerts
    List<StockAlert> findByIsResolvedFalse();
    Page<StockAlert> findByIsResolvedFalse(Pageable pageable);

    // Count unresolved alerts
    Long countByIsResolvedFalse();

    // Find unresolved alerts for a product
    @Query("SELECT sa FROM StockAlert sa WHERE sa.product.id = :productId " +
            "AND sa.isResolved = false ORDER BY sa.createdAt DESC")
    List<StockAlert> findUnresolvedAlertsByProduct(@Param("productId") Long productId);

    // Find recent unresolved alerts
    @Query("SELECT sa FROM StockAlert sa WHERE sa.isResolved = false " +
            "ORDER BY sa.createdAt DESC")
    Page<StockAlert> findRecentUnresolvedAlerts(Pageable pageable);

    // Advanced search with filters
    @Query("SELECT sa FROM StockAlert sa WHERE " +
            "(:alertType IS NULL OR sa.alertType = :alertType) AND " +
            "(:isResolved IS NULL OR sa.isResolved = :isResolved) AND " +
            "(:productId IS NULL OR sa.product.id = :productId) " +
            "ORDER BY sa.createdAt DESC")
    Page<StockAlert> findByFilters(@Param("alertType") AlertType alertType,
                                   @Param("isResolved") Boolean isResolved,
                                   @Param("productId") Long productId,
                                   Pageable pageable);
}
