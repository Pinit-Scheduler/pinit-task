package me.gg.pinit.pinittask.application.events;

import me.gg.pinit.pinittask.domain.events.DomainEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DomainEventPublisherTest {
    @Mock
    ApplicationEventPublisher applicationEventPublisher;
    @InjectMocks
    DomainEventPublisher domainEventPublisher;

    @Test
    void delegatesToSpringPublisher() {
        //given
        DomainEvent event = new DummyEvent("payload");

        //when
        domainEventPublisher.publish(event);

        //then
        verify(applicationEventPublisher).publishEvent(event);
    }

    private record DummyEvent(String value) implements DomainEvent {
    }
}
