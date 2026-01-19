package com.inventoryEmployee.demo.dto.response;

import com.inventoryEmployee.demo.enums.NotificationPriority;
import com.inventoryEmployee.demo.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;

    private NotificationType type;
    private String title;
    private String message;
    private String link;

    private NotificationPriority priority;

    private Boolean isRead;

    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}