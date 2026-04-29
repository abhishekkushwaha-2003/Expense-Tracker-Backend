package com.spendsmart.payment_service.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spendsmart.payment_service.dto.PaymentSummary;
import com.spendsmart.payment_service.dto.PaymentVerificationRequest;
import com.spendsmart.payment_service.dto.RecurringAccessOrderRequest;
import com.spendsmart.payment_service.dto.RecurringAccessOrderResponse;
import com.spendsmart.payment_service.dto.RecurringAccessStatusResponse;
import com.spendsmart.payment_service.entity.Payment;
import com.spendsmart.payment_service.entity.PaymentMethod;
import com.spendsmart.payment_service.entity.PaymentStatus;
import com.spendsmart.payment_service.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/payments")
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public Payment createPayment(@Valid @RequestBody Payment payment) {
        return paymentService.createPayment(payment);
    }

    @PostMapping("/recurring-access/order")
    public RecurringAccessOrderResponse createRecurringAccessOrder(
            @Valid @RequestBody RecurringAccessOrderRequest request
    ) {
        return paymentService.createRecurringAccessOrder(request);
    }

    @PostMapping("/recurring-access/verify")
    public Payment verifyRecurringAccessPayment(
            @Valid @RequestBody PaymentVerificationRequest request
    ) {
        return paymentService.verifyRecurringAccessPayment(request);
    }

    @GetMapping("/recurring-access/user/{userId}/status")
    public RecurringAccessStatusResponse getRecurringAccessStatus(@PathVariable Long userId) {
        return paymentService.getRecurringAccessStatus(userId);
    }

    @GetMapping("/{id}")
    public Payment getPayment(@PathVariable Long id) {
        return paymentService.getPaymentById(id);
    }

    @GetMapping("/user/{userId}")
    public List<Payment> getByUser(@PathVariable Long userId) {
        return paymentService.getPaymentsByUser(userId);
    }

    @GetMapping("/expense/{expenseId}")
    public List<Payment> getByExpense(@PathVariable Long expenseId) {
        return paymentService.getPaymentsByExpense(expenseId);
    }

    @GetMapping("/user/{userId}/method/{paymentMethod}")
    public List<Payment> getByMethod(@PathVariable Long userId,
                                     @PathVariable PaymentMethod paymentMethod) {
        return paymentService.getPaymentsByMethod(userId, paymentMethod);
    }

    @GetMapping("/user/{userId}/status/{status}")
    public List<Payment> getByStatus(@PathVariable Long userId,
                                     @PathVariable PaymentStatus status) {
        return paymentService.getPaymentsByStatus(userId, status);
    }

    @GetMapping("/user/{userId}/summary")
    public PaymentSummary getSummary(@PathVariable Long userId) {
        return paymentService.getSummary(userId);
    }

    @PutMapping("/{id}")
    public Payment updatePayment(@PathVariable Long id,
                                 @Valid @RequestBody Payment payment) {
        return paymentService.updatePayment(id, payment);
    }

    @PatchMapping("/{id}/status/{status}")
    public Payment updateStatus(@PathVariable Long id,
                                @PathVariable PaymentStatus status) {
        return paymentService.updateStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public String deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return "Deleted successfully";
    }
}
