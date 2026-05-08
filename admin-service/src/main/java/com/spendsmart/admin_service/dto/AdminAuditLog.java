package com.spendsmart.admin_service.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminAuditLog {
    private LocalDateTime timestamp;
    private String actorEmail;
    private String action;
    private String targetType;
    private String targetId;
    private String details;
}
