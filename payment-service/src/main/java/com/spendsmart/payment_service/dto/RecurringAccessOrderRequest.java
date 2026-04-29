package com.spendsmart.payment_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RecurringAccessOrderRequest(
        @NotNull Long userId,
        @Email @NotBlank String email
) {
}
