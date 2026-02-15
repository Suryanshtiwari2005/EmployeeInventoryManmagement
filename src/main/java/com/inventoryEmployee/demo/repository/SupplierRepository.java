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


    Page<Supplier> findByDeletedFalse(Pageable pageable);

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
