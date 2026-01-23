package me.gg.pinit.pinittask.interfaces.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import me.gg.pinit.pinittask.application.datetime.DateTimeUtils;
import me.gg.pinit.pinittask.application.schedule.dto.DependencyDto;
import me.gg.pinit.pinittask.application.schedule.dto.ScheduleDependencyAdjustCommand;
import me.gg.pinit.pinittask.domain.schedule.model.ScheduleType;
import me.gg.pinit.pinittask.interfaces.dto.DateTimeWithZone;
import me.gg.pinit.pinittask.interfaces.task.dto.DependencyRequest;
import me.gg.pinit.pinittask.interfaces.utils.FibonacciDifficulty;

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
        @Schema(description = "연결할 기존 작업 ID (선택)", example = "7")
        Long taskId,
        @NotNull
        @Schema(description = "마감 기한 (V0 legacy, V1에서는 사용 안 함)", example = "{\"dateTime\":\"2024-03-01T18:00:00\",\"zoneId\":\"Asia/Seoul\"}", deprecated = true)
        @Valid
        DateTimeWithZone deadline,
        @NotNull
        @Min(1)
        @Max(9)
        @Schema(description = "중요도 (1~9) - V0 legacy", example = "5", deprecated = true)
        Integer importance,
        @NotNull
        @FibonacciDifficulty
        @Schema(description = "난이도 (피보나치 수: 1,2,3,5,8,13,21) - V0 legacy", example = "3", deprecated = true)
        Integer difficulty,
        @NotNull
        @Schema(description = "일정 유형", example = "DEEP_WORK")
        ScheduleType scheduleType,
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
                taskId,
                title,
                description,
                dateTimeUtils.toZonedDateTime(deadline.dateTime(), deadline.zoneId()),
                importance,
                difficulty,
                scheduleType,
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
