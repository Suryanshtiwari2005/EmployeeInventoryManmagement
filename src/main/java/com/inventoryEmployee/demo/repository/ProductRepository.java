package com.inventoryEmployee.demo.repository;

import com.inventoryEmployee.demo.entity.Product;
import com.inventoryEmployee.demo.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find by SKU
    Optional<Product> findBySku(String sku);

    // Find by barcode
    Optional<Product> findByBarcode(String barcode);

    // Find by category
    List<Product> findByCategoryId(Long categoryId);
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    // Find by supplier
    List<Product> findBySupplierId(Long supplierId);
    Page<Product> findBySupplierId(Long supplierId, Pageable pageable);

    // Find by status
    List<Product> findByStatus(ProductStatus status);
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    // Find active products (not soft deleted)
    List<Product> findByDeletedFalse();
    Page<Product> findByDeletedFalse(Pageable pageable);

    // Find by status and not deleted
    List<Product> findByStatusAndDeletedFalse(ProductStatus status);
    Page<Product> findByStatusAndDeletedFalse(ProductStatus status, Pageable pageable);

    // Search by name or SKU
    @Query("SELECT p FROM Product p WHERE " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
            "p.deleted = false")
    Page<Product> searchByNameOrSku(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Find by price range
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    // Find products expiring soon
    @Query("SELECT p FROM Product p WHERE p.expiryDate BETWEEN :startDate AND :endDate " +
            "AND p.deleted = false ORDER BY p.expiryDate ASC")
    List<Product> findProductsExpiringSoon(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    // Find expired products
    @Query("SELECT p FROM Product p WHERE p.expiryDate < :currentDate AND p.deleted = false")
    List<Product> findExpiredProducts(@Param("currentDate") LocalDate currentDate);

    // Check if SKU exists
    boolean existsBySkuAndDeletedFalse(String sku);

    // Check if barcode exists
    boolean existsByBarcodeAndDeletedFalse(String barcode);

    // Count products by category
    Long countByCategoryIdAndDeletedFalse(Long categoryId);

    // Count products by supplier
    Long countBySupplierIdAndDeletedFalse(Long supplierId);

    // Advanced search with filters
    @Query("SELECT p FROM Product p WHERE " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:supplierId IS NULL OR p.supplier.id = :supplierId) AND " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:searchTerm IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
            "p.deleted = false")
    Page<Product> findByFilters(@Param("categoryId") Long categoryId,
                                @Param("supplierId") Long supplierId,
                                @Param("status") ProductStatus status,
                                @Param("minPrice") BigDecimal minPrice,
                                @Param("maxPrice") BigDecimal maxPrice,
                                @Param("searchTerm") String searchTerm,
                                Pageable pageable);

    // GLOBAL SEARCH: Looks for text in Product Name, SKU, Description, Category Name, and Supplier Name
    @Query("SELECT p FROM Product p " +
            "LEFT JOIN p.category c " +
            "LEFT JOIN p.supplier s " +
            "WHERE " +
            "p.deleted = false AND (" +
            "   LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "   LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "   LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "   LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "   LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            ")")
    Page<Product> searchGlobal(@Param("keyword") String keyword, Pageable pageable);
}
