package com.inventoryEmployee.demo.repository;

import com.inventoryEmployee.demo.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // Find by name
    Optional<Department> findByName(String name);

    // Find by location
    List<Department> findByLocation(String location);

    // Find active departments (not soft deleted)
    List<Department> findByDeletedFalse();
    Page<Department> findByDeletedFalse(Pageable pageable);

    // Search by name
    @Query("SELECT d FROM Department d WHERE " +
            "LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND " +
            "d.deleted = false")
    Page<Department> searchByName(String searchTerm, Pageable pageable);

    // Check if department name exists
    boolean existsByNameAndDeletedFalse(String name);

    // Get departments with employee count
    @Query("SELECT d, COUNT(e) FROM Department d " +
            "LEFT JOIN d.employees e WHERE e.deleted = false " +
            "GROUP BY d")
    List<Object[]> findDepartmentsWithEmployeeCount();
}
