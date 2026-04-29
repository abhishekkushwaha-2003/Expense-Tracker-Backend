package com.spendsmart.payment_service.service.impl;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.spendsmart.payment_service.dto.PaymentSummary;
import com.spendsmart.payment_service.entity.Payment;
import com.spendsmart.payment_service.entity.PaymentMethod;
import com.spendsmart.payment_service.entity.PaymentStatus;
import com.spendsmart.payment_service.repository.PaymentRepository;
import com.spendsmart.payment_service.service.PaymentService;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Payment createPayment(Payment payment) {
        validatePayment(payment);
        return paymentRepository.save(payment);
    }

    @Override
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
    }

    @Override
    public List<Payment> getPaymentsByUser(Long userId) {
        validateUserId(userId);
        return paymentRepository.findByUserId(userId);
    }

    @Override
    public List<Payment> getPaymentsByExpense(Long expenseId) {
        if (expenseId == null || expenseId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expense ID must be greater than zero");
        }
        return paymentRepository.findByExpenseId(expenseId);
    }

    @Override
    public List<Payment> getPaymentsByMethod(Long userId, PaymentMethod paymentMethod) {
        validateUserId(userId);
        return paymentRepository.findByUserIdAndPaymentMethod(userId, paymentMethod);
    }

    @Override
    public List<Payment> getPaymentsByStatus(Long userId, PaymentStatus status) {
        validateUserId(userId);
        return paymentRepository.findByUserIdAndStatus(userId, status);
    }

    @Override
    public PaymentSummary getSummary(Long userId) {
        validateUserId(userId);

        List<Payment> payments = paymentRepository.findByUserId(userId);
        Map<PaymentMethod, Long> transactionsByMethod = new EnumMap<>(PaymentMethod.class);
        Map<PaymentStatus, Long> transactionsByStatus = new EnumMap<>(PaymentStatus.class);

        Arrays.stream(PaymentMethod.values()).forEach(method -> transactionsByMethod.put(method, 0L));
        Arrays.stream(PaymentStatus.values()).forEach(status -> transactionsByStatus.put(status, 0L));

        double totalAmount = 0;
        double completedAmount = 0;
        double pendingAmount = 0;
        double failedAmount = 0;

        for (Payment payment : payments) {
            double amount = payment.getAmount() == null ? 0 : payment.getAmount();
            PaymentMethod method = payment.getPaymentMethod();
            PaymentStatus status = payment.getStatus();

            totalAmount += amount;
            transactionsByMethod.computeIfPresent(method, (key, count) -> count + 1);
            transactionsByStatus.computeIfPresent(status, (key, count) -> count + 1);

            if (status == PaymentStatus.COMPLETED) {
                completedAmount += amount;
            } else if (status == PaymentStatus.PENDING) {
                pendingAmount += amount;
            } else if (status == PaymentStatus.FAILED) {
                failedAmount += amount;
            }
        }

        return PaymentSummary.builder()
                .userId(userId)
                .totalTransactions(payments.size())
                .totalAmount(totalAmount)
                .completedAmount(completedAmount)
                .pendingAmount(pendingAmount)
                .failedAmount(failedAmount)
                .transactionsByMethod(transactionsByMethod)
                .transactionsByStatus(transactionsByStatus)
                .build();
    }

    @Override
    public Payment updatePayment(Long id, Payment payment) {
        validatePayment(payment);
        Payment existing = getPaymentById(id);

        existing.setUserId(payment.getUserId());
        existing.setExpenseId(payment.getExpenseId());
        existing.setTitle(payment.getTitle());
        existing.setAmount(payment.getAmount());
        existing.setCurrency(payment.getCurrency());
        existing.setPaymentMethod(payment.getPaymentMethod());
        existing.setStatus(payment.getStatus() == null ? existing.getStatus() : payment.getStatus());
        existing.setTransactionReference(payment.getTransactionReference());
        existing.setNotes(payment.getNotes());
        existing.setPaidAt(payment.getPaidAt() == null ? existing.getPaidAt() : payment.getPaidAt());

        return paymentRepository.save(existing);
    }

    @Override
    public Payment updateStatus(Long id, PaymentStatus status) {
        Payment existing = getPaymentById(id);
        existing.setStatus(status);
        return paymentRepository.save(existing);
    }

    @Override
    public void deletePayment(Long id) {
        Payment payment = getPaymentById(id);
        paymentRepository.delete(payment);
    }

    private void validatePayment(Payment payment) {
        validateUserId(payment.getUserId());

        if (payment.getTitle() == null || payment.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");
        }

        if (payment.getAmount() == null || payment.getAmount() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
        }

        if (payment.getCurrency() == null || payment.getCurrency().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Currency is required");
        }

        if (payment.getPaymentMethod() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment method is required");
        }
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID must be greater than zero");
        }
    }
}
