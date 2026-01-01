package com.inventoryEmployee.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String name; // e.g., "INVENTORY_READ", "EMPLOYEE_WRITE"

    @Column(length = 255)
    private String description;

    @Column(length = 50)
    private String module; // INVENTORY, EMPLOYEE, REPORTS, etc.

    // Relationships
    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles = new HashSet<>();
}
