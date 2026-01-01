package com.inventoryEmployee.demo.repository;

import com.inventoryEmployee.demo.entity.Supplier;
import com.inventoryEmployee.demo.enums.SupplierStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    // Find by name
    Optional<Supplier> findByName(String name);

    // Find by email
    Optional<Supplier> findByEmail(String email);

    // Find by status
    List<Supplier> findByStatus(SupplierStatus status);
    Page<Supplier> findByStatus(SupplierStatus status, Pageable pageable);

    // Find active suppliers (not soft deleted)
    List<Supplier> findByDeletedFalse();
    Page<Supplier> findByDeletedFalse(Pageable pageable);

    // Find by city
    List<Supplier> findByCityIgnoreCase(String city);

    // Find by country
    List<Supplier> findByCountryIgnoreCase(String country);

    // Search by name
    @Query("SELECT s FROM Supplier s WHERE " +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND " +
            "s.deleted = false")
    Page<Supplier> searchByName(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Check if supplier exists
    boolean existsByEmailAndDeletedFalse(String email);

    // Count suppliers by status
    Long countByStatusAndDeletedFalse(SupplierStatus status);

    // Find suppliers with filters
    @Query("SELECT s FROM Supplier s WHERE " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(:city IS NULL OR LOWER(s.city) = LOWER(:city)) AND " +
            "(:searchTerm IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
            "s.deleted = false")
    Page<Supplier> findByFilters(@Param("status") SupplierStatus status,
                                 @Param("city") String city,
                                 @Param("searchTerm") String searchTerm,
                                 Pageable pageable);
}
