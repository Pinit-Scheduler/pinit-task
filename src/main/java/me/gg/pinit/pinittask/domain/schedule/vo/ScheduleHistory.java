package me.gg.pinit.pinittask.domain.schedule.vo;

import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import me.gg.pinit.pinittask.domain.converter.service.DurationConverter;
import me.gg.pinit.pinittask.domain.schedule.exception.StartNotRecordedException;
import me.gg.pinit.pinittask.domain.schedule.exception.TimeOrderReversedException;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;

@Getter
@Embeddable
public class ScheduleHistory {
    @Convert(converter = DurationConverter.class)
    private Duration elapsedTime;
    private ZonedDateTime startTime;

    protected ScheduleHistory() {
    }

    public ScheduleHistory(ZonedDateTime startTime, Duration elapsedTime) {
        this.startTime = startTime;
        this.elapsedTime = elapsedTime;
    }



    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ScheduleHistory that = (ScheduleHistory) o;
        return Objects.equals(elapsedTime, that.elapsedTime) && Objects.equals(startTime, that.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elapsedTime, startTime);
    }
    
    public ScheduleHistory recordStart(ZonedDateTime startTime) {
        Objects.requireNonNull(startTime, "시작 시간은 null일 수 없습니다.");
        return new ScheduleHistory(startTime, this.elapsedTime);
    }

    public ScheduleHistory recordStop(ZonedDateTime stopTime) {
        Objects.requireNonNull(this.startTime, "시작 시간이 기록되지 않았습니다.");
        validateTimeOrder(stopTime);
        Duration sessionDuration = Duration.between(this.startTime,  stopTime);
        return new ScheduleHistory(null, this.elapsedTime.plus(sessionDuration));
    }

    public static ScheduleHistory zero(){
        return new ScheduleHistory(null, Duration.ZERO);
    }

    private void validateTimeOrder(ZonedDateTime stopTime) {
        if(stopTime.isBefore(startTime)) {
            throw new TimeOrderReversedException("종료 시간은 시작 시간 이후여야 합니다.");
        }
    }
}
