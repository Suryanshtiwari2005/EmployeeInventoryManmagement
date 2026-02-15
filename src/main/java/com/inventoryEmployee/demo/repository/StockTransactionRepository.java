package com.inventoryEmployee.demo.repository;


import com.inventoryEmployee.demo.entity.StockTransaction;
import com.inventoryEmployee.demo.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {

    // Find by product ID
    Page<StockTransaction> findByProductId(Long productId, Pageable pageable);

    // Find by employee ID
    Page<StockTransaction> findByEmployeeId(Long employeeId, Pageable pageable);

    // Find transactions in date range
    Page<StockTransaction> findByTransactionDateBetween(LocalDateTime startDate,
                                                        LocalDateTime endDate,
                                                        Pageable pageable);

    // Find recent transactions
    @Query("SELECT st FROM StockTransaction st ORDER BY st.transactionDate DESC")
    Page<StockTransaction> findRecentTransactions(Pageable pageable);

    // Count transactions by employee
    Long countByEmployeeId(Long employeeId);

    long countByTransactionDateBetween(LocalDateTime start, LocalDateTime end);

    // Advanced search with filters
    @Query("SELECT st FROM StockTransaction st WHERE " +
            "(:productId IS NULL OR st.product.id = :productId) AND " +
            "(:employeeId IS NULL OR st.employee.id = :employeeId) AND " +
            "(:type IS NULL OR st.type = :type) AND " +
            "(:startDate IS NULL OR st.transactionDate >= :startDate) AND " +
            "(:endDate IS NULL OR st.transactionDate <= :endDate) " +
            "ORDER BY st.transactionDate DESC")
    Page<StockTransaction> findByFilters(@Param("productId") Long productId,
                                         @Param("employeeId") Long employeeId,
                                         @Param("type") TransactionType type,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate,
                                         Pageable pageable);
}