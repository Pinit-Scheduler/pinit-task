package me.gg.pinit.pinittask.infrastructure.events.schedule;

public class ScheduleMessaging {
    public static final String DIRECT_EXCHANGE = "task.schedule.direct";
    public static final String RK_SCHEDULE_TIME_UPDATED = "schedule.time.upcoming.updated";
    public static final String RK_SCHEDULE_DELETED = "schedule.deleted";
    public static final String RK_SCHEDULE_CANCELED = "schedule.state.canceled";
    public static final String RK_SCHEDULE_STARTED = "schedule.state.started";
    public static final String RK_SCHEDULE_COMPLETED = "schedule.state.completed";
}
