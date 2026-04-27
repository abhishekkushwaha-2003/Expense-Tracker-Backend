package com.spendsmart.notification.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BudgetAlertRequest {

    @NotNull
    private Long recipientId;

    private String recipientEmail;

    @NotNull
    private Long budgetId;

    private Long categoryId;

    @NotBlank
    private String budgetName;

    @NotNull
    @DecimalMin("0.0")
    private Double spentAmount;

    @NotNull
    @DecimalMin("0.0")
    private Double limitAmount;

    @NotNull
    @DecimalMin("0.0")
    private Double alertThreshold;

    private String currency = "INR";
}
