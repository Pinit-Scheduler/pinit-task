package me.gg.pinit.pinittask.infrastructure.events.schedule.dto;

public record ScheduleCanceledPayload(Long scheduleId, Long ownerId, String beforeState, String occurredAt,
                                      String idempotentKey) {
}
