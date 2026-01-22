package me.gg.pinit.pinittask.domain.task.model;

import me.gg.pinit.pinittask.domain.task.vo.ImportanceConstraint;
import me.gg.pinit.pinittask.domain.task.vo.TemporalConstraint;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;

public class TaskUtils {
    public static ZonedDateTime DEADLINE_TIME = ZonedDateTime.of(2025, 10, 10, 0, 0, 0, 0, Clock.systemDefaultZone().getZone());

    public static TemporalConstraint getTemporalConstraintSample() {
        return new TemporalConstraint(DEADLINE_TIME, Duration.ZERO, TaskType.DEEP_WORK);
    }

    public static ImportanceConstraint getImportanceConstraintSample() {
        return new ImportanceConstraint(5, 5);
    }

    public static Task newTask(Long ownerId) {
        return new Task(ownerId, "Sample Task", "task description", getTemporalConstraintSample(), getImportanceConstraintSample());
    }

    public static Task newTask(Long ownerId, Long id) {
        Task task = newTask(ownerId);
        setTaskId(task, id);
        return task;
    }

    private static void setTaskId(Task task, Long id) {
        try {
            Field idField = Task.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(task, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
