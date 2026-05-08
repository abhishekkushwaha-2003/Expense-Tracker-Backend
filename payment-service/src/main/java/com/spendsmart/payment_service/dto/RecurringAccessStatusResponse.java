package com.spendsmart.payment_service.dto;

import java.time.LocalDateTime;

public record RecurringAccessStatusResponse(
        Long userId,
        boolean active,
        String featureCode,
        LocalDateTime validUntil,
        String message
) {
}
