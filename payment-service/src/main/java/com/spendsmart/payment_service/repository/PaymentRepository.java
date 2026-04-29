package com.spendsmart.payment_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spendsmart.payment_service.entity.Payment;
import com.spendsmart.payment_service.entity.PaymentMethod;
import com.spendsmart.payment_service.entity.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByUserId(Long userId);

    List<Payment> findByExpenseId(Long expenseId);

    List<Payment> findByUserIdAndPaymentMethod(Long userId, PaymentMethod paymentMethod);

    List<Payment> findByUserIdAndStatus(Long userId, PaymentStatus status);
}
