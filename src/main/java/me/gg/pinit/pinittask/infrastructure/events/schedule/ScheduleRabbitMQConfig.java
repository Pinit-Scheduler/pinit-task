package me.gg.pinit.pinittask.infrastructure.events.schedule;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the schedule event exchange so event publishing succeeds even on a fresh broker.
 */
@Configuration
public class ScheduleRabbitMQConfig {

    @Bean
    public DirectExchange scheduleDirectExchange() {
        return ExchangeBuilder.directExchange(ScheduleMessaging.DIRECT_EXCHANGE)
                .durable(true)
                .build();
    }
}
