package com.inventoryEmployee.demo.service;

import com.inventoryEmployee.demo.entity.Permission;
import com.inventoryEmployee.demo.exception.ResourceNotFoundException;
import com.inventoryEmployee.demo.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PermissionService {

    private final PermissionRepository permissionRepository;

    // Create new permission
    public Permission createPermission(String name, String description, String module) {
        log.info("Creating new permission: {}", name);

        if (permissionRepository.existsByName(name)) {
            throw new IllegalArgumentException("Permission " + name + " already exists");
        }

        Permission permission = Permission.builder()
                .name(name)
                .description(description)
                .module(module)
                .build();

        return permissionRepository.save(permission);
    }

    // Get all permissions
    @Transactional(readOnly = true)
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    // Get permissions by module
    @Transactional(readOnly = true)
    public List<Permission> getPermissionsByModule(String module) {
        return permissionRepository.findByModule(module);
    }

    // Delete permission
    public void deletePermission(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found"));
        permissionRepository.delete(permission);
    }
}