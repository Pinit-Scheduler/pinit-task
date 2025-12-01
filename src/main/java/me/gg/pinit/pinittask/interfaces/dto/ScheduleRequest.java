package me.gg.pinit.pinittask.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import me.gg.pinit.pinittask.application.schedule.dto.DependencyDto;
import me.gg.pinit.pinittask.application.datetime.DateTimeUtils;
import me.gg.pinit.pinittask.application.schedule.dto.ScheduleDependencyAdjustCommand;
import me.gg.pinit.pinittask.domain.schedule.model.TaskType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record ScheduleRequest(
        @NotBlank
        @Schema(description = "일정 제목", example = "스터디 준비")
        String title,
        @NotBlank
        @Schema(description = "일정 설명", example = "다음 주 발표 자료 정리")
        String description,
        @NotNull
        @Schema(description = "마감 기한", example = "{\"dateTime\":\"2024-03-01T18:00:00\",\"zoneId\":\"Asia/Seoul\"}")
        @Valid
        DateTimeWithZone deadline,
        @NotNull
        @Min(1)
        @Max(9)
        @Schema(description = "중요도 (1~9)", example = "5")
        Integer importance,
        @NotNull
        @Min(1)
        @Max(9)
        @Schema(description = "긴급도 (1~9)", example = "7")
        Integer urgency,
        @NotNull
        @Schema(description = "작업 유형", example = "TASK")
        TaskType taskType,
        @NotNull
        @Schema(description = "일정 시작 예정 시각", example = "{\"dateTime\":\"2024-02-28T09:00:00\",\"zoneId\":\"Asia/Seoul\"}")
        @Valid
        DateTimeWithZone date,
        @Schema(description = "제거할 의존 관계 목록")
        List<@Valid DependencyRequest> removeDependencies,
        @Schema(description = "추가할 의존 관계 목록")
        List<@Valid DependencyRequest> addDependencies
) {
    public ScheduleDependencyAdjustCommand toCommand(Long scheduleId, Long ownerId, DateTimeUtils dateTimeUtils) {
        List<DependencyDto> remove = toDependencyDtos(removeDependencies);
        List<DependencyDto> add = toDependencyDtos(addDependencies);
        return new ScheduleDependencyAdjustCommand(
                scheduleId,
                ownerId,
                title,
                description,
                dateTimeUtils.toZonedDateTime(deadline.dateTime(), deadline.zoneId()),
                importance,
                urgency,
                taskType,
                dateTimeUtils.toZonedDateTime(date.dateTime(), date.zoneId()),
                remove,
                add
        );
    }

    private List<DependencyDto> toDependencyDtos(List<DependencyRequest> requests) {
        return Optional.ofNullable(requests)
                .orElseGet(ArrayList::new)
                .stream()
                .map(request -> new DependencyDto(null, request.fromId(), request.toId()))
                .toList();
    }
}
