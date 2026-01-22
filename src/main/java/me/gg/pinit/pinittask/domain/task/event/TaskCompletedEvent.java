package me.gg.pinit.pinittask.domain.task.event;

import me.gg.pinit.pinittask.domain.events.DomainEvent;

public record TaskCompletedEvent(Long taskId, Long ownerId) implements DomainEvent {
}
