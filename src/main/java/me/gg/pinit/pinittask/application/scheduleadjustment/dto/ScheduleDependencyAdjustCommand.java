package me.gg.pinit.pinittask.application.scheduleadjustment.dto;

import lombok.Getter;
import me.gg.pinit.pinittask.domain.dependency.model.Dependency;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.domain.schedule.model.TaskType;
import me.gg.pinit.pinittask.domain.schedule.patch.SchedulePatch;
import me.gg.pinit.pinittask.domain.schedule.vo.ImportanceConstraint;
import me.gg.pinit.pinittask.domain.schedule.vo.TemporalConstraint;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

public class ScheduleDependencyAdjustCommand {
    private Long scheduleId;
    @Getter
    private Long ownerId;
    @Getter
    private String title;
    @Getter
    private String description;
    @Getter
    private ZonedDateTime deadline;
    @Getter
    private Integer importance;
    @Getter
    private Integer urgency;
    @Getter
    private TaskType taskType;
    @Getter
    private ZonedDateTime date;
    private List<DependencyDto> removeDependencies;
    private List<DependencyDto> addDependencies;

    public ScheduleDependencyAdjustCommand(Long scheduleId, Long ownerId, String title, String description, ZonedDateTime deadline, Integer importance, Integer urgency, TaskType taskType, ZonedDateTime date, List<DependencyDto> removeDependencies, List<DependencyDto> addDependencies) {
        this.scheduleId = scheduleId;
        this.ownerId = ownerId;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.importance = importance;
        this.urgency = urgency;
        this.taskType = taskType;
        this.date = date;
        this.removeDependencies = removeDependencies;
        this.addDependencies = addDependencies;
    }

    public Long getScheduleId() {
        return Objects.requireNonNullElse(scheduleId, 0L);
    }

    public Schedule getTemporalSchedule() {
        return new Schedule(
                ownerId,
                title,
                description,
                date,
                new TemporalConstraint(deadline, Duration.ZERO, taskType),
                new ImportanceConstraint(importance, urgency)
        );
    }

    public SchedulePatch getSchedulePatch() {
        return new SchedulePatch()
                .setTitle(title)
                .setDescription(description)
                .setDeadline(deadline)
                .setImportance(importance)
                .setUrgency(urgency)
                .setTaskType(taskType)
                .setDate(date);
    }

    public List<Dependency> getRemoveDependencies() {
        return removeDependencies.stream().map(d -> new Dependency(d.getFromId(), d.getToId())).toList();
    }

    public List<Dependency> getAddDependencies() {
        return addDependencies.stream().map(d -> new Dependency(d.getFromId(), d.getToId())).toList();
    }
}
