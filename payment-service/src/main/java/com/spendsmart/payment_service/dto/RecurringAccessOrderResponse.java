package com.spendsmart.payment_service.dto;

public record RecurringAccessOrderResponse(
        Long paymentId,
        String keyId,
        String orderId,
        Long amountInSubunits,
        String currency,
        String planName,
        Integer validityDays
) {
}
