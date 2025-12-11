package me.gg.pinit.pinittask.application.events;

import me.gg.pinit.pinittask.domain.events.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class DomainEventPublisher {
    ApplicationEventPublisher eventPublisher;

    public DomainEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publish(DomainEvent event) {
        eventPublisher.publishEvent(event);
    }
}
