package me.gg.pinit.pinittask.infrastructure.events.task;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the task event exchange so publishers don't fail when the broker is empty.
 */
@Configuration
public class TaskRabbitMQConfig {

    @Bean
    public DirectExchange taskDirectExchange() {
        return ExchangeBuilder.directExchange(TaskMessaging.DIRECT_EXCHANGE)
                .durable(true)
                .build();
    }
}
