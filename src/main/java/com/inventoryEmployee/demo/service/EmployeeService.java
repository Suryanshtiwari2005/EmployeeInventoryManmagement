package com.inventoryEmployee.demo.service;

import com.inventoryEmployee.demo.entity.Employee;
import com.inventoryEmployee.demo.enums.EmployeeStatus;
import com.inventoryEmployee.demo.exception.ResourceNotFoundException;
import com.inventoryEmployee.demo.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;

    // Create new employee
    public Employee createEmployee(Employee employee) {
        log.info("Creating new employee: {}", employee.getEmail());

        // Check if email already exists
        if (employeeRepository.findByEmail(employee.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Employee with email " + employee.getEmail() + " already exists");
        }

        Employee savedEmployee = employeeRepository.save(employee);
        auditService.logAction("Employee", savedEmployee.getId(), "CREATE", null, savedEmployee);

        return savedEmployee;
    }

    // Get employee by ID
    @Transactional(readOnly = true)
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .filter(emp -> !emp.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    // Get all employees
    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        return employeeRepository.findByDeletedFalse();
    }

    // Get employees with pagination
    @Transactional(readOnly = true)
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findByDeletedFalse(pageable);
    }

    // Update employee
    public Employee updateEmployee(Long id, Employee updatedEmployee) {
        Employee existingEmployee = getEmployeeById(id);

        log.info("Updating employee: {}", id);

        // Update fields
        existingEmployee.setFirstName(updatedEmployee.getFirstName());
        existingEmployee.setLastName(updatedEmployee.getLastName());
        existingEmployee.setEmail(updatedEmployee.getEmail());
        existingEmployee.setPhone(updatedEmployee.getPhone());
        existingEmployee.setPosition(updatedEmployee.getPosition());
        existingEmployee.setSalary(updatedEmployee.getSalary());
        existingEmployee.setAddress(updatedEmployee.getAddress());
        existingEmployee.setCity(updatedEmployee.getCity());
        existingEmployee.setState(updatedEmployee.getState());
        existingEmployee.setZipCode(updatedEmployee.getZipCode());
        existingEmployee.setStatus(updatedEmployee.getStatus());
        existingEmployee.setDepartment(updatedEmployee.getDepartment());

        Employee saved = employeeRepository.save(existingEmployee);
        auditService.logAction("Employee", id, "UPDATE", existingEmployee, saved);

        return saved;
    }

    // Soft delete employee
    public void deleteEmployee(Long id) {
        Employee employee = getEmployeeById(id);
        log.info("Soft deleting employee: {}", id);

        employee.setDeleted(true);
        employee.setStatus(EmployeeStatus.TERMINATED);
        employeeRepository.save(employee);

        auditService.logAction("Employee", id, "DELETE", employee, null);
    }

    // Search employees by name
    @Transactional(readOnly = true)
    public Page<Employee> searchEmployeesByName(String searchTerm, Pageable pageable) {
        return employeeRepository.searchByName(searchTerm, pageable);
    }

    // Get employees by department
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByDepartment(Long departmentId) {
        return employeeRepository.findByDepartmentId(departmentId);
    }

    // Get employees by status
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByStatus(EmployeeStatus status) {
        return employeeRepository.findByStatusAndDeletedFalse(status);
    }

    // Get employees hired this month
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesHiredThisMonth() {
        return employeeRepository.findEmployeesHiredThisMonth();
    }

    // Advanced search with filters
    @Transactional(readOnly = true)
    public Page<Employee> searchEmployeesWithFilters(Long departmentId, EmployeeStatus status,
                                                     String searchTerm, Pageable pageable) {
        return employeeRepository.findByFilters(departmentId, status, searchTerm, pageable);
    }

    // Count employees by department
    @Transactional(readOnly = true)
    public Long countEmployeesByDepartment(Long departmentId) {
        return employeeRepository.countEmployeesByDepartment(departmentId);
    }

    // Count active employees
    @Transactional(readOnly = true)
    public Long countActiveEmployees() {
        return employeeRepository.countByStatusAndDeletedFalse(EmployeeStatus.ACTIVE);
    }
}
