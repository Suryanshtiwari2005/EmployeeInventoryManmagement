package com.inventoryEmployee.demo.repository;
import com.inventoryEmployee.demo.entity.Employee;
import com.inventoryEmployee.demo.enums.EmployeeStatus;
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
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Find by email (for login/unique check)
    Optional<Employee> findByEmail(String email);

    // Find by department
    List<Employee> findByDepartmentId(Long departmentId);

    // Find active employees (not soft deleted)
    List<Employee> findByDeletedFalse();

    Page<Employee> findByDeletedFalse(Pageable pageable);

    // Find by status and not deleted
    List<Employee> findByStatusAndDeletedFalse(EmployeeStatus status);

    // Search by name (first or last name)
    @Query("SELECT e FROM Employee e WHERE " +
            "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Employee> searchByName(@Param("searchTerm") String searchTerm, Pageable pageable);


    // Count employees by department
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department.id = :departmentId AND e.deleted = false")
    Long countEmployeesByDepartment(@Param("departmentId") Long departmentId);

    // Count active employees
    Long countByStatusAndDeletedFalse(EmployeeStatus status);

    // Find employees hired this month
    @Query("SELECT e FROM Employee e WHERE MONTH(e.hireDate) = MONTH(CURRENT_DATE) " +
            "AND YEAR(e.hireDate) = YEAR(CURRENT_DATE) AND e.deleted = false")
    List<Employee> findEmployeesHiredThisMonth();

    // In EmployeeRepository
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.hireDate BETWEEN :startDate AND :endDate")
    Long countEmployeesHiredThisMonth(@Param("startDate") LocalDate start, @Param("endDate") LocalDate end);

    // Custom search with multiple filters
    @Query("SELECT e FROM Employee e WHERE " +
            "(:departmentId IS NULL OR e.department.id = :departmentId) AND " +
            "(:status IS NULL OR e.status = :status) AND " +
            "(:searchTerm IS NULL OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
            "e.deleted = false")
    Page<Employee> findByFilters(@Param("departmentId") Long departmentId,
                                 @Param("status") EmployeeStatus status,
                                 @Param("searchTerm") String searchTerm,
                                 Pageable pageable);

}