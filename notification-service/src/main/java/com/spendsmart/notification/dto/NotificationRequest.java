package com.spendsmart.notification.dto;

import com.spendsmart.notification.entity.NotificationSeverity;
import com.spendsmart.notification.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationRequest {

    @NotNull
    private Long recipientId;

    private String recipientEmail;

    @NotNull
    private NotificationType type;

    @NotNull
    private NotificationSeverity severity;

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    private Long relatedId;

    private String relatedType;

    private boolean emailEnabled;
}
