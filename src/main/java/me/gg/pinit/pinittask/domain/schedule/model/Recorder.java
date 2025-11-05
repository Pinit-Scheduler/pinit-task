package me.gg.pinit.pinittask.domain.schedule.model;

import me.gg.pinit.pinittask.domain.statistics.model.Statistics;

public interface Recorder {
    void record(Schedule ctx, Statistics statistics);
    void rollback(Schedule ctx, Statistics statistics);
}
