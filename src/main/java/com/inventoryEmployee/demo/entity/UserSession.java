package com.inventoryEmployee.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(unique = true, nullable = false, length = 500)
    private String sessionToken;

    @Column(nullable = false)
    private LocalDateTime loginTime;

    private LocalDateTime logoutTime;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(length = 100)
    private String deviceInfo;

    private LocalDateTime expiresAt;
}
