package me.gg.pinit.pinittask.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import me.gg.pinit.pinittask.application.datetime.DateTimeUtils;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;

public record ScheduleSimpleRequest(
        @NotBlank
        @Schema(description = "일정 제목", example = "팀 회의")
        String title,
        @NotBlank
        @Schema(description = "일정 설명", example = "분기 OKR 리뷰")
        String description,
        @NotNull
        @Valid
        @Schema(description = "일정 시작 시각", example = "{\"dateTime\":\"2024-02-28T09:00:00\",\"zoneId\":\"Asia/Seoul\"}")
        DateTimeWithZone date
) {
    public Schedule toSchedule(Long ownerId, DateTimeUtils dateTimeUtils) {
        return new Schedule(
                ownerId,
                null,
                title,
                description,
                dateTimeUtils.toZonedDateTime(this.date.dateTime(), this.date.zoneId())
        );
    }
}
