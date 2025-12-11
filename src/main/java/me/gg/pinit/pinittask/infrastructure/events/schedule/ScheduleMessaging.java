package me.gg.pinit.pinittask.infrastructure.events.schedule;

public class ScheduleMessaging {
    public static final String DIRECT_EXCHANGE = "task.schedule.direct";
    public static final String RK_SCHEDULE_TIME_CHANGED = "schedule.time.upcoming.changed";
    public static final String RK_SCHEDULE_TIME_ENROLLED = "schedule.time.upcoming.enrolled";
    public static final String RK_SCHEDULE_DELETED = "schedule.deleted";
    public static final String RK_SCHEDULE_CANCELED = "schedule.state.canceled";
    public static final String RK_SCHEDULE_STARTED = "schedule.state.started";
    public static final String RK_SCHEDULE_COMPLETED = "schedule.state.completed";
}
