package me.gg.pinit.pinittask.domain.statistics.model;

import jakarta.persistence.*;
import lombok.Getter;
import me.gg.pinit.pinittask.domain.converter.service.DurationConverter;

import java.time.Duration;
import java.time.ZonedDateTime;

@Entity
@Getter
public class Statistics {
    @Id
    @Column(name = "statistics_id")
    @GeneratedValue
    private Long id;

    @Version
    private Long version;

    private Long memberId;

    private ZonedDateTime startOfWeek;

    @Convert(converter = DurationConverter.class)
    private Duration deepWorkElapsedTime;
    @Convert(converter = DurationConverter.class)
    private Duration adminWorkElapsedTime;
    @Convert(converter = DurationConverter.class)
    private Duration totalWorkElapsedTime;

    private int completedTaskCount;
    private int archivedDailyObjectiveCount;

    protected Statistics() {
    }

    public Statistics(Long memberId, ZonedDateTime startOfWeek) {
        this.memberId = memberId;
        this.startOfWeek = startOfWeek;
        this.deepWorkElapsedTime = Duration.ZERO;
        this.adminWorkElapsedTime = Duration.ZERO;
        this.totalWorkElapsedTime = Duration.ZERO;
    }

    public void addDeepWorkDuration(Duration duration) {
        this.deepWorkElapsedTime = this.deepWorkElapsedTime.plus(duration);
        this.totalWorkElapsedTime = this.totalWorkElapsedTime.plus(duration);
    }

    public void removeDeepWorkDuration(Duration duration) {
        this.deepWorkElapsedTime = this.deepWorkElapsedTime.minus(duration);
        this.totalWorkElapsedTime = this.totalWorkElapsedTime.minus(duration);
    }

    public void addAdminWorkDuration(Duration duration) {
        this.adminWorkElapsedTime = this.adminWorkElapsedTime.plus(duration);
        this.totalWorkElapsedTime = this.totalWorkElapsedTime.plus(duration);
    }

    public void removeAdminWorkDuration(Duration duration) {
        this.adminWorkElapsedTime = this.adminWorkElapsedTime.minus(duration);
        this.totalWorkElapsedTime = this.totalWorkElapsedTime.minus(duration);
    }

    public void addQuickWorkDuration(Duration duration) {
        this.totalWorkElapsedTime = this.totalWorkElapsedTime.plus(duration);
    }

    public void removeQuickWorkDuration(Duration duration) {
        this.totalWorkElapsedTime = this.totalWorkElapsedTime.minus(duration);
    }

    public Duration getDeepWorkElapsedTime() {
        return deepWorkElapsedTime;
    }

    public Duration getAdminWorkElapsedTime() {
        return adminWorkElapsedTime;
    }

    public Duration getTotalWorkElapsedTime() {
        return totalWorkElapsedTime;
    }

    public Long getMemberId() {
        return memberId;
    }

    public ZonedDateTime getStartOfWeek() {
        return startOfWeek;
    }
}
