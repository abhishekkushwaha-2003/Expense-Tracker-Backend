package com.spendsmart.admin_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminBroadcastRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String message;

    private boolean emailEnabled = true;
}
