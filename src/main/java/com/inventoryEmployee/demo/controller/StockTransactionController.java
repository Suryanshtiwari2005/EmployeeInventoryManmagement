package com.inventoryEmployee.demo.controller;

import com.inventoryEmployee.demo.entity.StockTransaction;
import com.inventoryEmployee.demo.enums.TransactionType;
import com.inventoryEmployee.demo.service.StockTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StockTransactionController {

    private final StockTransactionService stockTransactionService;

    // Get transactions by product
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<StockTransaction>> getTransactionsByProduct(
            @PathVariable Long productId,
            Pageable pageable) {
        Page<StockTransaction> transactions = stockTransactionService
                .getTransactionsByProduct(productId, pageable);
        return ResponseEntity.ok(transactions);
    }

    // Get transactions by employee
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<StockTransaction>> getTransactionsByEmployee(
            @PathVariable Long employeeId,
            Pageable pageable) {
        Page<StockTransaction> transactions = stockTransactionService
                .getTransactionsByEmployee(employeeId, pageable);
        return ResponseEntity.ok(transactions);
    }

    // Get recent transactions
    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<StockTransaction>> getRecentTransactions(Pageable pageable) {
        Page<StockTransaction> transactions = stockTransactionService
                .getRecentTransactions(pageable);
        return ResponseEntity.ok(transactions);
    }

    // Get transactions by date range
    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<StockTransaction>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        Page<StockTransaction> transactions = stockTransactionService
                .getTransactionsByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(transactions);
    }

    // Search with filters
    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<StockTransaction>> filterTransactions(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        Page<StockTransaction> transactions = stockTransactionService
                .searchTransactionsWithFilters(productId, employeeId, type,
                        startDate, endDate, pageable);
        return ResponseEntity.ok(transactions);
    }

    // Count transactions by employee
    @GetMapping("/count/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Long> countTransactionsByEmployee(@PathVariable Long employeeId) {
        Long count = stockTransactionService.countTransactionsByEmployee(employeeId);
        return ResponseEntity.ok(count);
    }
}
