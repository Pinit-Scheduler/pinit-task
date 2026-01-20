package me.gg.pinit.pinittask.domain.task.patch;

import me.gg.pinit.pinittask.domain.task.model.TaskType;

import java.time.ZonedDateTime;
import java.util.Optional;

public final class TaskPatch {
    private String title;
    private String description;
    private ZonedDateTime dueDate;
    private Integer importance;
    private Integer urgency;
    private TaskType taskType;

    public TaskPatch setTitle(String v) {
        this.title = v;
        return this;
    }

    public TaskPatch setDescription(String v) {
        this.description = v;
        return this;
    }

    public TaskPatch setDueDate(ZonedDateTime v) {
        this.dueDate = v;
        return this;
    }

    public TaskPatch setImportance(Integer v) {
        this.importance = v;
        return this;
    }

    public TaskPatch setUrgency(Integer v) {
        this.urgency = v;
        return this;
    }

    public TaskPatch setTaskType(TaskType v) {
        this.taskType = v;
        return this;
    }

    public Optional<String> title() {
        return Optional.ofNullable(title);
    }

    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    public Optional<ZonedDateTime> dueDate() {
        return Optional.ofNullable(dueDate);
    }

    public Optional<Integer> importance() {
        return Optional.ofNullable(importance);
    }

    public Optional<Integer> urgency() {
        return Optional.ofNullable(urgency);
    }

    public Optional<TaskType> taskType() {
        return Optional.ofNullable(taskType);
    }

    public boolean isEmpty() {
        return title == null && description == null && dueDate == null
                && importance == null && urgency == null && taskType == null;
    }
}
