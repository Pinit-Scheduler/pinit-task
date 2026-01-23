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

public record TaskUpdateRequestV2(
        @NotBlank
        @Schema(description = "작업 제목", example = "스터디 준비")
        String title,
        @NotBlank
        @Schema(description = "작업 설명", example = "다음 주 발표 자료 정리")
        String description,
        @NotNull
        @Schema(description = "마감 날짜(+오프셋)", example = "{\"date\":\"2024-03-01\",\"offset\":\"+09:00\"}")
        @Valid
        DateWithOffset dueDate,
        @NotNull
        @Min(1)
        @Max(9)
        @Schema(description = "중요도 (1~9)", example = "5")
        Integer importance,
        @NotNull
        @FibonacciDifficulty
        @Schema(description = "난이도 (피보나치 수: 1,2,3,5,8,13,21)", example = "5")
        Integer difficulty,
        @Schema(description = "제거할 의존 관계 목록 (수정 시 0 사용 금지)")
        List<@Valid DependencyRequest> removeDependencies,
        @Schema(description = "추가할 의존 관계 목록 (수정 시 0 사용 금지)")
        List<@Valid DependencyRequest> addDependencies
) {
    public TaskDependencyAdjustCommand toCommand(Long taskId, Long ownerId, DateTimeUtils dateTimeUtils) {
        validateNoPlaceholder(removeDependencies);
        validateNoPlaceholder(addDependencies);
        List<DependencyDto> remove = toDependencyDtos(removeDependencies);
        List<DependencyDto> add = toDependencyDtos(addDependencies);
        return new TaskDependencyAdjustCommand(
                taskId,
                ownerId,
                title,
                description,
                dateTimeUtils.toStartOfDay(dueDate.date(), dueDate.offset()),
                importance,
                difficulty,
                remove,
                add
        );
    }

    private void validateNoPlaceholder(List<DependencyRequest> dependencies) {
        Optional.ofNullable(dependencies)
                .orElseGet(ArrayList::new)
                .forEach(dep -> {
                    if (dep.fromId() == 0L || dep.toId() == 0L) {
                        throw new IllegalArgumentException("수정 요청에서는 의존 관계 ID에 0을 사용할 수 없습니다.");
                    }
                });
    }

    private List<DependencyDto> toDependencyDtos(List<DependencyRequest> requests) {
        return Optional.ofNullable(requests)
                .orElseGet(ArrayList::new)
                .stream()
                .map(request -> new DependencyDto(null, request.fromId(), request.toId()))
                .toList();
    }
}
