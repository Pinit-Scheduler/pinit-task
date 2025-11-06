package me.gg.pinit.pinittask.domain.schedule.model;

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
