package me.gg.pinit.pinittask.application.task.dto;

import lombok.Getter;
import me.gg.pinit.pinittask.application.schedule.dto.DependencyDto;
import me.gg.pinit.pinittask.domain.dependency.model.Dependency;
import me.gg.pinit.pinittask.domain.task.model.Task;
import me.gg.pinit.pinittask.domain.task.model.TaskType;
import me.gg.pinit.pinittask.domain.task.patch.TaskPatch;
import me.gg.pinit.pinittask.domain.task.vo.ImportanceConstraint;
import me.gg.pinit.pinittask.domain.task.vo.TemporalConstraint;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

public class TaskDependencyAdjustCommand {
    private final Long taskId;
    @Getter
    private final Long ownerId;
    @Getter
    private final String title;
    @Getter
    private final String description;
    @Getter
    private final ZonedDateTime dueDate;
    @Getter
    private final Integer importance;
    @Getter
    private final Integer difficulty;
    @Getter
    private final TaskType taskType;
    private final List<DependencyDto> removeDependencies;
    private final List<DependencyDto> addDependencies;

    public TaskDependencyAdjustCommand(Long taskId, Long ownerId, String title, String description, ZonedDateTime dueDate, Integer importance, Integer difficulty, TaskType taskType, List<DependencyDto> removeDependencies, List<DependencyDto> addDependencies) {
        this.taskId = taskId;
        this.ownerId = ownerId;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.importance = importance;
        this.difficulty = difficulty;
        this.taskType = taskType;
        this.removeDependencies = removeDependencies;
        this.addDependencies = addDependencies;
    }

    public Long getTaskId() {
        return Objects.requireNonNullElse(taskId, 0L);
    }

    public Task buildTask() {
        return new Task(
                ownerId,
                title,
                description,
                new TemporalConstraint(dueDate, Duration.ZERO, taskType),
                new ImportanceConstraint(importance, difficulty)
        );
    }

    public TaskPatch getTaskPatch() {
        return new TaskPatch()
                .setTitle(title)
                .setDescription(description)
                .setDueDate(dueDate)
                .setImportance(importance)
                .setDifficulty(difficulty)
                .setTaskType(taskType);
    }

    public List<Dependency> getRemoveDependencies() {
        return removeDependencies.stream().map(d -> new Dependency(ownerId, d.getFromId(), d.getToId())).toList();
    }

    public List<Dependency> getAddDependencies() {
        return addDependencies.stream().map(d -> new Dependency(ownerId, d.getFromId(), d.getToId())).toList();
    }
}
