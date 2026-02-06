package com.inventoryEmployee.demo.service;

import com.inventoryEmployee.demo.dto.request.DepartmentRequest;
import com.inventoryEmployee.demo.entity.Department;
import com.inventoryEmployee.demo.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    // 1. Create Department (Using DTO)
    public Department createDepartment(DepartmentRequest request) {
        Department department = Department.builder()
                .name(request.getName())
                .description(request.getDescription())
                .location(request.getLocation())
                .headOfDepartment(request.getHeadOfDepartment()) // Assuming you have this field
                .build();

        return departmentRepository.save(department);
    }

    // 2. Get All
    public List<Department> getAllDepartments() {
        return departmentRepository.findByDeletedFalse();
    }

    // 3. Get One
    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));
    }

    // 4. Update (Using DTO)
    public Department updateDepartment(Long id, DepartmentRequest request) {
        Department existing = getDepartmentById(id);

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setLocation(request.getLocation());
        existing.setHeadOfDepartment(request.getHeadOfDepartment());

        return departmentRepository.save(existing);
    }

    // 5. Delete
    public void deleteDepartment(Long id) {
        Department existing = getDepartmentById(id);
        existing.setDeleted(true);
        departmentRepository.save(existing);
    }
}
