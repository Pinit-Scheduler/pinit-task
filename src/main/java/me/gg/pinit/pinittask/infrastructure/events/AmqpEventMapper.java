package me.gg.pinit.pinittask.infrastructure.events;

import me.gg.pinit.pinittask.domain.events.DomainEvent;

public interface AmqpEventMapper<T extends DomainEvent> {
    Class<T> eventType();

    String exchange();

    String routingKey();

    Object payload(T event);
}
