package me.gg.pinit.pinittask.domain.schedule.event;

import lombok.Getter;
import lombok.Setter;
import me.gg.pinit.pinittask.domain.events.DomainEvent;
import me.gg.pinit.pinittask.domain.schedule.model.TaskType;

import java.time.Duration;

@Getter
public class ScheduleCanceledEvent implements DomainEvent {
    private final TaskType taskType;
    private final Duration duration;
    public ScheduleCanceledEvent(Long ownerId, TaskType taskType, Duration duration) {
        this.taskType = taskType;
        this.duration = duration;
    }
}
