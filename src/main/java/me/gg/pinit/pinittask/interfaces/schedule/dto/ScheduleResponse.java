package me.gg.pinit.pinittask.interfaces.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.domain.schedule.vo.ScheduleHistory;
import me.gg.pinit.pinittask.domain.task.model.Task;
import me.gg.pinit.pinittask.domain.task.vo.ImportanceConstraint;
import me.gg.pinit.pinittask.domain.task.vo.TemporalConstraint;
import me.gg.pinit.pinittask.interfaces.dto.DateTimeWithZone;

import java.time.Duration;
import java.time.ZoneId;
import java.util.Objects;

public record ScheduleResponse(
        @Schema(description = "일정 ID", example = "10")
        Long id,
        @Schema(description = "회원 ID", example = "3")
        Long ownerId,
        @Schema(description = "관련 작업 ID", example = "7")
        Long taskId,
        @Schema(description = "일정 제목")
        String title,
        @Schema(description = "일정 설명")
        String description,
        @Schema(description = "일정 시작 예정 시각")
        DateTimeWithZone date,
        @Schema(description = "일정 유형")
        String scheduleType,
        @Schema(description = "마감 기한 (V0 legacy)", deprecated = true)
        DateTimeWithZone deadline,
        @Schema(description = "중요도 (V0 legacy)", deprecated = true)
        Integer importance,
        @Schema(description = "난이도 (V0 legacy)", deprecated = true)
        Integer difficulty,
        @Schema(description = "누적 작업 시간")
        Duration duration,
        @Schema(description = "현재 상태")
        String state
) {
    public static ScheduleResponse from(Schedule schedule, Task task) {
        return from(schedule, task, schedule.getDesignatedStartTime().getZone());
    }

    public static ScheduleResponse from(Schedule schedule, Task task, ZoneId viewZoneId) {
        Objects.requireNonNull(viewZoneId, "viewZoneId must not be null");
        TemporalConstraint temporal = task == null ? null : task.getTemporalConstraint();
        ImportanceConstraint importanceConstraint = task == null ? null : task.getImportanceConstraint();
        ScheduleHistory history = schedule.getHistory();
        return new ScheduleResponse(
                schedule.getId(),
                schedule.getOwnerId(),
                task == null ? null : task.getId(),
                schedule.getTitle(),
                schedule.getDescription(),
                DateTimeWithZone.from(schedule.getDesignatedStartTime().withZoneSameInstant(viewZoneId)),
                schedule.getScheduleType().name(),
                temporal == null ? null : DateTimeWithZone.from(temporal.getDeadline()),
                importanceConstraint == null ? null : importanceConstraint.getImportance(),
                importanceConstraint == null ? null : importanceConstraint.getDifficulty(),
                history.getElapsedTime(),
                schedule.getState()
        );
    }

    public static ScheduleResponse from(Schedule schedule) {
        return from(schedule, null);
    }
}
