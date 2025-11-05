package me.gg.pinit.pinittask.domain.schedule.model;

import me.gg.pinit.pinittask.domain.schedule.vo.ImportanceConstraint;
import me.gg.pinit.pinittask.domain.schedule.vo.TemporalConstraint;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;

public class ScheduleUtils {
    public static Schedule getScheduleSample(){
        return new Schedule(
                1L,
                "Sample Schedule",
                "sample description",
                ZonedDateTime.of(2025,
                        10,
                        1,
                        10,
                        0,
                        0,
                        0,
                        Clock.systemDefaultZone().getZone()),
                getTemporalConstraintSample(),
                getImportanceConstraintSample());
    }

    public static TemporalConstraint getTemporalConstraintSample(){
        return new TemporalConstraint(
                ZonedDateTime.of(2025,
                        10,
                        10,
                        0,
                        0,
                        0,
                        0,
                        Clock.systemDefaultZone().getZone()),
                Duration.ZERO,
                TaskType.DEEP_WORK);
    }

    public static ImportanceConstraint getImportanceConstraintSample(){
        return new ImportanceConstraint(5, 5);
    }
}
