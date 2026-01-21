package me.gg.pinit.pinittask.infrastructure.events.task.dto;

public record TaskCanceledPayload(Long taskId, Long ownerId, String occurredAt, String idempotentKey) {
}
