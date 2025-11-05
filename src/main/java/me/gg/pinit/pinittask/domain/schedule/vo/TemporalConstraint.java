package me.gg.pinit.pinittask.domain.schedule.vo;

import jakarta.persistence.*;
import lombok.Getter;
import me.gg.pinit.pinittask.domain.schedule.model.TaskType;
import me.gg.pinit.pinittask.domain.converter.service.DurationConverter;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;

@Embeddable
public class TemporalConstraint {
    @Column(name = "deadline_time")
    @Getter
    private ZonedDateTime deadline;
    @Convert(converter = DurationConverter.class)
    @Column(name = "expected_duration")
    private Duration duration;
    @Getter
    @Enumerated(EnumType.STRING)
    private TaskType taskType;

    protected TemporalConstraint() {
    }

    public TemporalConstraint(ZonedDateTime deadline, Duration duration, TaskType taskType) {
        this.deadline = deadline;
        this.duration = duration;
        this.taskType = taskType;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TemporalConstraint that = (TemporalConstraint) o;
        return Objects.equals(deadline, that.deadline) && Objects.equals(duration, that.duration) && taskType == that.taskType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(deadline, duration, taskType);
    }
}
