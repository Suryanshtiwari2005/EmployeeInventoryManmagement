package com.inventoryEmployee.demo.service;

import com.inventoryEmployee.demo.entity.Employee;
import com.inventoryEmployee.demo.entity.Inventory;
import com.inventoryEmployee.demo.entity.Product;
import com.inventoryEmployee.demo.enums.StockMovementReason;
import com.inventoryEmployee.demo.enums.TransactionType;
import com.inventoryEmployee.demo.exception.InsufficientStockException;
import com.inventoryEmployee.demo.exception.ResourceNotFoundException;
import com.inventoryEmployee.demo.repository.EmployeeRepository;
import com.inventoryEmployee.demo.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final StockTransactionService stockTransactionService;
    private final StockAlertService stockAlertService;
    private final AuditService auditService;

    @Autowired
    private EmployeeRepository employeeRepository;

    // Create inventory for a product
    public Inventory createInventoryForProduct(Product product) {
        log.info("Creating inventory for product: {}", product.getId());

        Inventory inventory = Inventory.builder()
                .product(product)
                .quantityAvailable(0)
                .minStockLevel(10)
                .maxStockLevel(1000)
                .reorderPoint(20)
                .reorderQuantity(50)
                .lowStockAlertEnabled(true)
                .isActive(true)
                .build();

        return inventoryRepository.save(inventory);
    }

    // Get inventory by ID
    @Transactional(readOnly = true)
    public Inventory getInventoryById(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));
    }

    // Get inventory by product ID
    @Transactional(readOnly = true)
    public Inventory getInventoryByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product: " + productId));
    }

    // Get all inventory
    @Transactional(readOnly = true)
    public Page<Inventory> getAllInventory(Pageable pageable) {
        return inventoryRepository.findByIsActiveTrue(pageable);
    }

    // Update inventory settings
    public Inventory updateInventorySettings(Long id, Inventory updatedInventory) {
        Inventory existingInventory = getInventoryById(id);

        log.info("Updating inventory settings: {}", id);

        existingInventory.setMinStockLevel(updatedInventory.getMinStockLevel());
        existingInventory.setMaxStockLevel(updatedInventory.getMaxStockLevel());
        existingInventory.setReorderPoint(updatedInventory.getReorderPoint());
        existingInventory.setReorderQuantity(updatedInventory.getReorderQuantity());
        existingInventory.setLocation(updatedInventory.getLocation());
        existingInventory.setBinNumber(updatedInventory.getBinNumber());
        existingInventory.setRackNumber(updatedInventory.getRackNumber());
        existingInventory.setLowStockAlertEnabled(updatedInventory.getLowStockAlertEnabled());

        return inventoryRepository.save(existingInventory);
    }

    // Add stock (IN transaction)
    public Inventory addStock(Long productId, Integer quantity, StockMovementReason reason,
                              String notes, Long employeeId) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        // 2. Fetch the REAL employee from the DB
        // This ensures the object has 'email', 'hireDate', and is fully valid.
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));

        Inventory inventory = getInventoryByProductId(productId);
        int previousQuantity = inventory.getQuantityAvailable();
        int newQuantity = previousQuantity + quantity;

        log.info("Adding {} units to product {}", quantity, productId);

        // Update inventory
        inventory.setQuantityAvailable(newQuantity);
        inventory.setLastRestockDate(LocalDateTime.now());
        Inventory savedInventory = inventoryRepository.save(inventory);

        // Record transaction
        stockTransactionService.recordTransaction(
                inventory.getProduct(), employee, TransactionType.IN, reason,
                quantity, previousQuantity, newQuantity, notes
        );

        // Check and resolve alerts if stock is now sufficient
        stockAlertService.checkAndResolveAlerts(inventory);

        auditService.logAction("Inventory", inventory.getId(), "ADD_STOCK",
                previousQuantity, newQuantity);

        return savedInventory;
    }

    // Remove stock (OUT transaction)
    public Inventory removeStock(Long productId, Integer quantity, StockMovementReason reason,
                                 String notes, Employee employee) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        Inventory inventory = getInventoryByProductId(productId);
        int previousQuantity = inventory.getQuantityAvailable();

        // Check if sufficient stock available
        if (previousQuantity < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock. Available: " + previousQuantity + ", Required: " + quantity
            );
        }

        int newQuantity = previousQuantity - quantity;

        log.info("Removing {} units from product {}", quantity, productId);

        // Update inventory
        inventory.setQuantityAvailable(newQuantity);
        inventory.setLastSaleDate(LocalDateTime.now());
        Inventory savedInventory = inventoryRepository.save(inventory);

        // Record transaction
        stockTransactionService.recordTransaction(
                inventory.getProduct(), employee, TransactionType.OUT, reason,
                quantity, previousQuantity, newQuantity, notes
        );

        // Check if low stock alert needed
        stockAlertService.checkAndCreateAlerts(inventory);

        auditService.logAction("Inventory", inventory.getId(), "REMOVE_STOCK",
                previousQuantity, newQuantity);

        return savedInventory;
    }

    // Adjust stock (ADJUSTMENT transaction) - for corrections
    public Inventory adjustStock(Long productId, Integer newQuantity, StockMovementReason reason,
                                 String notes, Employee employee) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        Inventory inventory = getInventoryByProductId(productId);
        int previousQuantity = inventory.getQuantityAvailable();
        int difference = newQuantity - previousQuantity;

        log.info("Adjusting stock for product {} from {} to {}", productId, previousQuantity, newQuantity);

        // Update inventory
        inventory.setQuantityAvailable(newQuantity);
        Inventory savedInventory = inventoryRepository.save(inventory);

        // Record transaction
        stockTransactionService.recordTransaction(
                inventory.getProduct(), employee, TransactionType.ADJUSTMENT, reason,
                Math.abs(difference), previousQuantity, newQuantity, notes
        );

        // Check alerts
        stockAlertService.checkAndCreateAlerts(inventory);
        stockAlertService.checkAndResolveAlerts(inventory);

        auditService.logAction("Inventory", inventory.getId(), "ADJUST_STOCK",
                previousQuantity, newQuantity);

        return savedInventory;
    }

    // Get low stock items
    @Transactional(readOnly = true)
    public List<Inventory> getLowStockItems() {
        return inventoryRepository.findLowStockItems();
    }

    // Get out of stock items
    @Transactional(readOnly = true)
    public List<Inventory> getOutOfStockItems() {
        return inventoryRepository.findOutOfStockItems();
    }

    // Get overstocked items
    @Transactional(readOnly = true)
    public List<Inventory> getOverstockedItems() {
        return inventoryRepository.findOverstockedItems();
    }

    // Calculate total inventory value
    @Transactional(readOnly = true)
    public Double calculateTotalInventoryValue() {
        Double value = inventoryRepository.calculateTotalInventoryValue();
        return value != null ? value : 0.0;
    }

    // Count low stock items
    @Transactional(readOnly = true)
    public Long countLowStockItems() {
        return inventoryRepository.countLowStockItems();
    }

    // Count out of stock items
    @Transactional(readOnly = true)
    public Long countOutOfStockItems() {
        return inventoryRepository.countOutOfStockItems();
    }

    // Search inventory with filters
    @Transactional(readOnly = true)
    public Page<Inventory> searchInventoryWithFilters(String location, String searchTerm,
                                                      Pageable pageable) {
        return inventoryRepository.findByFilters(location, searchTerm, pageable);
    }
}
