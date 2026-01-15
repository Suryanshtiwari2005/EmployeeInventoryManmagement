package com.inventoryEmployee.demo.controller;

import com.inventoryEmployee.demo.dto.InventoryRequest;
import com.inventoryEmployee.demo.dto.StockAdjustmentRequest;
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

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InventoryController {

    private final InventoryService inventoryService;
    private final EmployeeService employeeService;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;


    // Helper method to get employee from authentication
    private Employee getEmployeeFromAuth(Authentication authentication) {
        String username = authentication.getName();
        return employeeRepository.findByEmail(username)
                .orElseGet(() -> {
                    // Create a system user if no employee found
                    Employee systemUser = Employee.builder()
                            .firstName("System")
                            .lastName("User")
                            .email(username)
                            .phone("0000000000")
                            .position("System")
                            .build();
                    return employeeRepository.save(systemUser);
                });
    }

    // Get inventory by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Inventory> getInventoryById(@PathVariable Long id) {
        Inventory inventory = inventoryService.getInventoryById(id);
        return ResponseEntity.ok(inventory);
    }

    // Get inventory by product ID
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Inventory> getInventoryByProductId(@PathVariable Long productId) {
        Inventory inventory = inventoryService.getInventoryByProductId(productId);
        return ResponseEntity.ok(inventory);
    }

    // Get all inventory
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Page<Inventory>> getAllInventory(Pageable pageable) {
        Page<Inventory> inventory = inventoryService.getAllInventory(pageable);
        return ResponseEntity.ok(inventory);
    }

    // Update inventory settings
    @PutMapping("/{id}/settings")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Inventory> updateInventorySettings(
            @PathVariable Long id,
            @Valid @RequestBody InventoryRequest request) {
        Inventory inventoryEntity = mapInventoryRequesttoEntity(request);

        Inventory updated = inventoryService.updateInventorySettings(id, inventoryEntity);
        return ResponseEntity.ok(updated);
    }

    // Add stock (IN transaction)
    @PostMapping("/add-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Inventory> addStock(
            @RequestBody StockAdjustmentRequest request,
            Authentication authentication) {

        // Get employee from authentication
        Employee employee = getEmployeeFromAuth(authentication);

        // Use the quantity field for ADD operation
        Inventory inventory = inventoryService.addStock(
                request.getProductId(),
                request.getQuantity(),  // Use quantity, not newQuantity
                request.getReason(),
                request.getNotes(),
                employee.getId()
        );

        return ResponseEntity.ok(inventory);

//        Long productId = request.getProductId();
//        Integer quantity = request.getNewQuantity();
//        StockMovementReason reason = request.getReason();
//        String notes = request.getNotes();
//
//        System.out.print("Add Stock Called");
////------------------------------------------
//        // 2. FIXED LOGIC: Get User first, then Employee
//        String currentUsername = authentication.getName();
//
//        // Find the User credential first
//        User user = userRepository.findByUsername(currentUsername)
//                .orElseThrow(() -> new RuntimeException("User account not found"));
//
//        // Get the Employee profile linked to this User
//        Employee employee = user.getEmployee();
//
//        // Safety check in case the link is broken
//        if (employee == null) {
//            throw new RuntimeException("This user is not linked to any Employee profile");
//        }
////------------------------------------------
//        Inventory inventory = inventoryService.addStock(productId, quantity, reason, notes, employee.getId());
//        return ResponseEntity.ok(inventory);
    }

    // Remove stock (OUT transaction)
    @PostMapping("/remove-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Inventory> removeStock(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {

        Long productId = Long.valueOf(request.get("productId").toString());
        Integer quantity = Integer.valueOf(request.get("quantity").toString());
        StockMovementReason reason = StockMovementReason.valueOf(request.get("reason").toString());
        String notes = request.get("notes") != null ? request.get("notes").toString() : null;

        Employee employee = getEmployeeFromAuth(authentication);

//------------------------------------------------------------------------------------------------------------------------------
        Inventory inventory = inventoryService.removeStock(productId, quantity, reason, notes, employee);
        return ResponseEntity.ok(inventory);
    }

    // Adjust stock (ADJUSTMENT transaction)
    @PostMapping("/adjust-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Inventory> adjustStock(
            @RequestBody StockAdjustmentRequest request,
            Authentication authentication) {

        // Get employee from authentication
        Employee employee = getEmployeeFromAuth(authentication);

        // Use the quantity field for ADD operation
        Inventory inventory = inventoryService.addStock(
                request.getProductId(),
                request.getQuantity(),  // Use quantity, not newQuantity
                request.getReason(),
                request.getNotes(),
                employee
        );
        Employee employee = getEmployeeFromAuth(authentication);

        Inventory inventory = inventoryService.adjustStock(productId, newQuantity, reason, notes, employee);
        return ResponseEntity.ok(inventory);
    }

    // Get low stock items
    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Inventory>> getLowStockItems() {
        List<Inventory> inventory = inventoryService.getLowStockItems();
        return ResponseEntity.ok(inventory);
    }

    // Get out of stock items
    @GetMapping("/out-of-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Inventory>> getOutOfStockItems() {
        List<Inventory> inventory = inventoryService.getOutOfStockItems();
        return ResponseEntity.ok(inventory);
    }

    // Get overstocked items
    @GetMapping("/overstocked")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Inventory>> getOverstockedItems() {
        List<Inventory> inventory = inventoryService.getOverstockedItems();
        return ResponseEntity.ok(inventory);
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
    public ResponseEntity<Page<Inventory>> filterInventory(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String searchTerm,
            Pageable pageable) {
        Page<Inventory> inventory = inventoryService.searchInventoryWithFilters(
                location, searchTerm, pageable);
        return ResponseEntity.ok(inventory);
    }

    private Inventory maptoEntity(StockAdjustmentRequest request){
        Inventory inventory = new Inventory();

        if (request.getProductId() != null) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            inventory.setProduct(product);
        }

        if (request.getNewQuantity() != null) {
            inventory.setQuantityAvailable(request.getNewQuantity());
        } else {
            inventory.setQuantityAvailable(request.getQuantity());
        }

        return inventory;
    }

    private Inventory mapInventoryRequesttoEntity(InventoryRequest request){
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
}
