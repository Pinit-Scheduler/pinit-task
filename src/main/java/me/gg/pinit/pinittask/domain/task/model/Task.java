package me.gg.pinit.pinittask.domain.task.model;

import jakarta.persistence.*;
import lombok.Getter;
import me.gg.pinit.pinittask.domain.events.DomainEvents;
import me.gg.pinit.pinittask.domain.task.event.TaskCanceledEvent;
import me.gg.pinit.pinittask.domain.task.event.TaskCompletedEvent;
import me.gg.pinit.pinittask.domain.task.patch.TaskPatch;
import me.gg.pinit.pinittask.domain.task.vo.ImportanceConstraint;
import me.gg.pinit.pinittask.domain.task.vo.TemporalConstraint;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.ZonedDateTime;

@Entity
@Table(indexes = {
        @Index(name = "idx_task_owner_deadline", columnList = "owner_id, deadline_date, task_id")
})
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "task_id")
    @Getter
    private Long id;

    @Version
    private Long version;

    @Getter
    private Long ownerId;

    @Getter
    private String title;
    @Getter
    private String description;

    @Getter
    @Embedded
    private TemporalConstraint temporalConstraint;
    @Getter
    @Embedded
    private ImportanceConstraint importanceConstraint;

    @Getter
    private boolean completed;
    @Getter
    @Column(nullable = false)
    private int inboundDependencyCount;

    @CreationTimestamp
    @Column(updatable = false, columnDefinition = "DATETIME(6)")
    private Instant createdAt;
    @UpdateTimestamp
    @Column(columnDefinition = "DATETIME(6)")
    private Instant updatedAt;

    protected Task() {
    }

    public Task(Long ownerId, String title, String description, TemporalConstraint temporalConstraint, ImportanceConstraint importanceConstraint) {
        this.ownerId = ownerId;
        setTitle(title);
        setDescription(description);
        this.temporalConstraint = temporalConstraint;
        this.importanceConstraint = importanceConstraint;
        this.completed = false;
        this.inboundDependencyCount = 0;
    }

    public ZonedDateTime getDueDate() {
        return temporalConstraint.getDeadline();
    }

    public void patch(TaskPatch patch) {
        patch.title().ifPresent(this::setTitle);
        patch.description().ifPresent(this::setDescription);
        patch.dueDate().ifPresent(this::changeDeadline);
        patch.importance().ifPresent(this::changeImportance);
        patch.difficulty().ifPresent(this::changeDifficulty);
    }

    public void markCompleted() {
        this.completed = true;
        if (this.id != null) {
            DomainEvents.raise(new TaskCompletedEvent(this.id, this.ownerId));
        }
    }

    public void markIncomplete() {
        this.completed = false;
        if (this.id != null) {
            DomainEvents.raise(new TaskCanceledEvent(this.id, this.ownerId));
        }
    }

    public void changeDeadline(ZonedDateTime newDeadline) {
        this.temporalConstraint = this.temporalConstraint.changeDeadline(newDeadline);
    }

    public void changeImportance(int newImportance) {
        this.importanceConstraint = this.importanceConstraint.changeImportance(newImportance);
    }

    public void changeDifficulty(int newDifficulty) {
        this.importanceConstraint = this.importanceConstraint.changeDifficulty(newDifficulty);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void adjustInboundDependencies(int delta) {
        this.inboundDependencyCount += delta;
        if (this.inboundDependencyCount < 0) {
            throw new IllegalStateException("Inbound dependency count cannot be negative");
        }
    }

    private void setTitle(String title) {
        validateTitle(title);
        this.title = title;
    }

    private void setDescription(String description) {
        validateDescription(description);
        this.description = description;
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("제목의 길이는 1자 이상 20자 이하여야 합니다.");
        } else if (title.length() > 20) {
            throw new IllegalArgumentException("제목의 길이는 1자 이상 20자 이하여야 합니다.");
        }
    }

    private void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("설명의 길이는 1자 이상 100자 이하여야 합니다.");
        } else if (description.length() > 100) {
            throw new IllegalArgumentException("설명의 길이는 1자 이상 100자 이하여야 합니다.");
        }
    }
}
