package me.gg.pinit.pinittask.infrastructure.events;

import me.gg.pinit.pinittask.domain.events.DomainEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RabbitEventPublisherTest {

    @Mock
    RabbitTemplate rabbitTemplate;

    RabbitEventPublisher rabbitEventPublisher;

    @BeforeEach
    void setUp() {
        rabbitEventPublisher = new RabbitEventPublisher(
                rabbitTemplate,
                List.of(new MappedEventMapper())
        );
    }

    @Test
    void publish_withMappedEvent_sendsMessage() {
        MappedEvent event = new MappedEvent(10L);

        rabbitEventPublisher.publish(event);

        verify(rabbitTemplate).convertAndSend("exchange.test", "routing.test", "payload-10");
    }

    @Test
    void publish_withoutMapper_doesNothing() {
        rabbitEventPublisher.publish(new UnmappedEvent());

        verifyNoInteractions(rabbitTemplate);
    }

    private record MappedEvent(Long id) implements DomainEvent {
    }

    private record UnmappedEvent() implements DomainEvent {
    }

    private static class MappedEventMapper implements AmqpEventMapper<MappedEvent> {
        @Override
        public Class<MappedEvent> eventType() {
            return MappedEvent.class;
        }

        @Override
        public String exchange() {
            return "exchange.test";
        }

        @Override
        public String routingKey() {
            return "routing.test";
        }

        @Override
        public Object payload(MappedEvent event) {
            return "payload-" + event.id();
        }
    }
}
