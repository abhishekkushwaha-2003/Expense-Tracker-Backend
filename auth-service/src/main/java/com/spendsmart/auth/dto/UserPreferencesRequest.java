package com.spendsmart.auth.dto;

import lombok.Data;

@Data
public class UserPreferencesRequest {

    private String fullName;

    private String currency;

    private String timezone;

    private Double monthlyBudget;
}
