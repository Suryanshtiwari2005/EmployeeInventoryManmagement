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
    List<StockTransaction> findByProductId(Long productId);
    Page<StockTransaction> findByProductId(Long productId, Pageable pageable);

    // Find by employee ID
    List<StockTransaction> findByEmployeeId(Long employeeId);
    Page<StockTransaction> findByEmployeeId(Long employeeId, Pageable pageable);

    // Find by transaction type
    List<StockTransaction> findByType(TransactionType type);
    Page<StockTransaction> findByType(TransactionType type, Pageable pageable);

    // Find transactions in date range
    List<StockTransaction> findByTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    Page<StockTransaction> findByTransactionDateBetween(LocalDateTime startDate,
                                                        LocalDateTime endDate,
                                                        Pageable pageable);

    // Find by product and date range
    @Query("SELECT st FROM StockTransaction st WHERE st.product.id = :productId " +
            "AND st.transactionDate BETWEEN :startDate AND :endDate " +
            "ORDER BY st.transactionDate DESC")
    List<StockTransaction> findByProductAndDateRange(@Param("productId") Long productId,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    // Find recent transactions
    @Query("SELECT st FROM StockTransaction st ORDER BY st.transactionDate DESC")
    Page<StockTransaction> findRecentTransactions(Pageable pageable);

    // Find transactions by username
    List<StockTransaction> findByPerformedBy(String username);

    // Count transactions by employee
    Long countByEmployeeId(Long employeeId);

    // Count transactions by product
    Long countByProductId(Long productId);

    // Get total quantity IN for a product
    @Query("SELECT SUM(st.quantity) FROM StockTransaction st " +
            "WHERE st.product.id = :productId AND st.type = 'IN'")
    Integer getTotalQuantityIn(@Param("productId") Long productId);

    // Get total quantity OUT for a product
    @Query("SELECT SUM(st.quantity) FROM StockTransaction st " +
            "WHERE st.product.id = :productId AND st.type = 'OUT'")
    Integer getTotalQuantityOut(@Param("productId") Long productId);

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