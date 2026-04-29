package com.spendsmart.payment_service.service;

import java.util.List;

import com.spendsmart.payment_service.dto.PaymentSummary;
import com.spendsmart.payment_service.entity.Payment;
import com.spendsmart.payment_service.entity.PaymentMethod;
import com.spendsmart.payment_service.entity.PaymentStatus;

public interface PaymentService {

    Payment createPayment(Payment payment);

    Payment getPaymentById(Long id);

    List<Payment> getPaymentsByUser(Long userId);

    List<Payment> getPaymentsByExpense(Long expenseId);

    List<Payment> getPaymentsByMethod(Long userId, PaymentMethod paymentMethod);

    List<Payment> getPaymentsByStatus(Long userId, PaymentStatus status);

    PaymentSummary getSummary(Long userId);

    Payment updatePayment(Long id, Payment payment);

    Payment updateStatus(Long id, PaymentStatus status);

    void deletePayment(Long id);
}
