package com.inventoryEmployee.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private Long id;

    private String entityName;
    private Long entityId;
    private String action;

    private String username;
    private Long userId;

    private String ipAddress;
    private String endpoint;

    private LocalDateTime timestamp;

    // Optional: Include old/new values if needed
    private String changes;
}
