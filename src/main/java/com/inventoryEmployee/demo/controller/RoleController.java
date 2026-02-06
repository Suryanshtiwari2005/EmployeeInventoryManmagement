package com.inventoryEmployee.demo.controller;

import com.inventoryEmployee.demo.entity.Role;
import com.inventoryEmployee.demo.entity.User;
import com.inventoryEmployee.demo.enums.UserRole;
import com.inventoryEmployee.demo.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RoleController {

    private final RoleService roleService;

    // Get all roles
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    // Get role by name
    @GetMapping("/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Role> getRoleByName(@PathVariable String roleName) {
        Role role = roleService.getRoleByName(roleName);
        return ResponseEntity.ok(role);
    }

    // Create new role
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Role> createRole(@RequestBody Map<String, String> request) {
        String roleName = request.get("name").toUpperCase();
        String description = request.get("description");

        Role role = roleService.createRole(roleName, description);
        return new ResponseEntity<>(role, HttpStatus.CREATED);
    }

    // Add permission to role
    @PostMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Role> addPermissionToRole(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        Role role = roleService.addPermissionToRole(roleId, permissionId);
        return ResponseEntity.ok(role);
    }

    // Remove permission from role
    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Role> removePermissionFromRole(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        Role role = roleService.removePermissionFromRole(roleId, permissionId);
        return ResponseEntity.ok(role);
    }

    // Assign role to user
    @PutMapping("/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> assignRoleToUser(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        String roleName = request.get("roleName").toString().toUpperCase();

        User user = roleService.assignRoleToUser(userId, roleName);
        return ResponseEntity.ok(user);
    }
}