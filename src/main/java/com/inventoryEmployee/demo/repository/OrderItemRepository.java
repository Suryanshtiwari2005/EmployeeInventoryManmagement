package com.inventoryEmployee.demo.repository;

import com.inventoryEmployee.demo.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

//    // Find by order ID
//    List<OrderItem> findByOrderId(Long orderId);
//
//    // Find by product ID
//    List<OrderItem> findByProductId(Long productId);
//
//    // Count items in an order
//    Long countByOrderId(Long orderId);
//
//    // Get total quantity ordered for a product
//    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.product.id = :productId")
//    Integer getTotalQuantityOrdered(@Param("productId") Long productId);
}
