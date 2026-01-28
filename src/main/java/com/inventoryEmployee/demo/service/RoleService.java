package com.inventoryEmployee.demo.service;

import com.inventoryEmployee.demo.entity.Permission;
import com.inventoryEmployee.demo.entity.Role;
import com.inventoryEmployee.demo.entity.User;
import com.inventoryEmployee.demo.enums.UserRole;
import com.inventoryEmployee.demo.exception.ResourceNotFoundException;
import com.inventoryEmployee.demo.repository.PermissionRepository;
import com.inventoryEmployee.demo.repository.RoleRepository;
import com.inventoryEmployee.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    // Create new role
    public Role createRole(UserRole roleName, String description) {
        log.info("Creating new role: {}", roleName);

        if (roleRepository.existsByName(roleName)) {
            throw new IllegalArgumentException("Role " + roleName + " already exists");
        }

        Role role = Role.builder()
                .name(roleName)
                .description(description)
                .permissions(new HashSet<>())
                .build();

        return roleRepository.save(role);
    }

    // Get all roles
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    // Get role by name
    @Transactional(readOnly = true)
    public Role getRoleByName(UserRole roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
    }

    // Add permission to role
    public Role addPermissionToRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found"));

        role.getPermissions().add(permission);
        return roleRepository.save(role);
    }

    // Remove permission from role
    public Role removePermissionFromRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found"));

        role.getPermissions().remove(permission);
        return roleRepository.save(role);
    }

    // Assign role to user
    public User assignRoleToUser(Long userId, UserRole roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Role role = getRoleByName(roleName);

        user.getRoles().clear(); // Remove all existing roles
        user.getRoles().add(role);

        return userRepository.save(user);
    }
}
