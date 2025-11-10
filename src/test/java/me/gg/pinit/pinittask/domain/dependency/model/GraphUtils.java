package me.gg.pinit.pinittask.domain.dependency.model;

import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.domain.schedule.model.TaskType;
import me.gg.pinit.pinittask.domain.schedule.vo.ImportanceConstraint;
import me.gg.pinit.pinittask.domain.schedule.vo.TemporalConstraint;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static me.gg.pinit.pinittask.domain.schedule.model.ScheduleUtils.getNotStartedSchedule;

public class GraphUtils {
    public static ZonedDateTime TIME_1 = ZonedDateTime.of(2025,
            10,
            1,
            10,
            0,
            0,
            0,
            Clock.systemDefaultZone().getZone());
    public static ZonedDateTime TIME_2 = TIME_1.plusDays(1);
    public static ZonedDateTime TIME_3 = TIME_2.plusDays(1);
    public static ZonedDateTime TIME_4 = TIME_3.plusDays(1);
    public static ZonedDateTime TIME_5 = TIME_4.plusDays(1);
    public static ZonedDateTime TIME_6 = TIME_5.plusDays(1);

    public static List<Dependency> getDependenciesSample() {
        TemporalConstraint tc1 = new TemporalConstraint(TIME_4, Duration.ofHours(5), TaskType.DEEP_WORK);
        TemporalConstraint tc2 = new TemporalConstraint(TIME_5, Duration.ofHours(5), TaskType.DEEP_WORK);
        TemporalConstraint tc3 = new TemporalConstraint(TIME_6, Duration.ofHours(5), TaskType.DEEP_WORK);
        ImportanceConstraint importanceConstraint = new ImportanceConstraint(5, 5);

        Schedule before1 = getNotStartedSchedule(1L, 1L, "A", "A", TIME_1, tc1, importanceConstraint);
        Schedule before2 = getNotStartedSchedule(2L, 1L, "B", "B", TIME_1, tc1, importanceConstraint);
        Schedule now = getNotStartedSchedule(3L, 1L, "C", "C", TIME_2, tc2, importanceConstraint);
        Schedule after1 = getNotStartedSchedule(4L, 1L, "D", "D", TIME_3, tc3, importanceConstraint);
        Schedule after2 = getNotStartedSchedule(5L, 1L, "E", "E", TIME_3, tc3, importanceConstraint);
        List<Dependency> dependencies = new ArrayList<>();
        now.addDependency(before1);
        now.addDependency(before2);
        after1.addDependency(now);
        after2.addDependency(now);
        dependencies.addAll(getDependenciesFromSchedule(now));
        dependencies.addAll(getDependenciesFromSchedule(after1));
        dependencies.addAll(getDependenciesFromSchedule(after2));
        return dependencies;
    }

    public static List<Dependency> getDependenciesFromSchedule(Schedule schedule) {
        Class<Schedule> cls = Schedule.class;
        try {
            Field dependenciesField = cls.getDeclaredField("dependencies");
            dependenciesField.setAccessible(true);
            Set<Dependency> nowDependencies = (Set<Dependency>) dependenciesField.get(schedule);
            return new ArrayList<>(nowDependencies);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
