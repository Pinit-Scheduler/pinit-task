package me.gg.pinit.pinittask.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.domain.schedule.vo.ImportanceConstraint;
import me.gg.pinit.pinittask.domain.schedule.vo.ScheduleHistory;
import me.gg.pinit.pinittask.domain.schedule.vo.TemporalConstraint;

import java.time.Duration;

public record ScheduleResponse(
        @Schema(description = "일정 ID", example = "10")
        Long id,
        @Schema(description = "회원 ID", example = "3")
        Long ownerId,
        @Schema(description = "일정 제목")
        String title,
        @Schema(description = "일정 설명")
        String description,
        @Schema(description = "일정 시작 예정 시각")
        DateTimeWithZone date,
        @Schema(description = "마감 기한")
        DateTimeWithZone deadline,
        @Schema(description = "중요도")
        int importance,
        @Schema(description = "긴급도")
        int urgency,
        @Schema(description = "")
        Duration duration,
        @Schema(description = "현재 상태")
        String state
) {
    public static ScheduleResponse from(Schedule schedule) {
        TemporalConstraint temporal = schedule.getTemporalConstraint();
        ImportanceConstraint importanceConstraint = schedule.getImportanceConstraint();
        ScheduleHistory history = schedule.getHistory();
        return new ScheduleResponse(
                schedule.getId(),
                schedule.getOwnerId(),
                schedule.getTitle(),
                schedule.getDescription(),
                DateTimeWithZone.from(schedule.getDesignatedStartTime()),
                DateTimeWithZone.from(temporal.getDeadline()),
                importanceConstraint.getImportance(),
                importanceConstraint.getUrgency(),
                history.getElapsedTime(),
                schedule.getState()
        );
    }
}
