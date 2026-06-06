package org.example.notification.config;

import org.example.notification.constant.NotificationConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "notification.mq", name = "enabled", havingValue = "true")
public class RabbitMqConfig {

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(NotificationConstants.EXCHANGE, true, false);
    }

    @Bean
    public Queue smsQueue() {
        return new Queue(NotificationConstants.SMS_QUEUE, true);
    }

    @Bean
    public Binding smsBinding(Queue smsQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(smsQueue)
                .to(notificationExchange)
                .with(NotificationConstants.SMS_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
