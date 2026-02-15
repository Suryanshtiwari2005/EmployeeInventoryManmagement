package com.inventoryEmployee.demo.service;

import com.inventoryEmployee.demo.entity.Employee;
import com.inventoryEmployee.demo.entity.Product;
import com.inventoryEmployee.demo.entity.StockTransaction;
import com.inventoryEmployee.demo.enums.StockMovementReason;
import com.inventoryEmployee.demo.enums.TransactionType;
import com.inventoryEmployee.demo.repository.StockTransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StockTransactionService {

    private final StockTransactionRepository stockTransactionRepository;
    private final HttpServletRequest request;

    // Record a stock transaction
    public void recordTransaction(Product product, Employee employee,
                                  TransactionType type, StockMovementReason reason,
                                  Integer quantity, Integer previousQuantity,
                                  Integer newQuantity, String notes) {

        log.info("Recording {} transaction for product {} by employee {}",
                type, product.getId(), employee != null ? employee.getId() : "SYSTEM");

        String ipAddress = IPUtil.getClientIP(request);
        String userAgent = request.getHeader("User-Agent");

        StockTransaction transaction = StockTransaction.builder()
                .product(product)
                .employee(employee)
                .type(type)
                .reason(reason)
                .quantity(quantity)
                .previousQuantity(previousQuantity)
                .newQuantity(newQuantity)
                .notes(notes)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .transactionDate(LocalDateTime.now())
                .performedBy(employee != null ? employee.getEmail() : "SYSTEM")
                .build();

        stockTransactionRepository.save(transaction);
    }

    // Get transactions by product
    @Transactional(readOnly = true)
    public Page<StockTransaction> getTransactionsByProduct(Long productId, Pageable pageable) {
        return stockTransactionRepository.findByProductId(productId, pageable);
    }

    // Get transactions by employee
    @Transactional(readOnly = true)
    public Page<StockTransaction> getTransactionsByEmployee(Long employeeId, Pageable pageable) {
        return stockTransactionRepository.findByEmployeeId(employeeId, pageable);
    }

    // Get recent transactions
    @Transactional(readOnly = true)
    public Page<StockTransaction> getRecentTransactions(Pageable pageable) {
        return stockTransactionRepository.findRecentTransactions(pageable);
    }

    // Get transactions by date range
    @Transactional(readOnly = true)
    public Page<StockTransaction> getTransactionsByDateRange(LocalDateTime startDate,
                                                             LocalDateTime endDate,
                                                             Pageable pageable) {
        return stockTransactionRepository.findByTransactionDateBetween(startDate, endDate, pageable);
    }

    // Search with filters
    @Transactional(readOnly = true)
    public Page<StockTransaction> searchTransactionsWithFilters(Long productId, Long employeeId,
                                                                TransactionType type,
                                                                LocalDateTime startDate,
                                                                LocalDateTime endDate,
                                                                Pageable pageable) {
        return stockTransactionRepository.findByFilters(productId, employeeId, type,
                startDate, endDate, pageable);
    }

    // Count transactions by employee
    @Transactional(readOnly = true)
    public Long countTransactionsByEmployee(Long employeeId) {
        return stockTransactionRepository.countByEmployeeId(employeeId);
    }
}

