package me.gg.pinit.pinittask.interfaces.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import me.gg.pinit.pinittask.application.datetime.DateTimeUtils;
import me.gg.pinit.pinittask.application.schedule.dto.DependencyDto;
import me.gg.pinit.pinittask.application.task.dto.TaskDependencyAdjustCommand;
import me.gg.pinit.pinittask.interfaces.dto.DateTimeWithZone;
import me.gg.pinit.pinittask.interfaces.utils.FibonacciDifficulty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record TaskCreateRequest(
        @NotBlank
        @Schema(description = "작업 제목", example = "스터디 준비")
        String title,
        @NotBlank
        @Schema(description = "작업 설명", example = "다음 주 발표 자료 정리")
        String description,
        @NotNull
        @Schema(description = "마감 기한", example = "{\"dateTime\":\"2024-03-01T18:00:00\",\"zoneId\":\"Asia/Seoul\"}")
        @Valid
        DateTimeWithZone dueDate,
        @NotNull
        @Min(1)
        @Max(9)
        @Schema(description = "중요도 (1~9)", example = "5")
        Integer importance,
        @NotNull
        @FibonacciDifficulty
        @Schema(description = "난이도 (피보나치 수: 1,2,3,5,8,13,21)", example = "5")
        Integer difficulty,
        @Schema(description = "추가할 의존 관계 목록 (생성 시 각 항목에 fromId 또는 toId 중 하나는 0)")
        List<@Valid DependencyRequest> addDependencies
) {
    public TaskDependencyAdjustCommand toCommand(Long taskId, Long ownerId, DateTimeUtils dateTimeUtils) {
        validateMustContainSelfPlaceholder(addDependencies);
        List<DependencyDto> remove = List.of(); // 생성 시 remove는 허용하지 않음
        List<DependencyDto> add = toDependencyDtos(addDependencies);
        return new TaskDependencyAdjustCommand(
                taskId,
                ownerId,
                title,
                description,
                dateTimeUtils.toZonedDateTime(dueDate.dateTime(), dueDate.zoneId()),
                importance,
                difficulty,
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

    private void validateMustContainSelfPlaceholder(List<DependencyRequest> dependencies) {
        Optional.ofNullable(dependencies)
                .orElseGet(ArrayList::new)
                .forEach(dep -> {
                    if (dep.fromId() != 0L && dep.toId() != 0L) {
                        throw new IllegalArgumentException("작업 생성 시 의존 관계에는 fromId 또는 toId 중 하나가 0이어야 합니다.");
                    }
                });
    }
}

