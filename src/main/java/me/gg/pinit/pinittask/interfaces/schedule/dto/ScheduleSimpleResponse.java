package me.gg.pinit.pinittask.interfaces.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.domain.schedule.vo.ScheduleHistory;
import me.gg.pinit.pinittask.domain.task.model.Task;
import me.gg.pinit.pinittask.interfaces.dto.DateTimeWithZone;

import java.time.Duration;
import java.time.Instant;

public record ScheduleSimpleResponse(
        @Schema(description = "일정 ID", example = "10")
        Long id,
        @Schema(description = "회원 ID", example = "3")
        Long ownerId,
        @Schema(description = "연결된 작업 ID", example = "7")
        Long taskId,
        @Schema(description = "연결된 작업 완료 여부", example = "true")
        Boolean taskCompleted,
        @Schema(description = "일정 제목")
        String title,
        @Schema(description = "일정 설명")
        String description,
        @Schema(description = "일정 시작 예정 시각")
        DateTimeWithZone date,
        @Schema(description = "일정 유형")
        String scheduleType,
        @Schema(description = "누적 작업 시간")
        Duration duration,
        @Schema(description = "현재 상태")
        String state,
        @Schema(description = "생성 시각")
        Instant createdAt,
        @Schema(description = "수정 시각")
        Instant updatedAt
) {
    public static ScheduleSimpleResponse from(Schedule schedule) {
        return from(schedule, null);
    }

    public static ScheduleSimpleResponse from(Schedule schedule, Task task) {
        ScheduleHistory history = schedule.getHistory();
        return new ScheduleSimpleResponse(
                schedule.getId(),
                schedule.getOwnerId(),
                schedule.getTaskId(),
                task == null ? null : task.isCompleted(),
                schedule.getTitle(),
                schedule.getDescription(),
                DateTimeWithZone.from(schedule.getDesignatedStartTime()),
                schedule.getScheduleType().name(),
                history.getElapsedTime(),
                schedule.getState(),
                schedule.getCreatedAt(),
                schedule.getUpdatedAt()
        );
    }
}

