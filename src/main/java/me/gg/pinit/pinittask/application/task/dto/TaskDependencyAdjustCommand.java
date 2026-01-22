package me.gg.pinit.pinittask.application.task.dto;

import lombok.Getter;
import me.gg.pinit.pinittask.application.schedule.dto.DependencyDto;
import me.gg.pinit.pinittask.domain.dependency.model.Dependency;
import me.gg.pinit.pinittask.domain.task.model.Task;
import me.gg.pinit.pinittask.domain.task.patch.TaskPatch;
import me.gg.pinit.pinittask.domain.task.vo.ImportanceConstraint;
import me.gg.pinit.pinittask.domain.task.vo.TemporalConstraint;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

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
    private final List<DependencyDto> removeDependencies;
    private final List<DependencyDto> addDependencies;

    public TaskDependencyAdjustCommand(Long taskId, Long ownerId, String title, String description, ZonedDateTime dueDate, Integer importance, Integer difficulty, List<DependencyDto> removeDependencies, List<DependencyDto> addDependencies) {
        this.taskId = taskId;
        this.ownerId = ownerId;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.importance = importance;
        this.difficulty = difficulty;
        this.removeDependencies = removeDependencies;
        this.addDependencies = addDependencies;
    }

    public Long getTaskId() {
        return taskId;
    }

    public Task buildTask() {
        return new Task(
                ownerId,
                title,
                description,
                new TemporalConstraint(dueDate, Duration.ZERO),
                new ImportanceConstraint(importance, difficulty)
        );
    }

    public TaskPatch getTaskPatch() {
        return new TaskPatch()
                .setTitle(title)
                .setDescription(description)
                .setDueDate(dueDate)
                .setImportance(importance)
                .setDifficulty(difficulty);
    }

    public List<Dependency> getRemoveDependencies(Long selfId) {
        return removeDependencies.stream()
                .map(d -> new Dependency(ownerId, resolveId(d.getFromId(), selfId), resolveId(d.getToId(), selfId)))
                .toList();
    }

    public List<Dependency> getAddDependencies(Long selfId) {
        return addDependencies.stream()
                .map(d -> new Dependency(ownerId, resolveId(d.getFromId(), selfId), resolveId(d.getToId(), selfId)))
                .toList();
    }

    private Long resolveId(Long rawId, Long selfId) {
        if (rawId == null) {
            return selfId;
        }
        return rawId;
    }
}
