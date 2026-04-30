package com.spendsmart.admin_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminLoginResponse {
    private String token;
    private String email;
    private String fullName;
    private String role;
}
