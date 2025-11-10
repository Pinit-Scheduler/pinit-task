package me.gg.pinit.pinittask.domain.schedule.model;

import me.gg.pinit.pinittask.domain.events.DomainEvents;
import me.gg.pinit.pinittask.domain.schedule.vo.ImportanceConstraint;
import me.gg.pinit.pinittask.domain.schedule.vo.TemporalConstraint;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;

public class ScheduleUtils {
    public static ZonedDateTime ENROLLED_TIME = ZonedDateTime.of(2025,
            10,
            1,
            10,
            0,
            0,
            0,
            Clock.systemDefaultZone().getZone());
    public static ZonedDateTime START_TIME = ZonedDateTime.of(2025,
            10,
            1,
            11,
            0,
            0,
            0,
            Clock.systemDefaultZone().getZone());
    public static ZonedDateTime SUSPEND_TIME = ZonedDateTime.of(2025,
            10,
            1,
            12,
            0,
            0,
            0,
            Clock.systemDefaultZone().getZone());
    public static ZonedDateTime RESTART_TIME = ZonedDateTime.of(2025,
            10,
            1,
            13,
            0,
            0,
            0,
            Clock.systemDefaultZone().getZone());
    public static ZonedDateTime FINISH_TIME = ZonedDateTime.of(2025,
            10,
            1,
            15,
            0,
            0,
            0,
            Clock.systemDefaultZone().getZone());
    public static ZonedDateTime DEADLINE_TIME = ZonedDateTime.of(2025,
            10,
            10,
            0,
            0,
            0,
            0,
            Clock.systemDefaultZone().getZone());

    public static ZonedDateTime TIME_1 = ENROLLED_TIME;
    public static ZonedDateTime TIME_2 = ENROLLED_TIME.plusDays(1);
    public static ZonedDateTime TIME_3 = TIME_2.plusDays(1);
    public static ZonedDateTime TIME_4 = TIME_3.plusDays(1);
    public static ZonedDateTime TIME_5 = TIME_4.plusDays(1);
    public static ZonedDateTime TIME_6 = TIME_5.plusDays(1);

    public static Schedule getConnectedScheduleSample() {
        Schedule before1 = new Schedule(1L, "A", "A", TIME_1, new TemporalConstraint(TIME_4, Duration.ofHours(5), TaskType.DEEP_WORK), new ImportanceConstraint(5, 5));
        Schedule before2 = new Schedule(1L, "B", "B", TIME_1, new TemporalConstraint(TIME_4, Duration.ofHours(5), TaskType.DEEP_WORK), new ImportanceConstraint(5, 5));
        Schedule now = new Schedule(1L, "C", "C", TIME_2, new TemporalConstraint(TIME_5, Duration.ofHours(5), TaskType.DEEP_WORK), new ImportanceConstraint(5, 5));
        Schedule after1 = new Schedule(1L, "D", "D", TIME_3, new TemporalConstraint(TIME_6, Duration.ofHours(5), TaskType.DEEP_WORK), new ImportanceConstraint(5, 5));
        Schedule after2 = new Schedule(1L, "E", "E", TIME_3, new TemporalConstraint(TIME_6, Duration.ofHours(5), TaskType.DEEP_WORK), new ImportanceConstraint(5, 5));
        now.addDependency(before1);
        now.addDependency(before2);
        after1.addDependency(now);
        after2.addDependency(now);
        return now;
    }

    public static Schedule getNotStartedSchedule() {
        return new Schedule(
                1L,
                "Sample Schedule",
                "sample description",
                ENROLLED_TIME,
                getTemporalConstraintSample(),
                getImportanceConstraintSample());
    }

    public static Schedule getInProgressSchedule() {
        Schedule schedule = new Schedule(
                1L,
                "Sample Schedule",
                "sample description",
                ENROLLED_TIME,
                getTemporalConstraintSample(),
                getImportanceConstraintSample());
        schedule.start(START_TIME);
        return schedule;
    }

    public static Schedule getSuspendedSchedule() {
        Schedule schedule = new Schedule(
                1L,
                "Sample Schedule",
                "sample description",
                ENROLLED_TIME,
                getTemporalConstraintSample(),
                getImportanceConstraintSample());
        schedule.start(START_TIME);
        schedule.suspend(SUSPEND_TIME);
        return schedule;
    }

    public static Schedule getCompletedSchedule() {
        Schedule schedule = new Schedule(
                1L,
                "Sample Schedule",
                "sample description",
                ENROLLED_TIME,
                getTemporalConstraintSample(),
                getImportanceConstraintSample());
        schedule.start(START_TIME);
        schedule.finish(FINISH_TIME);
        DomainEvents.getEventsAndClear();
        return schedule;
    }

    public static TemporalConstraint getTemporalConstraintSample(){
        return new TemporalConstraint(
                DEADLINE_TIME,
                Duration.ZERO,
                TaskType.DEEP_WORK);
    }

    public static ImportanceConstraint getImportanceConstraintSample(){
        return new ImportanceConstraint(5, 5);
    }
}
