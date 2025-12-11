package me.gg.pinit.pinittask.infrastructure.events.schedule.dto;

public record ScheduleStartedPayload(Long scheduleId, Long ownerId, String beforeState, String occurredAt,
                                     String idempotentKey) {
}
