package me.gg.pinit.pinittask.domain.schedule.vo;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.Getter;
import me.gg.pinit.pinittask.domain.converter.service.DurationConverter;
import me.gg.pinit.pinittask.domain.datetime.ZonedDateTimeAttribute;
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
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "dateTime", column = @Column(name = "start_time")),
            @AttributeOverride(name = "zoneId", column = @Column(name = "start_zone_id"))
    })
    private ZonedDateTimeAttribute startTime;

    protected ScheduleHistory() {
    }

    public ScheduleHistory(ZonedDateTime startTime, Duration elapsedTime) {
        this.startTime = startTime == null ? null : ZonedDateTimeAttribute.from(startTime);
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

    public ZonedDateTime getStartTime() {
        return startTime == null ? null : startTime.toZonedDateTime();
    }

    public ScheduleHistory recordStart(ZonedDateTime startTime) {
        Objects.requireNonNull(startTime, "시작 시간은 null일 수 없습니다.");
        return new ScheduleHistory(startTime, this.elapsedTime);
    }

    public ScheduleHistory recordStop(ZonedDateTime stopTime) {
        isStartTimeRecorded(getStartTime());
        ZonedDateTime start = getStartTime();
        isStartTimePrecede(stopTime);
        Duration sessionDuration = Duration.between(start,  stopTime);
        return new ScheduleHistory(null, this.elapsedTime.plus(sessionDuration));
    }

    public ScheduleHistory rollback(){
        return new ScheduleHistory(null, this.elapsedTime);
    }

    public static ScheduleHistory zero(){
        return new ScheduleHistory(null, Duration.ZERO);
    }

    private void isStartTimePrecede(ZonedDateTime stopTime) {
        ZonedDateTime start = getStartTime();
        if(stopTime.isBefore(start)) {
            throw new TimeOrderReversedException("종료 시간은 시작 시간 이후여야 합니다.");
        }
    }

    private void isStartTimeRecorded(ZonedDateTime start) {
        if(start == null) {
            throw new StartNotRecordedException("시작 시간이 기록되지 않았습니다.");
        }
    }
}
