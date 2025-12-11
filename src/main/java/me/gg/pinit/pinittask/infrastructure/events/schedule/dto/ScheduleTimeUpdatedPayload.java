package me.gg.pinit.pinittask.infrastructure.events.schedule.dto;

public record ScheduleTimeUpdatedPayload(Long scheduleId, Long ownerId, String newUpcomingTime, String occurredAt,
                                         String idempotentKey) {
}
