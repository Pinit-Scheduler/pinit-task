package me.gg.pinit.pinittask.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import me.gg.pinit.pinittask.application.datetime.DateTimeUtils;
import me.gg.pinit.pinittask.application.schedule.dto.DependencyDto;
import me.gg.pinit.pinittask.application.task.dto.TaskDependencyAdjustCommand;
import me.gg.pinit.pinittask.interfaces.utils.FibonacciDifficulty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record TaskRequest(
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
        @Schema(description = "제거할 의존 관계 목록")
        List<@Valid DependencyRequest> removeDependencies,
        @Schema(description = "추가할 의존 관계 목록")
        List<@Valid DependencyRequest> addDependencies
) {
    public TaskDependencyAdjustCommand toCommand(Long taskId, Long ownerId, DateTimeUtils dateTimeUtils) {
        List<DependencyDto> remove = toDependencyDtos(removeDependencies);
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
}
