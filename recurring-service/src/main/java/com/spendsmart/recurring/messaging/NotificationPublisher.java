package com.spendsmart.recurring.messaging;

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
    private final String sendRoutingKey;

    public NotificationPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${app.messaging.notification.exchange}") String exchange,
            @Value("${app.messaging.notification.routing.send}") String sendRoutingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.sendRoutingKey = sendRoutingKey;
    }

    public void publishNotification(Map<String, Object> payload) {
        rabbitTemplate.convertAndSend(exchange, sendRoutingKey, payload);
    }
}
