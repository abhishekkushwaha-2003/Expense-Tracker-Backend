package com.spendsmart.payment_service;

import com.spendsmart.payment_service.config.RazorpayProperties;
import com.spendsmart.payment_service.controller.PaymentController;
import com.spendsmart.payment_service.dto.*;
import com.spendsmart.payment_service.entity.Payment;
import com.spendsmart.payment_service.entity.PaymentMethod;
import com.spendsmart.payment_service.entity.PaymentStatus;
import com.spendsmart.payment_service.service.PaymentService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentLayerCoverageTest {

    @Test
    void controllerDelegatesToService() {
        PaymentService service = mock(PaymentService.class);
        PaymentController controller = new PaymentController(service);
        Payment payment = Payment.builder().paymentId(1L).userId(10L).title("Pay").amount(1.0).currency("INR").paymentMethod(PaymentMethod.CASH).build();
        RecurringAccessOrderRequest orderRequest = new RecurringAccessOrderRequest(10L, "user@test.com");
        RecurringAccessOrderResponse orderResponse = new RecurringAccessOrderResponse(1L, "key", "order", 100L, "INR", "Plan", 30);
        PaymentVerificationRequest verifyRequest = new PaymentVerificationRequest(10L, "user@test.com", "order", "pay", "sig");
        RecurringAccessStatusResponse statusResponse = new RecurringAccessStatusResponse(10L, true, "RECURRING_ACCESS", LocalDateTime.now(), "ok");
        PaymentSummary summary = PaymentSummary.builder().userId(10L).totalTransactions(1).transactionsByMethod(Map.of()).transactionsByStatus(Map.of()).build();
        when(service.createPayment(payment)).thenReturn(payment);
        when(service.createRecurringAccessOrder(orderRequest)).thenReturn(orderResponse);
        when(service.verifyRecurringAccessPayment(verifyRequest)).thenReturn(payment);
        when(service.getRecurringAccessStatus(10L)).thenReturn(statusResponse);
        when(service.getPaymentById(1L)).thenReturn(payment);
        when(service.getPaymentsByUser(10L)).thenReturn(List.of(payment));
        when(service.getPaymentsByExpense(20L)).thenReturn(List.of(payment));
        when(service.getPaymentsByMethod(10L, PaymentMethod.CASH)).thenReturn(List.of(payment));
        when(service.getPaymentsByStatus(10L, PaymentStatus.COMPLETED)).thenReturn(List.of(payment));
        when(service.getSummary(10L)).thenReturn(summary);
        when(service.updatePayment(1L, payment)).thenReturn(payment);
        when(service.updateStatus(1L, PaymentStatus.FAILED)).thenReturn(payment);

        assertSame(payment, controller.createPayment(payment));
        assertSame(orderResponse, controller.createRecurringAccessOrder(orderRequest));
        assertSame(payment, controller.verifyRecurringAccessPayment(verifyRequest));
        assertSame(statusResponse, controller.getRecurringAccessStatus(10L));
        assertSame(payment, controller.getPayment(1L));
        assertEquals(List.of(payment), controller.getByUser(10L));
        assertEquals(List.of(payment), controller.getByExpense(20L));
        assertEquals(List.of(payment), controller.getByMethod(10L, PaymentMethod.CASH));
        assertEquals(List.of(payment), controller.getByStatus(10L, PaymentStatus.COMPLETED));
        assertSame(summary, controller.getSummary(10L));
        assertSame(payment, controller.updatePayment(1L, payment));
        assertSame(payment, controller.updateStatus(1L, PaymentStatus.FAILED));
        assertEquals("Deleted successfully", controller.deletePayment(1L));
        verify(service).deletePayment(1L);
    }

    @Test
    void entityDtoAndPropertiesWork() {
        Payment payment = Payment.builder().currency(" inr ").build();
        payment.onCreate();
        assertNotNull(payment.getCreatedAt());
        assertNotNull(payment.getPaidAt());
        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        assertEquals("MANUAL", payment.getProviderName());
        assertEquals("INR", payment.getCurrency());
        payment.setCurrency(" usd ");
        payment.onUpdate();
        assertEquals("USD", payment.getCurrency());
        assertNotNull(payment.getUpdatedAt());

        RazorpayProperties defaults = new RazorpayProperties(" ", null, " ", 0.0, " usd ", 0);
        assertFalse(defaults.hasCredentials());
        assertEquals("Recurring Access Plan", defaults.recurringPlanName());
        assertEquals(199.0, defaults.recurringAmount());
        assertEquals("USD", defaults.recurringCurrency());
        assertEquals(30, defaults.recurringValidityDays());
        assertTrue(new RazorpayProperties("key", "secret", "Plan", 1.0, "INR", 5).hasCredentials());

        PaymentSummary summary = new PaymentSummary();
        summary.setUserId(1L);
        summary.setTotalTransactions(2);
        summary.setTotalAmount(3.0);
        summary.setCompletedAmount(1.0);
        summary.setPendingAmount(1.0);
        summary.setFailedAmount(1.0);
        assertEquals(2, summary.getTotalTransactions());
    }
}