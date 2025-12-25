package me.gg.pinit.pinittask.domain.statistics.model;

import jakarta.persistence.*;
import lombok.Getter;
import me.gg.pinit.pinittask.domain.converter.service.DurationConverter;
import me.gg.pinit.pinittask.domain.datetime.ZonedDateAttribute;

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


    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "date", column = @Column(name = "start_of_week_date")),
            @AttributeOverride(name = "offsetId", column = @Column(name = "start_of_week_offset_id"))
    })
    private ZonedDateAttribute startOfWeek;

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
        this.startOfWeek = ZonedDateAttribute.from(startOfWeek);
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

    public ZonedDateTime getStartOfWeek() {
        return startOfWeek.toZonedDateTime();
    }
}
