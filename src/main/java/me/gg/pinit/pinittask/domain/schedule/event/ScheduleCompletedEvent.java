package me.gg.pinit.pinittask.domain.schedule.event;

import lombok.Getter;
import me.gg.pinit.pinittask.domain.events.DomainEvent;

@Getter
public class ScheduleCompletedEvent implements DomainEvent {
    private final Long scheduleId;
    private final Long ownerId;
    private final String beforeState;

    public ScheduleCompletedEvent(Long scheduleId, Long ownerId, String beforeState) {
        this.scheduleId = scheduleId;
        this.ownerId = ownerId;
        this.beforeState = beforeState;
    }
}
