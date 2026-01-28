package com.inventoryEmployee.demo.service;

import com.inventoryEmployee.demo.dto.request.OrderRequest;
import com.inventoryEmployee.demo.entity.Employee;
import com.inventoryEmployee.demo.entity.*;
import com.inventoryEmployee.demo.enums.OrderStatus;
import com.inventoryEmployee.demo.exception.ResourceNotFoundException;
import com.inventoryEmployee.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final EmployeeRepository employeeRepository;
    private final SupplierRepository supplierRepository;
    private final AuditService auditService;
    private final InventoryService inventoryService;

    // Create new order
    public Order createOrder(OrderRequest request, Employee employee) {
        log.info("Creating new order: {}", request.getOrderNumber());

        // Check if order number exists
        if (orderRepository.findByOrderNumber(request.getOrderNumber()).isPresent()) {
            throw new IllegalArgumentException("Order with number " + request.getOrderNumber() + " already exists");
        }

        // Build order
        Order order = Order.builder()
                .orderNumber(request.getOrderNumber())
                .orderDate(request.getOrderDate() != null ? request.getOrderDate() : LocalDateTime.now())
                .status(request.getStatus() != null ? request.getStatus() : OrderStatus.PENDING)
                .orderType(request.getOrderType())
                .taxAmount(request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO)
                .discountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO)
                .notes(request.getNotes())
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .employee(employee)
                .build();

        // Set supplier if provided
        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
            order.setSupplier(supplier);
        }

        // Create order items
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (var itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            // --- CRITICAL FIX START ---
            // 1. Check and Remove Stock
            // We pass "SALES" as the reason and the order number as a note
            inventoryService.removeStock(
                    product.getId(),
                    itemRequest.getQuantity(),
                    com.inventoryEmployee.demo.enums.StockMovementReason.SALES,
                    "Order #" + request.getOrderNumber(),
                    employee
            );
            // --- CRITICAL FIX END ---

            BigDecimal subtotal = itemRequest.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            BigDecimal discountAmount = subtotal
                    .multiply(itemRequest.getDiscountPercent() != null ? itemRequest.getDiscountPercent() : BigDecimal.ZERO)
                    .divide(BigDecimal.valueOf(100));

            BigDecimal finalAmount = subtotal.subtract(discountAmount);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(itemRequest.getUnitPrice())
                    .subtotal(subtotal)
                    .discountPercent(itemRequest.getDiscountPercent())
                    .discountAmount(discountAmount)
                    .finalAmount(finalAmount)
                    .build();

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(finalAmount);
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);
        order.setFinalAmount(totalAmount.add(order.getTaxAmount()).subtract(order.getDiscountAmount()));

        Order savedOrder = orderRepository.save(order);
        auditService.logAction("Order", savedOrder.getId(), "CREATE", null, savedOrder);

        return savedOrder;
    }

    // Get order by ID
    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .filter(order -> !order.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    // Get order by order number
    @Transactional(readOnly = true)
    public Order getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .filter(order -> !order.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));
    }

    // Get all orders
    @Transactional(readOnly = true)
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findRecentOrders(pageable);
    }

    // Update order status
    public Order updateOrderStatus(Long id, OrderStatus newStatus) {
        Order order = getOrderById(id);
        OrderStatus oldStatus = order.getStatus();

        log.info("Updating order {} status from {} to {}", id, oldStatus, newStatus);

        order.setStatus(newStatus);

        // If status is DELIVERED, set actual delivery date
        if (newStatus == OrderStatus.DELIVERED) {
            order.setActualDeliveryDate(LocalDateTime.now());
        }

        Order updated = orderRepository.save(order);
        auditService.logAction("Order", id, "UPDATE_STATUS", oldStatus, newStatus);

        return updated;
    }

    // Delete order (soft delete)
    public void deleteOrder(Long id) {
        Order order = getOrderById(id);
        log.info("Soft deleting order: {}", id);

        // --- CRITICAL FIX START ---
        // If order was not already cancelled, return stock
        if (order.getStatus() != OrderStatus.CANCELLED) {
            for (OrderItem item : order.getOrderItems()) {
                inventoryService.addStock(
                        item.getProduct().getId(),
                        item.getQuantity(),
                        com.inventoryEmployee.demo.enums.StockMovementReason.RETURNED,
                        "Order Cancelled #" + order.getOrderNumber(),
                        order.getEmployee().getId() // Assuming addStock takes ID, check your InventoryService signature
                );
            }
        }
        // --- CRITICAL FIX END ---

        order.setDeleted(true);
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        auditService.logAction("Order", id, "DELETE", order, null);
    }

    // Get orders by status
    @Transactional(readOnly = true)
    public Page<Order> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }

    // Get pending orders
    @Transactional(readOnly = true)
    public List<Order> getPendingOrders() {
        return orderRepository.findPendingOrders();
    }

    // Count orders by status
    @Transactional(readOnly = true)
    public Long countOrdersByStatus(OrderStatus status) {
        return orderRepository.countByStatusAndDeletedFalse(status);
    }
}
