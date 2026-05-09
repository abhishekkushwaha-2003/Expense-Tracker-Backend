package com.spendsmart.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsmart.notification.config.RabbitMqConfig;
import com.spendsmart.notification.controller.NotificationController;
import com.spendsmart.notification.dto.*;
import com.spendsmart.notification.entity.*;
import com.spendsmart.notification.exception.*;
import com.spendsmart.notification.messaging.NotificationMessageConsumer;
import com.spendsmart.notification.service.NotificationService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailSendException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationLayerCoverageTest {

    @Test
    void controllerDelegatesToService() {
        NotificationService service = mock(NotificationService.class);
        NotificationController controller = new NotificationController(service);
        Notification notification = Notification.builder().notificationId(1L).recipientId(10L).build();
        NotificationRequest request = new NotificationRequest();
        BudgetAlertRequest alert = new BudgetAlertRequest();
        BulkNotificationRequest bulk = new BulkNotificationRequest();
        EmailRequest email = new EmailRequest();
        when(service.send(request)).thenReturn(notification);
        when(service.sendBudgetAlert(alert)).thenReturn(notification);
        when(service.sendBulk(bulk)).thenReturn(List.of(notification));
        when(service.getByRecipient(10L)).thenReturn(List.of(notification));
        when(service.getUnreadByRecipient(10L)).thenReturn(List.of(notification));
        when(service.getUnreadCount(10L)).thenReturn(1L);
        when(service.getAll()).thenReturn(List.of(notification));
        when(service.markAsRead(1L)).thenReturn(notification);
        when(service.markAllRead(10L)).thenReturn(List.of(notification));
        when(service.acknowledge(1L)).thenReturn(notification);

        assertSame(notification, controller.send(request));
        assertSame(notification, controller.sendBudgetAlert(alert));
        assertEquals(List.of(notification), controller.sendBulk(bulk));
        controller.sendEmail(email);
        assertEquals(List.of(notification), controller.getByRecipient(10L));
        assertEquals(List.of(notification), controller.getUnread(10L));
        assertEquals(1L, controller.getUnreadCount(10L));
        assertEquals(List.of(notification), controller.getAll());
        assertSame(notification, controller.markAsRead(1L));
        assertEquals(List.of(notification), controller.markAllRead(10L));
        assertSame(notification, controller.acknowledge(1L));
        controller.delete(1L);
        verify(service).sendEmail(email);
        verify(service).deleteNotification(1L);
    }

    @Test
    void configConsumerEntityAndExceptionsWork() {
        RabbitMqConfig config = new RabbitMqConfig();
        DirectExchange exchange = config.notificationExchange("notifications");
        Queue sendQueue = config.notificationSendQueue("send.q");
        Queue alertQueue = config.budgetAlertQueue("alert.q");
        Binding sendBinding = config.notificationSendBinding(sendQueue, exchange, "send");
        Binding alertBinding = config.budgetAlertBinding(alertQueue, exchange, "alert");
        assertEquals("notifications", exchange.getName());
        assertEquals("send.q", sendQueue.getName());
        assertEquals("alert.q", alertQueue.getName());
        assertNotNull(sendBinding);
        assertNotNull(alertBinding);
        assertTrue(config.jsonMessageConverter() instanceof Jackson2JsonMessageConverter);

        Notification notification = Notification.builder().type(NotificationType.SYSTEM).severity(NotificationSeverity.INFO).build();
        notification.onCreate();
        assertFalse(notification.getIsRead());
        assertFalse(notification.getIsAcknowledged());
        assertNotNull(notification.getCreatedAt());

        NotificationService service = mock(NotificationService.class);
        NotificationMessageConsumer consumer = new NotificationMessageConsumer(service, new ObjectMapper());
        consumer.consumeNotification(java.util.Map.of("recipientId", 1L, "type", "SYSTEM", "severity", "INFO", "title", "T", "message", "M"));
        consumer.consumeBudgetAlert(java.util.Map.of("recipientId", 1L, "spentAmount", 1.0, "limitAmount", 2.0));
        verify(service).send(any(NotificationRequest.class));
        verify(service).sendBudgetAlert(any(BudgetAlertRequest.class));

        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        assertEquals(HttpStatus.NOT_FOUND, handler.handleNotFound(new ResourceNotFoundException("missing")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, handler.handleBadRequest(new IllegalArgumentException("bad")).getStatusCode());
        assertEquals(HttpStatus.BAD_GATEWAY, handler.handleMailError(new MailSendException("down")).getStatusCode());
    }
}