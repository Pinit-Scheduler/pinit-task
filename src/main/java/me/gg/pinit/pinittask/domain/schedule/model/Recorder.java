package me.gg.pinit.pinittask.domain.schedule.model;

import me.gg.pinit.pinittask.domain.statistics.model.Statistics;

import java.time.Duration;

/**
 * Statistics recorder for each schedule type.
 * Implementations update aggregated statistics when a schedule finishes or is rolled back.
 */
public interface Recorder {
    void record(Statistics statistics, Duration duration);

    void rollback(Statistics statistics, Duration duration);
}
