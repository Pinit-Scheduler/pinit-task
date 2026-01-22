package me.gg.pinit.pinittask.infrastructure.events.task.dto;

public record TaskCompletedPayload(Long taskId, Long ownerId, String occurredAt, String idempotentKey) {
}
