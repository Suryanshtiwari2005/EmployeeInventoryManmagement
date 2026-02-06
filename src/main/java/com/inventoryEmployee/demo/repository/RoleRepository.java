package com.inventoryEmployee.demo.repository;

import com.inventoryEmployee.demo.entity.Role;
import com.inventoryEmployee.demo.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Find by role name
    Optional<Role> findByName(String name);

    // Check if role exists
    boolean existsByName(String name);
}
