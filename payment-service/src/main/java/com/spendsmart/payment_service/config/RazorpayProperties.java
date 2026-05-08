package com.spendsmart.payment_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "razorpay")
public record RazorpayProperties(
        String keyId,
        String keySecret,
        String recurringPlanName,
        Double recurringAmount,
        String recurringCurrency,
        Integer recurringValidityDays
) {
    public RazorpayProperties {
        recurringPlanName = hasText(recurringPlanName) ? recurringPlanName : "Recurring Access Plan";
        recurringAmount = recurringAmount == null || recurringAmount <= 0 ? 199.0 : recurringAmount;
        recurringCurrency = hasText(recurringCurrency) ? recurringCurrency.trim().toUpperCase() : "INR";
        recurringValidityDays = recurringValidityDays == null || recurringValidityDays <= 0 ? 30 : recurringValidityDays;
    }

    public boolean hasCredentials() {
        return hasText(keyId) && hasText(keySecret);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
