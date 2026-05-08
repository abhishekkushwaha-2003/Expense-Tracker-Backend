package com.spendsmart.budget.messaging;

import java.util.Map;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.messaging.notification.async-enabled", havingValue = "true")
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String budgetAlertRoutingKey;

    public NotificationPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${app.messaging.notification.exchange}") String exchange,
            @Value("${app.messaging.notification.routing.budget-alert}") String budgetAlertRoutingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.budgetAlertRoutingKey = budgetAlertRoutingKey;
    }

    public void publishBudgetAlert(Map<String, Object> payload) {
        rabbitTemplate.convertAndSend(exchange, budgetAlertRoutingKey, payload);
    }
}
