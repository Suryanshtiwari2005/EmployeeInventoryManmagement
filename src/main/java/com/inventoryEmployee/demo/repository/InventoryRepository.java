package com.inventoryEmployee.demo.repository;

import com.inventoryEmployee.demo.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    // Find by product ID
    Optional<Inventory> findByProductId(Long productId);

    // Find by location
    List<Inventory> findByLocation(String location);

    // Find active inventory items
    List<Inventory> findByIsActiveTrue();
    Page<Inventory> findByIsActiveTrue(Pageable pageable);

    // Find low stock items (quantity below minimum)
    @Query("SELECT i FROM Inventory i WHERE i.quantityAvailable <= i.minStockLevel " +
            "AND i.isActive = true AND i.product.deleted = false")
    List<Inventory> findLowStockItems();

    // Find out of stock items
    @Query("SELECT i FROM Inventory i WHERE i.quantityAvailable = 0 " +
            "AND i.isActive = true AND i.product.deleted = false")
    List<Inventory> findOutOfStockItems();

    // Find overstocked items
    @Query("SELECT i FROM Inventory i WHERE i.quantityAvailable > i.maxStockLevel " +
            "AND i.isActive = true AND i.product.deleted = false")
    List<Inventory> findOverstockedItems();

    // Calculate total inventory value
    @Query("SELECT SUM(i.quantityAvailable * p.price) FROM Inventory i " +
            "JOIN i.product p WHERE i.isActive = true AND p.deleted = false")
    Double calculateTotalInventoryValue();

    // Count low stock items
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.quantityAvailable <= i.minStockLevel " +
            "AND i.isActive = true AND i.product.deleted = false")
    Long countLowStockItems();

    // Count out of stock items
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.quantityAvailable = 0 " +
            "AND i.isActive = true AND i.product.deleted = false")
    Long countOutOfStockItems();

    // Find by location and low stock
    @Query("SELECT i FROM Inventory i WHERE i.location = :location " +
            "AND i.quantityAvailable <= i.minStockLevel " +
            "AND i.isActive = true AND i.product.deleted = false")
    List<Inventory> findLowStockByLocation(@Param("location") String location);

    // Search inventory with filters
    @Query("SELECT i FROM Inventory i JOIN i.product p WHERE " +
            "(:location IS NULL OR i.location = :location) AND " +
            "(:searchTerm IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
            "i.isActive = true AND p.deleted = false")
    Page<Inventory> findByFilters(@Param("location") String location,
                                  @Param("searchTerm") String searchTerm,
                                  Pageable pageable);
}