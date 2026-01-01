package com.inventoryEmployee.demo.repository;

import com.inventoryEmployee.demo.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    // Find by permission name
    Optional<Permission> findByName(String name);

    // Find by module
    List<Permission> findByModule(String module);

    // Check if permission exists
    boolean existsByName(String name);
}
