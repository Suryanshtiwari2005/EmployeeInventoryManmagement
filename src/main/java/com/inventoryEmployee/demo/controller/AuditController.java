package com.inventoryEmployee.demo.controller;

import com.inventoryEmployee.demo.dto.response.AuditLogResponse;
import com.inventoryEmployee.demo.entity.AuditLog;
import com.inventoryEmployee.demo.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuditController {

    private final AuditService auditService;

    // Get audit logs for specific entity
    @GetMapping("/entity/{entityName}/{entityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogsByEntity(
            @PathVariable String entityName,
            @PathVariable Long entityId,
            Pageable pageable) {
        Page<AuditLog> logs = auditService.getAuditLogsByEntity(entityName, entityId, pageable);
        return ResponseEntity.ok(logs.map(this::mapToResponse));
    }

    // Get audit logs by user
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogsByUser(
            @PathVariable Long userId,
            Pageable pageable) {
        Page<AuditLog> logs = auditService.getAuditLogsByUser(userId, pageable);
        return ResponseEntity.ok(logs.map(this::mapToResponse));
    }

    // Get recent audit logs
    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<AuditLogResponse>> getRecentAuditLogs(Pageable pageable) {
        Page<AuditLog> logs = auditService.getRecentAuditLogs(pageable);
        return ResponseEntity.ok(logs.map(this::mapToResponse));
    }

    // Search with filters
    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<AuditLogResponse>> filterAuditLogs(
            @RequestParam(required = false) String entityName,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        Page<AuditLog> logs = auditService.searchAuditLogsWithFilters(
                entityName, action, userId, startDate, endDate, pageable);
        return ResponseEntity.ok(logs.map(this::mapToResponse));
    }

    private AuditLogResponse mapToResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .entityName(log.getEntityName())
                .entityId(log.getEntityId())
                .action(log.getAction())

                // Handle User safely
                .userId(log.getUser() != null ? log.getUser().getId() : null)
                // Prefer the snapshot 'username' field if available, fallback to relation
                .username(log.getUsername() != null ? log.getUsername() : (log.getUser() != null ? log.getUser().getUsername() : "System"))

                .ipAddress(log.getIpAddress())
                .endpoint(log.getEndpoint()) // Your entity has this field, so we can map it directly

                .timestamp(log.getTimestamp())

                // FIXED: Combine old/new values into the single 'changes' DTO field
                .changes(formatChanges(log.getOldValue(), log.getNewValue()))

                .build();
    }
    private String formatChanges(String oldVal, String newVal) {
        if (oldVal == null && newVal == null) return null;
        if (oldVal == null) return "Created: " + newVal;
        if (newVal == null) return "Deleted: " + oldVal;

        // For updates, show the transition
        return "Old: " + oldVal + " -> New: " + newVal;
    }
}
