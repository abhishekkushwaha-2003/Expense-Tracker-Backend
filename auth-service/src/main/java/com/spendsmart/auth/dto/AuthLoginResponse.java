package com.spendsmart.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthLoginResponse {
    private String token;
    private Long userId;
    private String email;
    private String fullName;
    private String currency;
    private String timezone;
    private Double monthlyBudget;
    private String status;
}
