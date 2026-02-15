package com.inventoryEmployee.demo.controller;

import com.inventoryEmployee.demo.dto.request.InventoryRequest;
import com.inventoryEmployee.demo.dto.request.StockAdjustmentRequest;
import com.inventoryEmployee.demo.dto.response.InventoryResponse;
import com.inventoryEmployee.demo.entity.Employee;
import com.inventoryEmployee.demo.entity.Inventory;
import com.inventoryEmployee.demo.entity.Product;
import com.inventoryEmployee.demo.entity.User;
import com.inventoryEmployee.demo.enums.StockMovementReason;
import com.inventoryEmployee.demo.repository.EmployeeRepository;
import com.inventoryEmployee.demo.repository.ProductRepository;
import com.inventoryEmployee.demo.repository.UserRepository;
import com.inventoryEmployee.demo.service.EmployeeService;
import com.inventoryEmployee.demo.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InventoryController {

    private final InventoryService inventoryService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    private Employee getEmployeeFromAuth(Authentication authentication) {
        String username = authentication.getName(); // Returns "superadmin"

        // 1. Find the User credentials first
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // 2. Return the linked Employee profile
        if (user.getEmployee() == null) {
            throw new RuntimeException("User " + username + " is not linked to an employee profile.");
        }

        return user.getEmployee();
    }

    // Get inventory by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<InventoryResponse> getInventoryById(@PathVariable Long id) {
        Inventory inventory = inventoryService.getInventoryById(id);
        return ResponseEntity.ok(mapToResponse(inventory));
    }

    // Get inventory by product ID
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<InventoryResponse> getInventoryByProductId(@PathVariable Long productId) {
        Inventory inventory = inventoryService.getInventoryByProductId(productId);
        return ResponseEntity.ok(mapToResponse(inventory));
    }

    // Get all inventory
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Page<InventoryResponse>> getAllInventory(Pageable pageable) {
        Page<Inventory> inventory = inventoryService.getAllInventory(pageable);

        Page<InventoryResponse> responsePage = inventory.map(this::mapToResponse);

        return ResponseEntity.ok(responsePage);
    }

    // Update inventory settings
    @PutMapping("/{id}/settings")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<InventoryResponse> updateInventorySettings(
            @PathVariable Long id,
            @Valid @RequestBody InventoryRequest request) {
        Inventory inventoryEntity = mapInventoryRequestEntity(request);

        Inventory updated = inventoryService.updateInventorySettings(id, inventoryEntity);
        return ResponseEntity.ok(mapToResponse(updated));
    }

    // Add stock (IN transaction)
    @PostMapping("/add-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<InventoryResponse> addStock(
            @RequestBody StockAdjustmentRequest request,
            Authentication authentication) {

        // Get employee from authentication
        Employee employee = getEmployeeFromAuth(authentication);

        // Use the quantity field for ADD operation
        Inventory inventory = inventoryService.addStock(
                request.getProductId(),
                request.getQuantity(),
                request.getReason(),
                request.getNotes(),
                employee.getId()
        );

        return ResponseEntity.ok(mapToResponse(inventory));

    }

    // Remove stock (OUT transaction)
    @PostMapping("/remove-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<InventoryResponse> removeStock(
            @RequestBody StockAdjustmentRequest request,
            Authentication authentication) {

        Employee employee = getEmployeeFromAuth(authentication);
        Inventory inventory = inventoryService.removeStock(
                request.getProductId(),
                request.getQuantity(),
                request.getReason(),
                request.getNotes(),
                employee
        );
        return ResponseEntity.ok(mapToResponse(inventory));
    }

    // Adjust stock (ADJUSTMENT transaction)
    @PostMapping("/adjust-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<InventoryResponse> adjustStock(
            @RequestBody StockAdjustmentRequest request,
            Authentication authentication) {

        // Get employee from authentication
        Employee employee = getEmployeeFromAuth(authentication);

        // Use the quantity field for ADD operation
        Inventory inventory = inventoryService.adjustStock(
                request.getProductId(),
                request.getNewQuantity(),  // Use quantity, not newQuantity
                request.getReason(),
                request.getNotes(),
                employee
        );

        return ResponseEntity.ok(mapToResponse(inventory));
    }

    // Get low stock items
    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<InventoryResponse>> getLowStockItems() {
        List<Inventory> inventory = inventoryService.getLowStockItems();

        List<InventoryResponse> responseList = inventory.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    // Get out of stock items
    @GetMapping("/out-of-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<InventoryResponse>> getOutOfStockItems() {
        List<Inventory> inventory = inventoryService.getOutOfStockItems();

        List<InventoryResponse> responseList = inventory.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    // Get overstocked items
    @GetMapping("/overstocked")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<InventoryResponse>> getOverstockedItems() {
        List<Inventory> inventory = inventoryService.getOverstockedItems();

        List<InventoryResponse> responseList = inventory.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    // Calculate total inventory value
    @GetMapping("/total-value")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Double> getTotalInventoryValue() {
        Double value = inventoryService.calculateTotalInventoryValue();
        return ResponseEntity.ok(value);
    }

    // Count low stock items
    @GetMapping("/count/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Long> countLowStockItems() {
        Long count = inventoryService.countLowStockItems();
        return ResponseEntity.ok(count);
    }

    // Count out of stock items
    @GetMapping("/count/out-of-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Long> countOutOfStockItems() {
        Long count = inventoryService.countOutOfStockItems();
        return ResponseEntity.ok(count);
    }

    // Search inventory with filters
    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Page<InventoryResponse>> filterInventory(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String searchTerm,
            Pageable pageable) {
        Page<Inventory> inventory = inventoryService.searchInventoryWithFilters(
                location, searchTerm, pageable);

        Page<InventoryResponse> responsePage = inventory.map(this::mapToResponse);

        return ResponseEntity.ok(responsePage);
    }


    private Inventory mapInventoryRequestEntity(InventoryRequest request){
        Inventory inventory = new Inventory();

        inventory.setId(request.getProductId());
        inventory.setQuantityAvailable(request.getQuantityAvailable());
        inventory.setMinStockLevel(request.getMinStockLevel());
        inventory.setMaxStockLevel(request.getMaxStockLevel());
        inventory.setReorderPoint(request.getReorderPoint());
        inventory.setReorderQuantity(request.getReorderQuantity());

        inventory.setLocation(request.getLocation());
        inventory.setBinNumber(request.getBinNumber());
        inventory.setRackNumber(request.getRackNumber());

        inventory.setLowStockAlertEnabled(request.getLowStockAlertEnabled());
        inventory.setIsActive(request.getIsActive());

        if (request.getProductId() != null) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + request.getProductId()));
            inventory.setProduct(product);
        }

        return inventory;

    }

    private InventoryResponse mapToResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .id(inventory.getId())
                .productId(inventory.getProduct().getId())
                .productName(inventory.getProduct().getName())
                .productSku(inventory.getProduct().getSku())

                .quantityAvailable(inventory.getQuantityAvailable())
                .minStockLevel(inventory.getMinStockLevel())
                .maxStockLevel(inventory.getMaxStockLevel())
                .reorderPoint(inventory.getReorderPoint())
                .reorderQuantity(inventory.getReorderQuantity())

                .location(inventory.getLocation())
                .binNumber(inventory.getBinNumber())
                .rackNumber(inventory.getRackNumber())

                .lowStockAlertEnabled(inventory.getLowStockAlertEnabled())
                .isActive(inventory.getIsActive())

                .lastRestockDate(inventory.getLastRestockDate())
                .lastSaleDate(inventory.getLastSaleDate())

                // Logic for Calculated Fields
                .isLowStock(inventory.getQuantityAvailable() <= inventory.getMinStockLevel())
                .isOutOfStock(inventory.getQuantityAvailable() <= 0)
                .isOverstocked(inventory.getQuantityAvailable() > inventory.getMaxStockLevel())

                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}
