package me.gg.pinit.pinittask.domain.statistics.model;

import jakarta.persistence.*;
import me.gg.pinit.pinittask.domain.converter.service.DurationConverter;

import java.time.Duration;
import java.time.ZonedDateTime;

@Entity
public class Statistics {
    @Id
    @Column(name = "statistics_id")
    @GeneratedValue
    private Long id;

    private Long memberId;

    private ZonedDateTime startOfWeek;

    @Convert(converter = DurationConverter.class)
    private Duration deepWorkDuration;
    @Convert(converter = DurationConverter.class)
    private Duration adminWorkDuration;
    @Convert(converter = DurationConverter.class)
    private Duration totalWorkDuration;

    private int completedTaskCount;
    private int archivedDailyObjectiveCount;

    protected Statistics() {
    }

    public Statistics(Long memberId, ZonedDateTime startOfWeek) {
        this.memberId = memberId;
        this.startOfWeek = startOfWeek;
        this.deepWorkDuration = Duration.ZERO;
        this.adminWorkDuration = Duration.ZERO;
    }

    public void addDeepWorkDuration(Duration duration) {
        this.deepWorkDuration = this.deepWorkDuration.plus(duration);
        this.totalWorkDuration = this.totalWorkDuration.plus(duration);
    }

    public void removeDeepWorkDuration(Duration duration) {
        this.deepWorkDuration = this.deepWorkDuration.minus(duration);
        this.totalWorkDuration = this.totalWorkDuration.minus(duration);
    }

    public void addAdminWorkDuration(Duration duration) {
        this.adminWorkDuration = this.adminWorkDuration.plus(duration);
        this.totalWorkDuration = this.totalWorkDuration.plus(duration);
    }

    public void removeAdminWorkDuration(Duration duration) {
        this.adminWorkDuration = this.adminWorkDuration.minus(duration);
        this.totalWorkDuration = this.totalWorkDuration.minus(duration);
    }

    public void addQuickWorkDuration(Duration duration) {
        this.totalWorkDuration = this.totalWorkDuration.plus(duration);
    }

    public void removeQuickWorkDuration(Duration duration) {
        this.totalWorkDuration = this.totalWorkDuration.minus(duration);
    }
}

