package com.inventoryEmployee.demo.controller;

import com.inventoryEmployee.demo.dto.request.OrderRequest;
import com.inventoryEmployee.demo.entity.Employee;
import com.inventoryEmployee.demo.entity.Order;
import com.inventoryEmployee.demo.entity.User;
import com.inventoryEmployee.demo.enums.OrderStatus;
import com.inventoryEmployee.demo.repository.UserRepository;
import com.inventoryEmployee.demo.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    // Helper method to get employee from authentication
    private Employee getEmployeeFromAuth(Authentication authentication) {
        String currentUsername = authentication.getName();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Employee employee = user.getEmployee();
        if (employee == null) {
            throw new RuntimeException("User is not linked to an employee profile");
        }
        return employee;
    }

    // Create order
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Order> createOrder(
            @Valid @RequestBody OrderRequest request,
            Authentication authentication) {
        Employee employee = getEmployeeFromAuth(authentication);
        Order created = orderService.createOrder(request, employee);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // Get order by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    // Get order by order number
    @GetMapping("/number/{orderNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Order> getOrderByOrderNumber(@PathVariable String orderNumber) {
        Order order = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(order);
    }

    // Get all orders
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<Order>> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    // Update order status
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        OrderStatus newStatus = OrderStatus.valueOf(request.get("status"));
        Order updated = orderService.updateOrderStatus(id, newStatus);
        return ResponseEntity.ok(updated);
    }

    // Delete order
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    // Get orders by status
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<Order>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            Pageable pageable) {
        Page<Order> orders = orderService.getOrdersByStatus(status, pageable);
        return ResponseEntity.ok(orders);
    }

    // Get pending orders
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Order>> getPendingOrders() {
        List<Order> orders = orderService.getPendingOrders();
        return ResponseEntity.ok(orders);
    }

    // Count orders by status
    @GetMapping("/count/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Long> countOrdersByStatus(@PathVariable OrderStatus status) {
        Long count = orderService.countOrdersByStatus(status);
        return ResponseEntity.ok(count);
    }
}
