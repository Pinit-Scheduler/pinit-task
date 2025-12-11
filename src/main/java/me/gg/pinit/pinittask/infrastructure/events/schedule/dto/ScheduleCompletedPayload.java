package me.gg.pinit.pinittask.infrastructure.events.schedule.dto;

public record ScheduleCompletedPayload(Long scheduleId, Long ownerId, String beforeState, String occurredAt,
                                       String idempotentKey) {
}
