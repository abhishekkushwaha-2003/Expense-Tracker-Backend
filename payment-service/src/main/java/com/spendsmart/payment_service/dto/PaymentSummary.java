package com.spendsmart.payment_service.dto;

import java.util.Map;

import com.spendsmart.payment_service.entity.PaymentMethod;
import com.spendsmart.payment_service.entity.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSummary {

    private Long userId;
    private long totalTransactions;
    private double totalAmount;
    private double completedAmount;
    private double pendingAmount;
    private double failedAmount;
    private Map<PaymentMethod, Long> transactionsByMethod;
    private Map<PaymentStatus, Long> transactionsByStatus;
}
