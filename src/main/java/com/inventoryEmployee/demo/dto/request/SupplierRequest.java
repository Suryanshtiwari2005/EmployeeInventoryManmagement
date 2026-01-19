package com.inventoryEmployee.demo.dto.request;

import com.inventoryEmployee.demo.enums.SupplierStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierRequest {

    @NotBlank(message = "Supplier name is required")
    @Size(max = 200, message = "Supplier name must not exceed 200 characters")
    private String name;

    @Size(max = 100, message = "Contact person must not exceed 100 characters")
    private String contactPerson;

    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Invalid phone number")
    private String phone;

    private String alternatePhone;
    private String address;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private String taxId;
    private String notes;

    private SupplierStatus status;
}
