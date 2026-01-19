package com.inventoryEmployee.demo.controller;

import com.inventoryEmployee.demo.dto.request.EmployeeRequest;
import com.inventoryEmployee.demo.dto.response.EmployeeResponse;
import com.inventoryEmployee.demo.entity.Employee;
import com.inventoryEmployee.demo.entity.Department;
import com.inventoryEmployee.demo.enums.EmployeeStatus;
import com.inventoryEmployee.demo.repository.DepartmentRepository;
import com.inventoryEmployee.demo.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final DepartmentRepository departmentRepository;

    // Create employee
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest request) {

        Employee employeeEntity = mapToEntity(request);

        Employee created = employeeService.createEmployee(employeeEntity);
        return new ResponseEntity<>(mapToResponse(created), HttpStatus.CREATED);
    }

    // Get employee by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
        Employee employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(mapToResponse(employee));
    }

    // Get all employees with pagination
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<EmployeeResponse>> getAllEmployees(Pageable pageable) {
        Page<Employee> employees = employeeService.getAllEmployees(pageable);
        Page<EmployeeResponse> responsePage = employees.map(this::mapToResponse);
        return ResponseEntity.ok(responsePage);
    }

    // Update employee
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<EmployeeResponse> updateEmployee(@PathVariable Long id,
                                                   @Valid @RequestBody EmployeeRequest request) {
        Employee employeeEntity = mapToEntity(request);

        Employee updated = employeeService.updateEmployee(id, employeeEntity);
        return ResponseEntity.ok(mapToResponse(updated));
    }

    // Delete employee (soft delete)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    // Search employees by name
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<EmployeeResponse>> searchEmployees(
            @RequestParam String searchTerm,
            Pageable pageable) {
        Page<Employee> employees = employeeService.searchEmployeesByName(searchTerm, pageable);
        return ResponseEntity.ok(employees.map(this::mapToResponse));
    }

    // Get employees by department
    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<EmployeeResponse>> getEmployeesByDepartment(@PathVariable Long departmentId) {
        List<Employee> employees = employeeService.getEmployeesByDepartment(departmentId);

        List<EmployeeResponse> responseList = employees.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    // Get employees by status
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<EmployeeResponse>> getEmployeesByStatus(@PathVariable EmployeeStatus status) {
        List<Employee> employees = employeeService.getEmployeesByStatus(status);

        List<EmployeeResponse> responseList = employees.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    // Get employees hired this month
    @GetMapping("/hired-this-month")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<EmployeeResponse>> getEmployeesHiredThisMonth() {
        List<Employee> employees = employeeService.getEmployeesHiredThisMonth();

        List<EmployeeResponse> responseList = employees.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    // Advanced search with filters
    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<EmployeeResponse>> filterEmployees(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(required = false) String searchTerm,
            Pageable pageable) {
        Page<Employee> employees = employeeService.searchEmployeesWithFilters(
                departmentId, status, searchTerm, pageable);
        return ResponseEntity.ok(employees.map(this::mapToResponse));
    }

    // Count employees by department
    @GetMapping("/count/department/{departmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Long> countEmployeesByDepartment(@PathVariable Long departmentId) {
        Long count = employeeService.countEmployeesByDepartment(departmentId);
        return ResponseEntity.ok(count);
    }

    // Count active employees
    @GetMapping("/count/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Long> countActiveEmployees() {
        Long count = employeeService.countActiveEmployees();
        return ResponseEntity.ok(count);
    }

    private Employee mapToEntity(EmployeeRequest request){
        Employee employee = new Employee();

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setPosition(request.getPosition());
        employee.setSalary(request.getSalary());
        employee.setHireDate(request.getHireDate());
        employee.setAddress(request.getAddress());
        employee.setCity(request.getCity());
        employee.setState(request.getState());
        employee.setZipCode(request.getZipCode());
        employee.setStatus(request.getStatus());

        // Map Complex Logic: Department ID -> Department Entity
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            employee.setDepartment(department);
        }

        return employee;
    }

    private EmployeeResponse mapToResponse(Employee employee){
        return EmployeeResponse.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .position(employee.getPosition())
                .salary(employee.getSalary())
                .hireDate(employee.getHireDate())
                .address(employee.getAddress())
                .city(employee.getCity())
                .state(employee.getState())
                .zipCode(employee.getZipCode())
                .status(employee.getStatus())
                .departmentName(employee.getDepartment() != null ? employee.getDepartment().getName():null)
                .departmentId(employee.getDepartment() != null ? employee.getDepartment().getId():null)
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();

    }
}
