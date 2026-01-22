package me.gg.pinit.pinittask.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import me.gg.pinit.pinittask.domain.task.model.Task;
import me.gg.pinit.pinittask.domain.task.vo.ImportanceConstraint;
import me.gg.pinit.pinittask.domain.task.vo.TemporalConstraint;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public record TaskResponse(
        @Schema(description = "작업 ID", example = "10")
        Long id,
        @Schema(description = "회원 ID", example = "3")
        Long ownerId,
        @Schema(description = "작업 제목")
        String title,
        @Schema(description = "작업 설명")
        String description,
        @Schema(description = "마감 기한")
        DateTimeWithZone dueDate,
        @Schema(description = "중요도")
        int importance,
        @Schema(description = "난이도")
        int difficulty,
        @Schema(description = "완료 여부")
        boolean completed,
        @Schema(description = "들어오는 의존 관계 수")
        int inboundDependencyCount,
        @Schema(description = "선행 작업 ID 목록")
        List<Long> previousTaskIds,
        @Schema(description = "후행 작업 ID 목록")
        List<Long> nextTaskIds,
        @Schema(description = "생성 시각")
        Instant createdAt,
        @Schema(description = "수정 시각")
        Instant updatedAt
) {
    public static TaskResponse from(Task task) {
        return from(task, null);
    }

    public static TaskResponse from(Task task, me.gg.pinit.pinittask.application.dependency.service.DependencyService.TaskDependencyInfo dependencyInfo) {
        TemporalConstraint temporal = task.getTemporalConstraint();
        ImportanceConstraint importanceConstraint = task.getImportanceConstraint();
        List<Long> previous = dependencyInfo == null ? Collections.emptyList() : dependencyInfo.previousTaskIds();
        List<Long> next = dependencyInfo == null ? Collections.emptyList() : dependencyInfo.nextTaskIds();
        return new TaskResponse(
                task.getId(),
                task.getOwnerId(),
                task.getTitle(),
                task.getDescription(),
                DateTimeWithZone.from(temporal.getDeadline()),
                importanceConstraint.getImportance(),
                importanceConstraint.getDifficulty(),
                task.isCompleted(),
                task.getInboundDependencyCount(),
                previous,
                next,
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
