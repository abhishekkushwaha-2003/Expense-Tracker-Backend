package com.spendsmart.payment_service.service.impl;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.spendsmart.payment_service.config.RazorpayProperties;
import com.spendsmart.payment_service.dto.PaymentSummary;
import com.spendsmart.payment_service.dto.PaymentVerificationRequest;
import com.spendsmart.payment_service.dto.RecurringAccessOrderRequest;
import com.spendsmart.payment_service.dto.RecurringAccessOrderResponse;
import com.spendsmart.payment_service.dto.RecurringAccessStatusResponse;
import com.spendsmart.payment_service.entity.Payment;
import com.spendsmart.payment_service.entity.PaymentMethod;
import com.spendsmart.payment_service.entity.PaymentStatus;
import com.spendsmart.payment_service.repository.PaymentRepository;
import com.spendsmart.payment_service.service.PaymentService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final String FEATURE_RECURRING_ACCESS = "RECURRING_ACCESS";

    private final PaymentRepository paymentRepository;
    private final RazorpayProperties razorpayProperties;

    public PaymentServiceImpl(PaymentRepository paymentRepository, RazorpayProperties razorpayProperties) {
        this.paymentRepository = paymentRepository;
        this.razorpayProperties = razorpayProperties;
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
        existing.setFeatureCode(payment.getFeatureCode());
        existing.setProviderName(payment.getProviderName());
        existing.setProviderOrderId(payment.getProviderOrderId());
        existing.setProviderPaymentId(payment.getProviderPaymentId());
        existing.setProviderSignature(payment.getProviderSignature());
        existing.setPayerEmail(payment.getPayerEmail());
        existing.setNotes(payment.getNotes());
        existing.setPaidAt(payment.getPaidAt() == null ? existing.getPaidAt() : payment.getPaidAt());
        existing.setAccessValidUntil(payment.getAccessValidUntil() == null ? existing.getAccessValidUntil() : payment.getAccessValidUntil());

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

    @Override
    public RecurringAccessOrderResponse createRecurringAccessOrder(RecurringAccessOrderRequest request) {
        validateUserId(request.userId());
        validateRazorpayConfiguration();

        try {
            RazorpayClient razorpayClient = new RazorpayClient(
                    razorpayProperties.keyId(),
                    razorpayProperties.keySecret()
            );

            JSONObject orderRequest = new JSONObject();
            long amountInSubunits = Math.round(razorpayProperties.recurringAmount() * 100);
            orderRequest.put("amount", amountInSubunits);
            orderRequest.put("currency", razorpayProperties.recurringCurrency());
            orderRequest.put("receipt", "recurring_" + request.userId() + "_" + System.currentTimeMillis());
            JSONObject notes = new JSONObject();
            notes.put("userId", request.userId());
            notes.put("featureCode", FEATURE_RECURRING_ACCESS);
            notes.put("email", request.email());
            orderRequest.put("notes", notes);

            Order order = razorpayClient.orders.create(orderRequest);

            Payment payment = Payment.builder()
                    .userId(request.userId())
                    .title(razorpayProperties.recurringPlanName())
                    .amount(razorpayProperties.recurringAmount())
                    .currency(razorpayProperties.recurringCurrency())
                    .paymentMethod(PaymentMethod.RAZORPAY)
                    .status(PaymentStatus.PENDING)
                    .transactionReference(order.get("receipt"))
                    .featureCode(FEATURE_RECURRING_ACCESS)
                    .providerName("RAZORPAY")
                    .providerOrderId(order.get("id"))
                    .payerEmail(request.email())
                    .notes("Recurring transactions access payment")
                    .build();

            Payment saved = paymentRepository.save(payment);
            return new RecurringAccessOrderResponse(
                    saved.getPaymentId(),
                    razorpayProperties.keyId(),
                    saved.getProviderOrderId(),
                    amountInSubunits,
                    razorpayProperties.recurringCurrency(),
                    razorpayProperties.recurringPlanName(),
                    razorpayProperties.recurringValidityDays()
            );
        } catch (RazorpayException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to create Razorpay order", ex);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to create Razorpay order", ex);
        }
    }

    @Override
    public Payment verifyRecurringAccessPayment(PaymentVerificationRequest request) {
        validateUserId(request.userId());
        Payment payment = paymentRepository.findByProviderOrderId(request.razorpayOrderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment order not found"));

        if (!Objects.equals(payment.getUserId(), request.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Payment does not belong to this user");
        }

        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", request.razorpayOrderId());
            options.put("razorpay_payment_id", request.razorpayPaymentId());
            options.put("razorpay_signature", request.razorpaySignature());
            Utils.verifyPaymentSignature(options, razorpayProperties.keySecret());

            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setProviderPaymentId(request.razorpayPaymentId());
            payment.setProviderSignature(request.razorpaySignature());
            payment.setPaymentMethod(PaymentMethod.RAZORPAY);
            payment.setPayerEmail(request.email());
            payment.setPaidAt(LocalDateTime.now());
            payment.setAccessValidUntil(LocalDateTime.now().plusDays(razorpayProperties.recurringValidityDays()));
            return paymentRepository.save(payment);
        } catch (RazorpayException ex) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment signature verification failed", ex);
        }
    }

    @Override
    public RecurringAccessStatusResponse getRecurringAccessStatus(Long userId) {
        validateUserId(userId);
        LocalDateTime now = LocalDateTime.now();
        Payment payment = paymentRepository
                .findTopByUserIdAndFeatureCodeAndStatusOrderByAccessValidUntilDesc(
                        userId,
                        FEATURE_RECURRING_ACCESS,
                        PaymentStatus.COMPLETED
                )
                .orElse(null);

        boolean active = payment != null
                && payment.getAccessValidUntil() != null
                && payment.getAccessValidUntil().isAfter(now);

        return new RecurringAccessStatusResponse(
                userId,
                active,
                FEATURE_RECURRING_ACCESS,
                payment == null ? null : payment.getAccessValidUntil(),
                active
                        ? "Recurring access is active."
                        : "Recurring access requires payment."
        );
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

    private void validateRazorpayConfiguration() {
        if (!razorpayProperties.hasCredentials()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Razorpay credentials are missing. Restart payment-service after updating application.yml."
            );
        }
    }
}
