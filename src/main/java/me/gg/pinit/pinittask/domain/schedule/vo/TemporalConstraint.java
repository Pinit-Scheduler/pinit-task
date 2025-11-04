package me.gg.pinit.pinittask.domain.schedule.vo;

import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import me.gg.pinit.pinittask.domain.schedule.model.AllocateType;
import me.gg.pinit.pinittask.domain.schedule.service.DurationConverter;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;

@Embeddable
public class TemporalConstraint {
    private ZonedDateTime deadline;
    @Convert(converter = DurationConverter.class)
    private Duration duration;
    @Enumerated(EnumType.STRING)
    private AllocateType allocateType;

    protected TemporalConstraint() {
    }

    public TemporalConstraint(ZonedDateTime deadline, Duration duration, AllocateType allocateType) {
        this.deadline = deadline;
        this.duration = duration;
        this.allocateType = allocateType;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TemporalConstraint that = (TemporalConstraint) o;
        return Objects.equals(deadline, that.deadline) && Objects.equals(duration, that.duration) && allocateType == that.allocateType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(deadline, duration, allocateType);
    }
}
