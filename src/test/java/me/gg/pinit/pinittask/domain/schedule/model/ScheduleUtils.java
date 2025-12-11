package me.gg.pinit.pinittask.domain.schedule.model;

import me.gg.pinit.pinittask.domain.events.DomainEvents;
import me.gg.pinit.pinittask.domain.schedule.vo.ImportanceConstraint;
import me.gg.pinit.pinittask.domain.schedule.vo.TemporalConstraint;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;

public class ScheduleUtils {
    public static ZonedDateTime ENROLLED_TIME = ZonedDateTime.of(2025, 10, 1, 10, 0, 0, 0, Clock.systemDefaultZone().getZone());
    public static ZonedDateTime START_TIME = ZonedDateTime.of(2025, 10, 1, 11, 0, 0, 0, Clock.systemDefaultZone().getZone());
    public static ZonedDateTime SUSPEND_TIME = ZonedDateTime.of(2025, 10, 1, 12, 0, 0, 0, Clock.systemDefaultZone().getZone());
    public static ZonedDateTime RESTART_TIME = ZonedDateTime.of(2025, 10, 1, 13, 0, 0, 0, Clock.systemDefaultZone().getZone());
    public static ZonedDateTime FINISH_TIME = ZonedDateTime.of(2025, 10, 1, 15, 0, 0, 0, Clock.systemDefaultZone().getZone());
    public static ZonedDateTime DEADLINE_TIME = ZonedDateTime.of(2025, 10, 10, 0, 0, 0, 0, Clock.systemDefaultZone().getZone());

    public static ZonedDateTime TIME_1 = ENROLLED_TIME;
    public static ZonedDateTime TIME_2 = ENROLLED_TIME.plusDays(1);
    public static ZonedDateTime TIME_3 = TIME_2.plusDays(1);
    public static ZonedDateTime TIME_4 = TIME_3.plusDays(1);
    public static ZonedDateTime TIME_5 = TIME_4.plusDays(1);
    public static ZonedDateTime TIME_6 = TIME_5.plusDays(1);


    public static Schedule getNotStartedSchedule() {
        Schedule schedule = new Schedule(1L, "Sample Schedule", "sample description", ENROLLED_TIME, getTemporalConstraintSample(), getImportanceConstraintSample());
        DomainEvents.getEventsAndClear();
        return schedule;
    }

    public static Schedule getNotStartedSchedule(
            Long id,
            Long ownerId,
            String title,
            String description,
            ZonedDateTime enrolledTime,
            TemporalConstraint temporalConstraint,
            ImportanceConstraint importanceConstraint
    ) {
        Schedule schedule = new Schedule(ownerId, title, description, enrolledTime, temporalConstraint, importanceConstraint);
        setScheduleId(schedule, id);
        return schedule;
    }

    public static Schedule getNotStartedSchedule(Long id) {
        Schedule schedule = getNotStartedSchedule();
        setScheduleId(schedule, id);
        return schedule;
    }

    public static Schedule getInProgressSchedule(
            Long id,
            Long ownerId,
            String title,
            String description,
            ZonedDateTime enrolledTime,
            TemporalConstraint temporalConstraint,
            ImportanceConstraint importanceConstraint
    ) {
        Schedule schedule = new Schedule(ownerId, title, description, enrolledTime, temporalConstraint, importanceConstraint);
        setScheduleId(schedule, id);
        schedule.start(START_TIME);
        return schedule;
    }


    public static Schedule getInProgressSchedule() {
        Schedule schedule = new Schedule(1L, "Sample Schedule", "sample description", ENROLLED_TIME, getTemporalConstraintSample(), getImportanceConstraintSample());
        schedule.start(START_TIME);
        DomainEvents.getEventsAndClear();
        return schedule;
    }

    public static Schedule getInProgressSchedule(Long id) {
        Schedule schedule = getInProgressSchedule();
        setScheduleId(schedule, id);
        return schedule;
    }

    public static Schedule getSuspendedSchedule(
            Long id,
            Long ownerId,
            String title,
            String description,
            ZonedDateTime enrolledTime,
            TemporalConstraint temporalConstraint,
            ImportanceConstraint importanceConstraint
    ) {
        Schedule schedule = new Schedule(ownerId, title, description, enrolledTime, temporalConstraint, importanceConstraint);
        setScheduleId(schedule, id);
        schedule.start(START_TIME);
        schedule.suspend(SUSPEND_TIME);
        DomainEvents.getEventsAndClear();
        return schedule;
    }

    public static Schedule getSuspendedSchedule(Long id) {
        Schedule schedule = getSuspendedSchedule();
        setScheduleId(schedule, id);
        DomainEvents.getEventsAndClear();
        return schedule;
    }

    public static Schedule getSuspendedSchedule() {
        Schedule schedule = new Schedule(1L, "Sample Schedule", "sample description", ENROLLED_TIME, getTemporalConstraintSample(), getImportanceConstraintSample());
        schedule.start(START_TIME);
        schedule.suspend(SUSPEND_TIME);
        DomainEvents.getEventsAndClear();
        return schedule;
    }

    public static Schedule getCompletedSchedule(
            Long id,
            Long ownerId,
            String title,
            String description,
            ZonedDateTime enrolledTime,
            TemporalConstraint temporalConstraint,
            ImportanceConstraint importanceConstraint
    ) {
        Schedule schedule = new Schedule(ownerId, title, description, enrolledTime, temporalConstraint, importanceConstraint);
        setScheduleId(schedule, id);
        schedule.start(START_TIME);
        schedule.finish(FINISH_TIME);
        DomainEvents.getEventsAndClear();
        return schedule;
    }

    public static Schedule getCompletedSchedule() {
        Schedule schedule = new Schedule(1L, "Sample Schedule", "sample description", ENROLLED_TIME, getTemporalConstraintSample(), getImportanceConstraintSample());
        schedule.start(START_TIME);
        schedule.finish(FINISH_TIME);
        DomainEvents.getEventsAndClear();
        return schedule;
    }

    public static Schedule getCompletedSchedule(Long id) {
        Schedule schedule = getCompletedSchedule();
        setScheduleId(schedule, id);
        DomainEvents.getEventsAndClear();
        return schedule;
    }

    public static TemporalConstraint getTemporalConstraintSample() {
        return new TemporalConstraint(DEADLINE_TIME, Duration.ZERO, TaskType.DEEP_WORK);
    }

    public static ImportanceConstraint getImportanceConstraintSample() {
        return new ImportanceConstraint(5, 5);
    }

    private static void setScheduleId(Schedule schedule, Long id) {
        try {
            Field idField = Schedule.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(schedule, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
