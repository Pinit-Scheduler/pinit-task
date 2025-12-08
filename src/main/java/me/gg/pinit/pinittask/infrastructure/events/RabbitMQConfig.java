package me.gg.pinit.pinittask.infrastructure.events;

import me.gg.pinit.pinittask.infrastructure.events.messaging.AuthMemberMessaging;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Bean
    public DirectExchange authMemberDirect() {
        return new DirectExchange(AuthMemberMessaging.DIRECT_EXCHANGE);
    }

    @Bean
    public Binding bindingMemberCreated(DirectExchange authMemberDirect, Queue memberCreatedQueue) {
        return BindingBuilder.bind(memberCreatedQueue)
                .to(authMemberDirect)
                .with(AuthMemberMessaging.BK_MEMBER_CREATED);
    }

    @Bean
    public Queue memberCreatedQueue() {
        return new Queue("member.created.queue", true);
    }
}
