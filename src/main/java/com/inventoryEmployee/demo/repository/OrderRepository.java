package com.inventoryEmployee.demo.repository;

import com.inventoryEmployee.demo.entity.Order;
import com.inventoryEmployee.demo.enums.OrderStatus;
import com.inventoryEmployee.demo.enums.OrderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find by order number
    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    // Find recent orders
    @Query("SELECT o FROM Order o WHERE o.deleted = false ORDER BY o.orderDate DESC")
    Page<Order> findRecentOrders(Pageable pageable);

    // Count orders by status
    Long countByStatusAndDeletedFalse(OrderStatus status);

    // Calculate total order value by status
    @Query("SELECT SUM(o.finalAmount) FROM Order o WHERE o.status = :status AND o.deleted = false")
    BigDecimal getTotalOrderValueByStatus(@Param("status") OrderStatus status);

    // Find pending orders
    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING' AND o.deleted = false")
    List<Order> findPendingOrders();

    // Search orders with filters
    @Query("SELECT o FROM Order o WHERE " +
            "(:status IS NULL OR o.status = :status) AND " +
            "(:orderType IS NULL OR o.orderType = :orderType) AND " +
            "(:employeeId IS NULL OR o.employee.id = :employeeId) AND " +
            "(:supplierId IS NULL OR o.supplier.id = :supplierId) AND " +
            "(:startDate IS NULL OR o.orderDate >= :startDate) AND " +
            "(:endDate IS NULL OR o.orderDate <= :endDate) AND " +
            "o.deleted = false " +
            "ORDER BY o.orderDate DESC")
    Page<Order> findByFilters(@Param("status") OrderStatus status,
                              @Param("orderType") OrderType orderType,
                              @Param("employeeId") Long employeeId,
                              @Param("supplierId") Long supplierId,
                              @Param("startDate") LocalDateTime startDate,
                              @Param("endDate") LocalDateTime endDate,
                              Pageable pageable);
}
