package com.inventoryEmployee.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventoryEmployee.demo.entity.AuditLog;
import com.inventoryEmployee.demo.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest request;
    private final ObjectMapper objectMapper;

    // Log action (async to not slow down main operations)
    @Async
    public void logAction(String entityName, Long entityId, String action,
                          Object oldValue, Object newValue) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth != null ? auth.getName() : "SYSTEM";

            String ipAddress = IPUtil.getClientIP(request);
            String userAgent = request.getHeader("User-Agent");
            String endpoint = request.getRequestURI();

            AuditLog auditLog = AuditLog.builder()
                    .entityName(entityName)
                    .entityId(entityId)
                    .action(action)
                    .oldValue(oldValue != null ? objectMapper.writeValueAsString(oldValue) : null)
                    .newValue(newValue != null ? objectMapper.writeValueAsString(newValue) : null)
                    .username(username)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .endpoint(endpoint)
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} {} by {}", action, entityName, username);

        } catch (Exception e) {
            log.error("Error creating audit log", e);
        }
    }

    // Get audit logs for specific entity
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByEntity(String entityName, Long entityId, Pageable pageable) {
        return auditLogRepository.findByEntityNameAndEntityId(entityName, entityId, pageable);
    }

    // Get audit logs by user
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByUser(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable);
    }

    // Get recent audit logs
    @Transactional(readOnly = true)
    public Page<AuditLog> getRecentAuditLogs(Pageable pageable) {
        return auditLogRepository.findRecentAuditLogs(pageable);
    }

    // Search with filters
    @Transactional(readOnly = true)
    public Page<AuditLog> searchAuditLogsWithFilters(String entityName, String action,
                                                     Long userId, LocalDateTime startDate,
                                                     LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findByFilters(entityName, action, userId,
                startDate, endDate, pageable);
    }
}
