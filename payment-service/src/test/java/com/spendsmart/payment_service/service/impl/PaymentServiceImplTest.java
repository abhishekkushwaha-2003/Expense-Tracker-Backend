package com.spendsmart.payment_service.service.impl;

import com.razorpay.Order;
import com.razorpay.OrderClient;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.spendsmart.payment_service.config.RazorpayProperties;
import com.spendsmart.payment_service.dto.PaymentSummary;
import com.spendsmart.payment_service.dto.PaymentVerificationRequest;
import com.spendsmart.payment_service.dto.RecurringAccessOrderRequest;
import com.spendsmart.payment_service.dto.RecurringAccessStatusResponse;
import com.spendsmart.payment_service.entity.Payment;
import com.spendsmart.payment_service.entity.PaymentMethod;
import com.spendsmart.payment_service.entity.PaymentStatus;
import com.spendsmart.payment_service.repository.PaymentRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.json.JSONObject;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    private PaymentServiceImpl paymentService;
    private Payment payment;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(
                paymentRepository,
                new RazorpayProperties("", "", null, null, null, null)
        );
        payment = Payment.builder()
                .paymentId(1L)
                .userId(10L)
                .expenseId(20L)
                .title("Dinner")
                .amount(750.0)
                .currency("INR")
                .paymentMethod(PaymentMethod.CARD)
                .status(PaymentStatus.COMPLETED)
                .build();
    }

    @Test
    void createPaymentValidatesAndSaves() {
        when(paymentRepository.save(payment)).thenReturn(payment);

        assertThat(paymentService.createPayment(payment)).isSameAs(payment);
        verify(paymentRepository).save(payment);
    }

    @Test
    void createPaymentRejectsInvalidFields() {
        payment.setUserId(0L);
        assertThatThrownBy(() -> paymentService.createPayment(payment))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User ID must be greater than zero");

        payment.setUserId(10L);
        payment.setTitle(" ");
        assertThatThrownBy(() -> paymentService.createPayment(payment))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Title is required");

        payment.setTitle("Dinner");
        payment.setAmount(0.0);
        assertThatThrownBy(() -> paymentService.createPayment(payment))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Amount must be greater than zero");

        payment.setAmount(10.0);
        payment.setCurrency("");
        assertThatThrownBy(() -> paymentService.createPayment(payment))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Currency is required");

        payment.setCurrency("INR");
        payment.setPaymentMethod(null);
        assertThatThrownBy(() -> paymentService.createPayment(payment))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Payment method is required");
    }

    @Test
    void getPaymentByIdReturnsPaymentOrThrows() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(paymentService.getPaymentById(1L)).isSameAs(payment);
        assertThatThrownBy(() -> paymentService.getPaymentById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Payment not found");
    }

    @Test
    void finderMethodsValidateInputsAndDelegate() {
        when(paymentRepository.findByUserId(10L)).thenReturn(List.of(payment));
        when(paymentRepository.findByExpenseId(20L)).thenReturn(List.of(payment));
        when(paymentRepository.findByUserIdAndPaymentMethod(10L, PaymentMethod.CARD)).thenReturn(List.of(payment));
        when(paymentRepository.findByUserIdAndStatus(10L, PaymentStatus.COMPLETED)).thenReturn(List.of(payment));

        assertThat(paymentService.getPaymentsByUser(10L)).containsExactly(payment);
        assertThat(paymentService.getPaymentsByExpense(20L)).containsExactly(payment);
        assertThat(paymentService.getPaymentsByMethod(10L, PaymentMethod.CARD)).containsExactly(payment);
        assertThat(paymentService.getPaymentsByStatus(10L, PaymentStatus.COMPLETED)).containsExactly(payment);
        assertThatThrownBy(() -> paymentService.getPaymentsByUser(null)).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> paymentService.getPaymentsByExpense(0L)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void getSummaryAggregatesAmountsAndCounts() {
        Payment pending = Payment.builder()
                .userId(10L)
                .title("Taxi")
                .amount(100.0)
                .currency("INR")
                .paymentMethod(PaymentMethod.UPI)
                .status(PaymentStatus.PENDING)
                .build();
        Payment failed = Payment.builder()
                .userId(10L)
                .title("Movie")
                .amount(null)
                .currency("INR")
                .paymentMethod(PaymentMethod.CASH)
                .status(PaymentStatus.FAILED)
                .build();
        when(paymentRepository.findByUserId(10L)).thenReturn(List.of(payment, pending, failed));

        PaymentSummary summary = paymentService.getSummary(10L);

        assertThat(summary.getTotalTransactions()).isEqualTo(3);
        assertThat(summary.getTotalAmount()).isEqualTo(850.0);
        assertThat(summary.getCompletedAmount()).isEqualTo(750.0);
        assertThat(summary.getPendingAmount()).isEqualTo(100.0);
        assertThat(summary.getFailedAmount()).isZero();
        assertThat(summary.getTransactionsByMethod().get(PaymentMethod.CARD)).isEqualTo(1);
        assertThat(summary.getTransactionsByStatus().get(PaymentStatus.FAILED)).isEqualTo(1);
    }

    @Test
    void updatePaymentCopiesFieldsAndPreservesExistingStatusAndDatesWhenNull() {
        LocalDateTime paidAt = LocalDateTime.now().minusDays(1);
        LocalDateTime validUntil = LocalDateTime.now().plusDays(10);
        payment.setPaidAt(paidAt);
        payment.setAccessValidUntil(validUntil);
        Payment update = Payment.builder()
                .userId(11L)
                .expenseId(22L)
                .title("Updated")
                .amount(900.0)
                .currency("USD")
                .paymentMethod(PaymentMethod.UPI)
                .transactionReference("ref")
                .featureCode("FEATURE")
                .providerName("MANUAL")
                .providerOrderId("order")
                .providerPaymentId("pay")
                .providerSignature("sig")
                .payerEmail("user@example.com")
                .notes("note")
                .build();
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);

        Payment result = paymentService.updatePayment(1L, update);

        assertThat(result.getUserId()).isEqualTo(11L);
        assertThat(result.getExpenseId()).isEqualTo(22L);
        assertThat(result.getTitle()).isEqualTo("Updated");
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(result.getPaidAt()).isEqualTo(paidAt);
        assertThat(result.getAccessValidUntil()).isEqualTo(validUntil);
    }

    @Test
    void updateStatusAndDeleteUseExistingPayment() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);

        assertThat(paymentService.updateStatus(1L, PaymentStatus.FAILED).getStatus()).isEqualTo(PaymentStatus.FAILED);
        paymentService.deletePayment(1L);

        verify(paymentRepository).delete(payment);
    }

    @Test
    void recurringOrderRequiresConfiguredCredentials() {
        RecurringAccessOrderRequest request = new RecurringAccessOrderRequest(10L, "user@example.com");

        assertThatThrownBy(() -> paymentService.createRecurringAccessOrder(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Razorpay credentials are missing");
    }

    @Test
    void createRecurringAccessOrderCreatesRazorpayOrderAndPendingPayment() throws Exception {
        paymentService = new PaymentServiceImpl(
                paymentRepository,
                new RazorpayProperties("key_id", "key_secret", "Recurring Plan", 199.0, "INR", 45)
        );
        OrderClient orderClient = mock(OrderClient.class);
        Order order = new Order(new JSONObject()
                .put("id", "order_123")
                .put("receipt", "receipt_123"));
        when(orderClient.create(any(JSONObject.class))).thenReturn(order);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment saved = invocation.getArgument(0);
            saved.setPaymentId(77L);
            return saved;
        });

        try (MockedConstruction<RazorpayClient> ignored = mockConstruction(
                RazorpayClient.class,
                (mock, context) -> mock.orders = orderClient
        )) {
            var response = paymentService.createRecurringAccessOrder(
                    new RecurringAccessOrderRequest(10L, "user@example.com")
            );

            assertThat(response.paymentId()).isEqualTo(77L);
            assertThat(response.orderId()).isEqualTo("order_123");
            assertThat(response.amountInSubunits()).isEqualTo(19900L);
            verify(paymentRepository).save(any(Payment.class));
        }
    }

    @Test
    void createRecurringAccessOrderWrapsRazorpayFailure() throws Exception {
        paymentService = new PaymentServiceImpl(
                paymentRepository,
                new RazorpayProperties("key_id", "key_secret", "Recurring Plan", 199.0, "INR", 45)
        );
        OrderClient orderClient = mock(OrderClient.class);
        when(orderClient.create(any(JSONObject.class))).thenThrow(new RazorpayException("down"));

        try (MockedConstruction<RazorpayClient> ignored = mockConstruction(
                RazorpayClient.class,
                (mock, context) -> mock.orders = orderClient
        )) {
            assertThatThrownBy(() -> paymentService.createRecurringAccessOrder(
                    new RecurringAccessOrderRequest(10L, "user@example.com")
            )).isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Unable to create Razorpay order");
        }
    }

    @Test
    void verifyRecurringAccessPaymentRejectsMissingOrWrongUserOrder() {
        PaymentVerificationRequest request = new PaymentVerificationRequest(
                10L,
                "user@example.com",
                "order_1",
                "pay_1",
                "signature"
        );
        when(paymentRepository.findByProviderOrderId("order_1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.verifyRecurringAccessPayment(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Payment order not found");

        payment.setUserId(99L);
        when(paymentRepository.findByProviderOrderId("order_1")).thenReturn(Optional.of(payment));
        assertThatThrownBy(() -> paymentService.verifyRecurringAccessPayment(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Payment does not belong to this user");
    }

    @Test
    void verifyRecurringAccessPaymentMarksPaymentCompletedWhenSignatureIsValid() {
        paymentService = new PaymentServiceImpl(
                paymentRepository,
                new RazorpayProperties("key_id", "key_secret", null, null, null, 45)
        );
        Payment pending = Payment.builder()
                .paymentId(1L)
                .userId(10L)
                .providerOrderId("order_1")
                .status(PaymentStatus.PENDING)
                .build();
        PaymentVerificationRequest request = new PaymentVerificationRequest(
                10L,
                "user@example.com",
                "order_1",
                "pay_1",
                "signature"
        );
        when(paymentRepository.findByProviderOrderId("order_1")).thenReturn(Optional.of(pending));
        when(paymentRepository.save(pending)).thenReturn(pending);

        try (MockedStatic<Utils> utilities = mockStatic(Utils.class)) {
            Payment result = paymentService.verifyRecurringAccessPayment(request);

            utilities.verify(() -> Utils.verifyPaymentSignature(any(JSONObject.class), eq("key_secret")));
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(result.getProviderPaymentId()).isEqualTo("pay_1");
            assertThat(result.getAccessValidUntil()).isAfter(LocalDateTime.now().plusDays(44));
        }
    }

    @Test
    void verifyRecurringAccessPaymentMarksPaymentFailedWhenSignatureIsInvalid() throws Exception {
        paymentService = new PaymentServiceImpl(
                paymentRepository,
                new RazorpayProperties("key_id", "key_secret", null, null, null, 45)
        );
        Payment pending = Payment.builder()
                .paymentId(1L)
                .userId(10L)
                .providerOrderId("order_1")
                .status(PaymentStatus.PENDING)
                .build();
        PaymentVerificationRequest request = new PaymentVerificationRequest(
                10L,
                "user@example.com",
                "order_1",
                "pay_1",
                "signature"
        );
        when(paymentRepository.findByProviderOrderId("order_1")).thenReturn(Optional.of(pending));
        when(paymentRepository.save(pending)).thenReturn(pending);

        try (MockedStatic<Utils> utilities = mockStatic(Utils.class)) {
            utilities.when(() -> Utils.verifyPaymentSignature(any(JSONObject.class), eq("key_secret")))
                    .thenThrow(new RazorpayException("bad signature"));

            assertThatThrownBy(() -> paymentService.verifyRecurringAccessPayment(request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Payment signature verification failed");
            assertThat(pending.getStatus()).isEqualTo(PaymentStatus.FAILED);
        }
    }

    @Test
    void getRecurringAccessStatusReportsActiveAndInactiveStates() {
        Payment active = Payment.builder()
                .userId(10L)
                .featureCode("RECURRING_ACCESS")
                .status(PaymentStatus.COMPLETED)
                .accessValidUntil(LocalDateTime.now().plusDays(1))
                .build();
        when(paymentRepository.findTopByUserIdAndFeatureCodeAndStatusOrderByAccessValidUntilDesc(
                10L,
                "RECURRING_ACCESS",
                PaymentStatus.COMPLETED
        )).thenReturn(Optional.of(active));

        RecurringAccessStatusResponse activeStatus = paymentService.getRecurringAccessStatus(10L);
        assertThat(activeStatus.active()).isTrue();

        active.setAccessValidUntil(LocalDateTime.now().minusDays(1));
        RecurringAccessStatusResponse inactiveStatus = paymentService.getRecurringAccessStatus(10L);
        assertThat(inactiveStatus.active()).isFalse();
    }
}
