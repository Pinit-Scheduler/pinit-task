package me.gg.pinit.pinittask.domain.schedule.event;

import lombok.Getter;
import me.gg.pinit.pinittask.domain.events.DomainEvent;

@Getter
public class ScheduleDeletedEvent implements DomainEvent {
    private final Long scheduleId;
    private final Long ownerId;
    private final Long taskId;

    public ScheduleDeletedEvent(Long scheduleId, Long ownerId, Long taskId) {
        this.scheduleId = scheduleId;
        this.ownerId = ownerId;
        this.taskId = taskId;
    }
}
