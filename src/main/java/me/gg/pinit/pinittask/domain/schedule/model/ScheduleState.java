package me.gg.pinit.pinittask.domain.schedule.model;

public interface ScheduleState {
    void start(Schedule ctx);
    void suspend(Schedule ctx);
    void cancel(Schedule ctx);
    void finish(Schedule ctx);
}
