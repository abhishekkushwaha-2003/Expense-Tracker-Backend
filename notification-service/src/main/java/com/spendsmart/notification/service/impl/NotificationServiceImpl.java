package com.spendsmart.notification.service.impl;

import com.spendsmart.notification.dto.BudgetAlertRequest;
import com.spendsmart.notification.dto.BulkNotificationRequest;
import com.spendsmart.notification.dto.EmailRequest;
import com.spendsmart.notification.dto.NotificationRequest;
import com.spendsmart.notification.entity.Notification;
import com.spendsmart.notification.entity.NotificationSeverity;
import com.spendsmart.notification.entity.NotificationType;
import com.spendsmart.notification.exception.ResourceNotFoundException;
import com.spendsmart.notification.repository.NotificationRepository;
import com.spendsmart.notification.service.EmailService;
import com.spendsmart.notification.service.NotificationService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            EmailService emailService
    ) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    @Override
    public Notification send(NotificationRequest request) {
        Notification notification = buildNotification(request);
        Notification saved = notificationRepository.save(notification);
        maybeSendEmail(saved, request.isEmailEnabled());
        return saved;
    }

    @Override
    public Notification sendBudgetAlert(BudgetAlertRequest request) {
        double usagePercent = calculateUsagePercent(request.getSpentAmount(), request.getLimitAmount());
        boolean exceeded = request.getSpentAmount() >= request.getLimitAmount();

        NotificationSeverity severity = exceeded
                ? NotificationSeverity.CRITICAL
                : NotificationSeverity.WARNING;

        NotificationType type = exceeded
                ? NotificationType.BUDGET_EXCEEDED
                : NotificationType.BUDGET_ALERT;

        String title = exceeded
                ? "Budget exceeded for " + request.getBudgetName()
                : "Budget threshold reached for " + request.getBudgetName();

        String currency = request.getCurrency() == null || request.getCurrency().isBlank()
                ? "INR"
                : request.getCurrency().toUpperCase(Locale.ROOT);

        String message = exceeded
                ? String.format(
                        Locale.US,
                        "%s budget is over the limit. Spent %s %.2f out of %s %.2f (%.2f%% used).",
                        request.getBudgetName(),
                        currency,
                        request.getSpentAmount(),
                        currency,
                        request.getLimitAmount(),
                        usagePercent
                )
                : String.format(
                        Locale.US,
                        "%s budget has reached the %.2f%% alert threshold. Spent %s %.2f out of %s %.2f (%.2f%% used).",
                        request.getBudgetName(),
                        request.getAlertThreshold(),
                        currency,
                        request.getSpentAmount(),
                        currency,
                        request.getLimitAmount(),
                        usagePercent
                );

        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setRecipientId(request.getRecipientId());
        notificationRequest.setRecipientEmail(request.getRecipientEmail());
        notificationRequest.setType(type);
        notificationRequest.setSeverity(severity);
        notificationRequest.setTitle(title);
        notificationRequest.setMessage(message);
        notificationRequest.setRelatedId(request.getBudgetId());
        notificationRequest.setRelatedType(request.getCategoryId() == null ? "BUDGET" : "CATEGORY_BUDGET");
        notificationRequest.setEmailEnabled(severity == NotificationSeverity.CRITICAL);

        return send(notificationRequest);
    }

    @Override
    public List<Notification> sendBulk(BulkNotificationRequest request) {
        return request.getNotifications().stream()
                .map(this::send)
                .toList();
    }

    @Override
    public void sendEmail(EmailRequest request) {
        emailService.sendEmail(request);
    }

    @Override
    public List<Notification> getByRecipient(Long recipientId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId);
    }

    @Override
    public List<Notification> getUnreadByRecipient(Long recipientId) {
        return notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(recipientId);
    }

    @Override
    public long getUnreadCount(Long recipientId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(recipientId);
    }

    @Override
    public List<Notification> getAll() {
        return notificationRepository.findAll().stream()
                .sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
                .toList();
    }

    @Override
    public Notification markAsRead(Long notificationId) {
        Notification notification = getNotification(notificationId);
        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
        }
        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> markAllRead(Long recipientId) {
        List<Notification> notifications = notificationRepository
                .findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(recipientId);
        LocalDateTime now = LocalDateTime.now();
        notifications.forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(now);
        });
        return notificationRepository.saveAll(notifications);
    }

    @Override
    public Notification acknowledge(Long notificationId) {
        Notification notification = getNotification(notificationId);
        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
        }
        notification.setIsAcknowledged(true);
        notification.setAcknowledgedAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    @Override
    public void deleteNotification(Long notificationId) {
        Notification notification = getNotification(notificationId);
        notificationRepository.delete(notification);
    }

    private Notification buildNotification(NotificationRequest request) {
        return Notification.builder()
                .recipientId(request.getRecipientId())
                .recipientEmail(request.getRecipientEmail())
                .type(request.getType())
                .severity(request.getSeverity())
                .title(request.getTitle())
                .message(request.getMessage())
                .relatedId(request.getRelatedId())
                .relatedType(request.getRelatedType())
                .build();
    }

    private Notification getNotification(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found: " + notificationId
                ));
    }

    private void maybeSendEmail(Notification notification, boolean emailEnabled) {
        if (!emailEnabled) {
            return;
        }
        if (notification.getRecipientEmail() == null || notification.getRecipientEmail().isBlank()) {
            throw new IllegalArgumentException("recipientEmail is required when emailEnabled is true");
        }

        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setTo(notification.getRecipientEmail());
        emailRequest.setSubject(notification.getTitle());
        emailRequest.setBody(notification.getMessage());
        emailService.sendEmail(emailRequest);
    }

    private double calculateUsagePercent(double spentAmount, double limitAmount) {
        if (limitAmount <= 0) {
            throw new IllegalArgumentException("limitAmount must be greater than zero");
        }
        return (spentAmount / limitAmount) * 100.0;
    }
}
