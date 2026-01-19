package com.inventoryEmployee.demo.dto.response;

import com.inventoryEmployee.demo.enums.EmployeeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String position;
    private BigDecimal salary;
    private LocalDate hireDate;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private EmployeeStatus status;

    private String departmentName;
    private Long departmentId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
