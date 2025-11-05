package me.gg.pinit.pinittask.domain.schedule.model;

import java.time.ZonedDateTime;

public interface ScheduleState {
    void start(Schedule ctx, ZonedDateTime startTime);
    void suspend(Schedule ctx, ZonedDateTime suspendTime);
    void cancel(Schedule ctx);
    void finish(Schedule ctx, ZonedDateTime finishTime);
}
