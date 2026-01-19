package com.inventoryEmployee.demo.controller;

import com.inventoryEmployee.demo.dto.request.DepartmentRequest;
import com.inventoryEmployee.demo.entity.Department;
import com.inventoryEmployee.demo.repository.DepartmentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    // Create department
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Department> createDepartment(@Valid @RequestBody Department department) {
        Department created = departmentRepository.save(department);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // Get department by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Department> getDepartmentById(@PathVariable Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        return ResponseEntity.ok(department);
    }

    // Get all departments
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Page<Department>> getAllDepartments(Pageable pageable) {
        Page<Department> departments = departmentRepository.findByDeletedFalse(pageable);
        return ResponseEntity.ok(departments);
    }

    // Update department
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Department> updateDepartment(@PathVariable Long id,
                                                       @Valid @RequestBody Department department) {
        Department existing = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        existing.setName(department.getName());
        existing.setDescription(department.getDescription());
        existing.setLocation(department.getLocation());
        existing.setHeadOfDepartment(department.getHeadOfDepartment());

        Department updated = departmentRepository.save(existing);
        return ResponseEntity.ok(updated);
    }

    // Delete department (soft delete)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        department.setDeleted(true);
        departmentRepository.save(department);
        return ResponseEntity.noContent().build();
    }

    private Department maptoEntity(DepartmentRequest request){
        Department department = new Department();

        department.setName(request.getName());
        department.setDescription(request.getDescription());
        department.setLocation(request.getLocation());
        department.setHeadOfDepartment(request.getHeadOfDepartment());


        return department;
    }
}
