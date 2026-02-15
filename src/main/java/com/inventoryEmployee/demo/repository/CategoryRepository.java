package com.inventoryEmployee.demo.repository;

import com.inventoryEmployee.demo.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Find root categories (no parent)
    List<Category> findByParentCategoryIsNull();

    // Find subcategories
    List<Category> findByParentCategoryId(Long parentCategoryId);

    // Find active categories (not soft deleted)
    Page<Category> findByDeletedFalse(Pageable pageable);

    // Check if category name exists
    boolean existsByNameAndDeletedFalse(String name);

    // Search by name
    @Query("SELECT c FROM Category c WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND " +
            "c.deleted = false")
    Page<Category> searchByName(String searchTerm, Pageable pageable);

    // Get categories with product count
    @Query("SELECT c, COUNT(p) FROM Category c " +
            "LEFT JOIN c.products p WHERE p.deleted = false " +
            "GROUP BY c")
    List<Object[]> findCategoriesWithProductCount();
}
