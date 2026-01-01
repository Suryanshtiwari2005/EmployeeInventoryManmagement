package com.inventoryEmployee.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_entity_name", columnList = "entityName"),
        @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String entityName; // e.g., "Product", "Employee"

    @Column(nullable = false)
    private Long entityId;

    @Column(nullable = false, length = 20)
    private String action; // CREATE, UPDATE, DELETE, LOGIN, LOGOUT

    @Column(columnDefinition = "TEXT")
    private String oldValue; // JSON of old state

    @Column(columnDefinition = "TEXT")
    private String newValue; // JSON of new state

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 50)
    private String username;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @Column(length = 100)
    private String endpoint; // API endpoint called

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String additionalInfo;
}
