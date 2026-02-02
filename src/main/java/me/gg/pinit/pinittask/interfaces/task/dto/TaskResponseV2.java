package me.gg.pinit.pinittask.interfaces.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import me.gg.pinit.pinittask.application.dependency.service.DependencyService;
import me.gg.pinit.pinittask.domain.task.model.Task;
import me.gg.pinit.pinittask.domain.task.vo.ImportanceConstraint;
import me.gg.pinit.pinittask.domain.task.vo.TemporalConstraint;
import me.gg.pinit.pinittask.interfaces.dto.DateWithOffset;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public record TaskResponseV2(
        @Schema(description = "작업 ID", example = "10")
        Long id,
        @Schema(description = "회원 ID", example = "3")
        Long ownerId,
        @Schema(description = "작업 제목")
        String title,
        @Schema(description = "작업 설명")
        String description,
        @Schema(description = "마감 날짜(+IANA 시간대, 00:00 시각). `offset`은 해당 날짜의 오프셋으로 반환됩니다.")
        DateWithOffset dueDate,
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
    public static TaskResponseV2 from(Task task) {
        return from(task, null);
    }

    public static TaskResponseV2 from(Task task, DependencyService.TaskDependencyInfo dependencyInfo) {
        TemporalConstraint temporal = task.getTemporalConstraint();
        ImportanceConstraint importanceConstraint = task.getImportanceConstraint();
        List<Long> previous = dependencyInfo == null ? Collections.emptyList() : dependencyInfo.previousTaskIds();
        List<Long> next = dependencyInfo == null ? Collections.emptyList() : dependencyInfo.nextTaskIds();
        return new TaskResponseV2(
                task.getId(),
                task.getOwnerId(),
                task.getTitle(),
                task.getDescription(),
                DateWithOffset.from(temporal.getDeadline()),
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
