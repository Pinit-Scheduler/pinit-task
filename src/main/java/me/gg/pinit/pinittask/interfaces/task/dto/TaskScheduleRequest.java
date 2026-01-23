package me.gg.pinit.pinittask.interfaces.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import me.gg.pinit.pinittask.application.datetime.DateTimeUtils;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.domain.schedule.model.ScheduleType;
import me.gg.pinit.pinittask.domain.task.model.Task;
import me.gg.pinit.pinittask.interfaces.dto.DateTimeWithZone;

public record TaskScheduleRequest(
        @Schema(description = "일정 제목(미입력 시 작업 제목 사용)", example = "오늘 할당된 작업")
        String title,
        @Schema(description = "일정 설명(미입력 시 작업 설명 사용)", example = "할 일 설명")
        String description,
        @NotNull
        @Valid
        @Schema(description = "일정 시작 시각", example = "{\"dateTime\":\"2024-02-28T09:00:00\",\"zoneId\":\"Asia/Seoul\"}")
        DateTimeWithZone date,
        @NotNull
        @Schema(description = "일정 유형", example = "DEEP_WORK")
        ScheduleType scheduleType
) {
    public Schedule toSchedule(Task task, Long ownerId, DateTimeUtils dateTimeUtils) {
        String scheduleTitle = title == null ? task.getTitle() : title;
        String scheduleDescription = description == null ? task.getDescription() : description;
        return new Schedule(
                ownerId,
                task.getId(),
                scheduleTitle,
                scheduleDescription,
                dateTimeUtils.toZonedDateTime(date.dateTime(), date.zoneId()),
                scheduleType
        );
    }
}
