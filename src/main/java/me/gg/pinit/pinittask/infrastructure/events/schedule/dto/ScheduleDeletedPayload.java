package me.gg.pinit.pinittask.infrastructure.events.schedule.dto;

public record ScheduleDeletedPayload(Long scheduleId, Long ownerId, String occurredAt, String idempotentKey) {
}
