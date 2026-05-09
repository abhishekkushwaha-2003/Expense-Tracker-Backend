package com.spendsmart.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.spendsmart.notification.dto.BudgetAlertRequest;
import com.spendsmart.notification.dto.BulkNotificationRequest;
import com.spendsmart.notification.dto.EmailRequest;
import com.spendsmart.notification.dto.NotificationRequest;
import com.spendsmart.notification.entity.Notification;
import com.spendsmart.notification.entity.NotificationSeverity;
import com.spendsmart.notification.entity.NotificationType;
import com.spendsmart.notification.repository.NotificationRepository;
import com.spendsmart.notification.service.impl.NotificationServiceImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationServiceImpl service;

    @Test
    void sendPersistsNotificationAndDispatchesEmail() {
        NotificationRequest request = new NotificationRequest();
        request.setRecipientId(4L);
        request.setRecipientEmail("user@example.com");
        request.setType(NotificationType.SYSTEM);
        request.setSeverity(NotificationSeverity.INFO);
        request.setTitle("Welcome");
        request.setMessage("Hello");
        request.setEmailEnabled(true);

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification saved = service.send(request);

        assertEquals("Welcome", saved.getTitle());
        ArgumentCaptor<EmailRequest> emailCaptor = ArgumentCaptor.forClass(EmailRequest.class);
        verify(emailService).sendEmail(emailCaptor.capture());
        assertEquals("user@example.com", emailCaptor.getValue().getTo());
    }

    @Test
    void sendPersistsNotificationWithoutEmailWhenDisabled() {
        NotificationRequest request = new NotificationRequest();
        request.setRecipientId(4L);
        request.setRecipientEmail("");
        request.setType(NotificationType.SYSTEM);
        request.setSeverity(NotificationSeverity.INFO);
        request.setTitle("Welcome");
        request.setMessage("Hello");
        request.setEmailEnabled(false);

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification saved = service.send(request);

        assertEquals("Hello", saved.getMessage());
        verify(emailService, never()).sendEmail(any());
    }

    @Test
    void sendRequiresEmailWhenEmailEnabled() {
        NotificationRequest request = new NotificationRequest();
        request.setRecipientId(4L);
        request.setType(NotificationType.SYSTEM);
        request.setSeverity(NotificationSeverity.INFO);
        request.setTitle("Welcome");
        request.setMessage("Hello");
        request.setEmailEnabled(true);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(IllegalArgumentException.class, () -> service.send(request));
    }

    @Test
    void sendBudgetAlertBuildsWarningNotification() {
        BudgetAlertRequest request = new BudgetAlertRequest();
        request.setRecipientId(8L);
        request.setRecipientEmail("budget@example.com");
        request.setBudgetId(3L);
        request.setBudgetName("Travel");
        request.setSpentAmount(800.0);
        request.setLimitAmount(1000.0);
        request.setAlertThreshold(75.0);
        request.setCurrency("INR");

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification saved = service.sendBudgetAlert(request);

        assertEquals(NotificationType.BUDGET_ALERT, saved.getType());
        assertEquals(NotificationSeverity.WARNING, saved.getSeverity());
        assertTrue(saved.getMessage().contains("80.00% used"));
    }

    @Test
    void sendBudgetAlertBuildsExceededNotificationWithDefaults() {
        BudgetAlertRequest request = new BudgetAlertRequest();
        request.setRecipientId(8L);
        request.setRecipientEmail("budget@example.com");
        request.setBudgetId(3L);
        request.setBudgetName("  ");
        request.setSpentAmount(1200.0);
        request.setLimitAmount(1000.0);
        request.setAlertThreshold(75.0);
        request.setCurrency("");

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification saved = service.sendBudgetAlert(request);

        assertEquals(NotificationType.BUDGET_EXCEEDED, saved.getType());
        assertEquals(NotificationSeverity.CRITICAL, saved.getSeverity());
        assertEquals("BUDGET", saved.getRelatedType());
        assertTrue(saved.getTitle().contains("Monthly Budget"));
        assertTrue(saved.getMessage().contains("100.00% used"));
    }

    @Test
    void sendBudgetAlertRejectsZeroLimit() {
        BudgetAlertRequest request = new BudgetAlertRequest();
        request.setSpentAmount(100.0);
        request.setLimitAmount(0.0);

        assertThrows(IllegalArgumentException.class, () -> service.sendBudgetAlert(request));
    }

    @Test
    void sendBulkSendsEveryNotification() {
        NotificationRequest first = new NotificationRequest();
        first.setRecipientId(1L);
        first.setType(NotificationType.SYSTEM);
        first.setSeverity(NotificationSeverity.INFO);
        first.setTitle("One");
        first.setMessage("First");
        NotificationRequest second = new NotificationRequest();
        second.setRecipientId(2L);
        second.setType(NotificationType.SYSTEM);
        second.setSeverity(NotificationSeverity.WARNING);
        second.setTitle("Two");
        second.setMessage("Second");
        BulkNotificationRequest request = new BulkNotificationRequest();
        request.setNotifications(List.of(first, second));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<Notification> sent = service.sendBulk(request);

        assertEquals(2, sent.size());
        assertEquals("One", sent.get(0).getTitle());
        assertEquals("Two", sent.get(1).getTitle());
    }

    @Test
    void sendEmailDelegatesToEmailService() {
        EmailRequest request = new EmailRequest();
        request.setTo("user@example.com");

        service.sendEmail(request);

        verify(emailService).sendEmail(request);
    }

    @Test
    void queryMethodsDelegateToRepository() {
        Notification notification = Notification.builder().notificationId(1L).recipientId(5L).createdAt(java.time.LocalDateTime.now()).build();
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(5L)).thenReturn(List.of(notification));
        when(notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(5L)).thenReturn(List.of(notification));
        when(notificationRepository.countByRecipientIdAndIsReadFalse(5L)).thenReturn(1L);

        assertEquals(List.of(notification), service.getByRecipient(5L));
        assertEquals(List.of(notification), service.getUnreadByRecipient(5L));
        assertEquals(1L, service.getUnreadCount(5L));
    }

    @Test
    void getAllSortsNewestFirst() {
        Notification older = Notification.builder().notificationId(1L).createdAt(java.time.LocalDateTime.now().minusDays(1)).build();
        Notification newer = Notification.builder().notificationId(2L).createdAt(java.time.LocalDateTime.now()).build();
        when(notificationRepository.findAll()).thenReturn(List.of(older, newer));

        assertEquals(List.of(newer, older), service.getAll());
    }

    @Test
    void markAsReadUpdatesReadState() {
        Notification notification = Notification.builder().notificationId(9L).isRead(false).build();
        when(notificationRepository.findById(9L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        Notification updated = service.markAsRead(9L);

        assertEquals(true, updated.getIsRead());
    }

    @Test
    void markAsReadKeepsAlreadyReadNotificationRead() {
        Notification notification = Notification.builder().notificationId(9L).isRead(true).build();
        when(notificationRepository.findById(9L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        Notification updated = service.markAsRead(9L);

        assertEquals(true, updated.getIsRead());
        assertEquals(null, updated.getReadAt());
    }

    @Test
    void markAllReadUpdatesUnreadNotifications() {
        Notification notification = Notification.builder().notificationId(9L).isRead(false).build();
        when(notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(5L)).thenReturn(List.of(notification));
        when(notificationRepository.saveAll(List.of(notification))).thenReturn(List.of(notification));

        List<Notification> updated = service.markAllRead(5L);

        assertEquals(true, updated.get(0).getIsRead());
        assertTrue(updated.get(0).getReadAt() != null);
    }

    @Test
    void acknowledgeMarksUnreadNotificationReadAndAcknowledged() {
        Notification notification = Notification.builder().notificationId(9L).isRead(false).isAcknowledged(false).build();
        when(notificationRepository.findById(9L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        Notification updated = service.acknowledge(9L);

        assertEquals(true, updated.getIsRead());
        assertEquals(true, updated.getIsAcknowledged());
        assertTrue(updated.getAcknowledgedAt() != null);
    }

    @Test
    void acknowledgeAlreadyReadNotificationOnlyAcknowledges() {
        Notification notification = Notification.builder().notificationId(9L).isRead(true).isAcknowledged(false).build();
        when(notificationRepository.findById(9L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        Notification updated = service.acknowledge(9L);

        assertEquals(true, updated.getIsAcknowledged());
        assertEquals(null, updated.getReadAt());
    }

    @Test
    void deleteNotificationDeletesExistingNotification() {
        Notification notification = Notification.builder().notificationId(9L).build();
        when(notificationRepository.findById(9L)).thenReturn(Optional.of(notification));

        service.deleteNotification(9L);

        verify(notificationRepository).delete(notification);
    }

    @Test
    void missingNotificationThrowsResourceNotFound() {
        when(notificationRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.markAsRead(404L));
    }
}

