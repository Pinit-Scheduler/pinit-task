package me.gg.pinit.pinittask.domain.schedule.model;

import me.gg.pinit.pinittask.domain.statistics.model.Statistics;

import java.time.Duration;

public interface Recorder {
    void record(Statistics statistics, Duration duration);

    void rollback(Statistics statistics, Duration duration);
}
