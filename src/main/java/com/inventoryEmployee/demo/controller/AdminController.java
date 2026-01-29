package com.inventoryEmployee.demo.controller;

import com.inventoryEmployee.demo.entity.User;
import com.inventoryEmployee.demo.repository.UserRepository;
import com.inventoryEmployee.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {

    private final AuthService authService;
    private final UserRepository userRepository;

    // Get all pending approvals
    @GetMapping("/pending-approvals")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getPendingApprovals() {
        // You might want to create a DTO for this to avoid exposing password hashes
        List<User> pendingUsers = userRepository.findByEnabledFalse();
        return ResponseEntity.ok(pendingUsers);
    }

    // Approve User
    @PutMapping("/approve/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> approveUser(@PathVariable Long userId) {
        authService.approveUser(userId);
        return ResponseEntity.ok("User approved successfully.");
    }

    // Reject User
    @DeleteMapping("/reject/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> rejectUser(@PathVariable Long userId) {
        authService.rejectUser(userId);
        return ResponseEntity.ok("User rejected and removed.");
    }
}
