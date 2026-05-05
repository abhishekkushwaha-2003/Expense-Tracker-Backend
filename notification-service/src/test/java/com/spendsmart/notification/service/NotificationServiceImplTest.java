package com.spendsmart.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.spendsmart.notification.dto.BudgetAlertRequest;
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
    void markAsReadUpdatesReadState() {
        Notification notification = Notification.builder().notificationId(9L).isRead(false).build();
        when(notificationRepository.findById(9L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        Notification updated = service.markAsRead(9L);

        assertEquals(true, updated.getIsRead());
    }
}

