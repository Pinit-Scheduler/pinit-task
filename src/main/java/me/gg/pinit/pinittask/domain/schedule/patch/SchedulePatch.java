package me.gg.pinit.pinittask.domain.schedule.patch;

import me.gg.pinit.pinittask.domain.schedule.model.TaskType;

import java.time.ZonedDateTime;
import java.util.Optional;

public final class SchedulePatch {
    private String title;
    private String description;
    private ZonedDateTime deadline;
    private Integer importance;
    private Integer difficulty;
    private TaskType taskType;
    private ZonedDateTime date;

    public SchedulePatch setTitle(String v) {
        this.title = v;
        return this;
    }

    public SchedulePatch setDescription(String v) {
        this.description = v;
        return this;
    }

    public SchedulePatch setDeadline(ZonedDateTime v) {
        this.deadline = v;
        return this;
    }

    public SchedulePatch setImportance(Integer v) {
        this.importance = v;
        return this;
    }

    public SchedulePatch setDifficulty(Integer v) {
        this.difficulty = v;
        return this;
    }

    public SchedulePatch setTaskType(TaskType v) {
        this.taskType = v;
        return this;
    }

    public SchedulePatch setDate(ZonedDateTime v) {
        this.date = v;
        return this;
    }

    public Optional<String> title() {
        return Optional.ofNullable(title);
    }

    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    public Optional<ZonedDateTime> deadline() {
        return Optional.ofNullable(deadline);
    }

    public Optional<Integer> importance() {
        return Optional.ofNullable(importance);
    }

    public Optional<Integer> difficulty() {
        return Optional.ofNullable(difficulty);
    }

    public Optional<TaskType> taskType() {
        return Optional.ofNullable(taskType);
    }

    public Optional<ZonedDateTime> date() {
        return Optional.ofNullable(date);
    }

    public boolean isEmpty() {
        return title == null && description == null && deadline == null
                && importance == null && difficulty == null && taskType == null && date == null;
    }
}


