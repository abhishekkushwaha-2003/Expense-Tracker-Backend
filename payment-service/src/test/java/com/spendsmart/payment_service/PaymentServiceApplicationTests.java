package com.spendsmart.payment_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.spendsmart.payment_service.config.RazorpayProperties;
import com.spendsmart.payment_service.dto.PaymentSummary;
import com.spendsmart.payment_service.entity.Payment;
import com.spendsmart.payment_service.entity.PaymentMethod;
import com.spendsmart.payment_service.entity.PaymentStatus;
import com.spendsmart.payment_service.repository.PaymentRepository;
import com.spendsmart.payment_service.service.impl.PaymentServiceImpl;

@ExtendWith(MockitoExtension.class)
class PaymentServiceApplicationTests {

    @Mock
    private PaymentRepository paymentRepository;

    private PaymentServiceImpl service;

    @BeforeEach
    void setUp() {
        RazorpayProperties razorpayProperties = new RazorpayProperties("key", "secret", "Recurring", 199.0, "INR", 30);
        service = new PaymentServiceImpl(paymentRepository, razorpayProperties);
    }

    @Test
    void getSummaryAggregatesAmountsAndCounts() {
        Payment completed = Payment.builder().userId(4L).amount(100.0).paymentMethod(PaymentMethod.UPI).status(PaymentStatus.COMPLETED).build();
        Payment pending = Payment.builder().userId(4L).amount(50.0).paymentMethod(PaymentMethod.CARD).status(PaymentStatus.PENDING).build();
        Payment failed = Payment.builder().userId(4L).amount(25.0).paymentMethod(PaymentMethod.UPI).status(PaymentStatus.FAILED).build();
        when(paymentRepository.findByUserId(4L)).thenReturn(List.of(completed, pending, failed));

        PaymentSummary summary = service.getSummary(4L);

        assertEquals(3, summary.getTotalTransactions());
        assertEquals(175.0, summary.getTotalAmount());
        assertEquals(100.0, summary.getCompletedAmount());
        assertEquals(50.0, summary.getPendingAmount());
        assertEquals(25.0, summary.getFailedAmount());
        assertEquals(2L, summary.getTransactionsByMethod().get(PaymentMethod.UPI));
    }

    @Test
    void getRecurringAccessStatusReportsActiveWhenAccessIsValid() {
        Payment payment = Payment.builder()
                .userId(7L)
                .featureCode("RECURRING_ACCESS")
                .status(PaymentStatus.COMPLETED)
                .accessValidUntil(LocalDateTime.now().plusDays(5))
                .build();
        when(paymentRepository.findTopByUserIdAndFeatureCodeAndStatusOrderByAccessValidUntilDesc(7L, "RECURRING_ACCESS", PaymentStatus.COMPLETED))
                .thenReturn(Optional.of(payment));

        var status = service.getRecurringAccessStatus(7L);

        assertTrue(status.active());
    }

    @Test
    void getRecurringAccessStatusReportsInactiveWhenMissingPayment() {
        when(paymentRepository.findTopByUserIdAndFeatureCodeAndStatusOrderByAccessValidUntilDesc(8L, "RECURRING_ACCESS", PaymentStatus.COMPLETED))
                .thenReturn(Optional.empty());

        var status = service.getRecurringAccessStatus(8L);

        assertFalse(status.active());
    }

    @Test
    void createPaymentRejectsInvalidAmount() {
        Payment payment = Payment.builder().userId(1L).title("Plan").amount(0.0).currency("INR").paymentMethod(PaymentMethod.UPI).build();

        assertThrows(ResponseStatusException.class, () -> service.createPayment(payment));
    }

    @Test
    void updateStatusPersistsNewStatus() {
        Payment payment = Payment.builder().paymentId(3L).userId(1L).status(PaymentStatus.PENDING).build();
        when(paymentRepository.findById(3L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);

        Payment updated = service.updateStatus(3L, PaymentStatus.COMPLETED);

        assertEquals(PaymentStatus.COMPLETED, updated.getStatus());
        verify(paymentRepository).save(payment);
    }
}
