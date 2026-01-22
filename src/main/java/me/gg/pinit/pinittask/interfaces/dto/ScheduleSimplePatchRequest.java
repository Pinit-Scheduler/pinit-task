package me.gg.pinit.pinittask.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import me.gg.pinit.pinittask.application.datetime.DateTimeUtils;
import me.gg.pinit.pinittask.domain.schedule.model.ScheduleType;
import me.gg.pinit.pinittask.domain.schedule.patch.SchedulePatch;

public record ScheduleSimplePatchRequest(
        @Schema(description = "일정 제목", example = "팀 회의")
        String title,
        @Schema(description = "일정 설명", example = "분기 OKR 리뷰")
        String description,
        @Valid
        @Schema(description = "일정 시작 시각", example = "{\"dateTime\":\"2024-02-28T09:00:00\",\"zoneId\":\"Asia/Seoul\"}")
        DateTimeWithZone date,
        @Schema(description = "일정 유형", example = "DEEP_WORK")
        ScheduleType scheduleType
) {
    public SchedulePatch toPatch(DateTimeUtils dateTimeUtils) {
        SchedulePatch patch = new SchedulePatch();
        patch.setTitle(title);
        patch.setDescription(description);
        if (date != null) {
            patch.setDesignatedStartTime(dateTimeUtils.toZonedDateTime(this.date.dateTime(), this.date.zoneId()));
        }
        patch.setScheduleType(scheduleType);
        return patch;
    }
}
