package com.spendsmart.admin_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminLoginRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
