package com.spendsmart.notification.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.messaging.notification.async-enabled", havingValue = "true")
public class RabbitMqConfig {

    @Bean
    public DirectExchange notificationExchange(@Value("${app.messaging.notification.exchange}") String exchangeName) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue notificationSendQueue(@Value("${app.messaging.notification.queues.send}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public Queue budgetAlertQueue(@Value("${app.messaging.notification.queues.budget-alert}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding notificationSendBinding(
            @Qualifier("notificationSendQueue") Queue notificationSendQueue,
            DirectExchange notificationExchange,
            @Value("${app.messaging.notification.routing.send}") String routingKey
    ) {
        return BindingBuilder.bind(notificationSendQueue).to(notificationExchange).with(routingKey);
    }

    @Bean
    public Binding budgetAlertBinding(
            @Qualifier("budgetAlertQueue") Queue budgetAlertQueue,
            DirectExchange notificationExchange,
            @Value("${app.messaging.notification.routing.budget-alert}") String routingKey
    ) {
        return BindingBuilder.bind(budgetAlertQueue).to(notificationExchange).with(routingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
