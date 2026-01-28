package com.inventoryEmployee.demo.controller;

import com.inventoryEmployee.demo.entity.Permission;
import com.inventoryEmployee.demo.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PermissionController {

    private final PermissionService permissionService;

    // Get all permissions
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Permission>> getAllPermissions() {
        List<Permission> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }

    // Get permissions by module
    @GetMapping("/module/{module}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Permission>> getPermissionsByModule(@PathVariable String module) {
        List<Permission> permissions = permissionService.getPermissionsByModule(module);
        return ResponseEntity.ok(permissions);
    }

    // Create new permission
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Permission> createPermission(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String description = request.get("description");
        String module = request.get("module");

        Permission permission = permissionService.createPermission(name, description, module);
        return new ResponseEntity<>(permission, HttpStatus.CREATED);
    }

    // Delete permission
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }
}
