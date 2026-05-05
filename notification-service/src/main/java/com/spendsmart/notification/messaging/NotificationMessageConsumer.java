package com.spendsmart.notification.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsmart.notification.dto.BudgetAlertRequest;
import com.spendsmart.notification.dto.NotificationRequest;
import com.spendsmart.notification.service.NotificationService;
import java.util.Map;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.messaging.notification.async-enabled", havingValue = "true")
public class NotificationMessageConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public NotificationMessageConsumer(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "${app.messaging.notification.queues.send}")
    public void consumeNotification(Map<String, Object> payload) {
        NotificationRequest request = objectMapper.convertValue(payload, NotificationRequest.class);
        notificationService.send(request);
    }

    @RabbitListener(queues = "${app.messaging.notification.queues.budget-alert}")
    public void consumeBudgetAlert(Map<String, Object> payload) {
        BudgetAlertRequest request = objectMapper.convertValue(payload, BudgetAlertRequest.class);
        notificationService.sendBudgetAlert(request);
    }
}
