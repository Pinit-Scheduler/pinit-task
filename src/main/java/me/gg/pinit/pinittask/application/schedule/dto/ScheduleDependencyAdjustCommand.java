package me.gg.pinit.pinittask.application.schedule.dto;

import lombok.Getter;
import me.gg.pinit.pinittask.domain.dependency.model.Dependency;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.domain.schedule.patch.SchedulePatch;
import me.gg.pinit.pinittask.domain.task.model.Task;
import me.gg.pinit.pinittask.domain.task.model.TaskType;
import me.gg.pinit.pinittask.domain.task.patch.TaskPatch;
import me.gg.pinit.pinittask.domain.task.vo.ImportanceConstraint;
import me.gg.pinit.pinittask.domain.task.vo.TemporalConstraint;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

public class ScheduleDependencyAdjustCommand {
    private final Long scheduleId;
    private final Long taskId;
    @Getter
    private final Long ownerId;
    @Getter
    private final String title;
    @Getter
    private final String description;
    @Getter
    private final ZonedDateTime deadline;
    @Getter
    private final Integer importance;
    @Getter
    private final Integer difficulty;
    @Getter
    private final TaskType taskType;
    @Getter
    private final ZonedDateTime date;
    private final List<DependencyDto> removeDependencies;
    private final List<DependencyDto> addDependencies;

    public ScheduleDependencyAdjustCommand(Long scheduleId, Long ownerId, Long taskId, String title, String description, ZonedDateTime deadline, Integer importance, Integer difficulty, TaskType taskType, ZonedDateTime date, List<DependencyDto> removeDependencies, List<DependencyDto> addDependencies) {
        this.scheduleId = scheduleId;
        this.ownerId = ownerId;
        this.taskId = taskId;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.importance = importance;
        this.difficulty = difficulty;
        this.taskType = taskType;
        this.date = date;
        this.removeDependencies = removeDependencies;
        this.addDependencies = addDependencies;
    }

    public Long getScheduleId() {
        return Objects.requireNonNullElse(scheduleId, 0L);
    }

    public Long getTaskId() {
        return taskId;
    }

    public boolean hasTaskId() {
        return taskId != null;
    }

    public Task buildTask() {
        return new Task(
                ownerId,
                title,
                description,
                new TemporalConstraint(deadline, Duration.ZERO, taskType),
                new ImportanceConstraint(importance, difficulty)
        );
    }

    public Schedule buildSchedule(Long taskId) {
        return new Schedule(
                ownerId,
                taskId,
                title,
                description,
                date
        );
    }

    public SchedulePatch getSchedulePatch() {
        return new SchedulePatch()
                .setTitle(title)
                .setDescription(description)
                .setDesignatedStartTime(date);
    }

    public TaskPatch getTaskPatch() {
        return new TaskPatch()
                .setTitle(title)
                .setDescription(description)
                .setDueDate(deadline)
                .setImportance(importance)
                .setDifficulty(difficulty)
                .setTaskType(taskType);
    }

    public List<Dependency> getRemoveDependencies() {
        return (removeDependencies == null ? List.<DependencyDto>of() : removeDependencies)
                .stream()
                .map(d -> new Dependency(d.getFromId(), d.getToId()))
                .toList();
    }

    public List<Dependency> getAddDependencies() {
        return (addDependencies == null ? List.<DependencyDto>of() : addDependencies)
                .stream()
                .map(d -> new Dependency(d.getFromId(), d.getToId()))
                .toList();
    }
}
