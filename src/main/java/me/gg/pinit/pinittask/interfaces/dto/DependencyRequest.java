package me.gg.pinit.pinittask.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record DependencyRequest(
        @NotNull
        @Schema(description = "선행 작업 ID (새 작업 생성 시 자기 자신은 0, 수정 시 0 금지)", example = "1")
        Long fromId,
        @NotNull
        @Schema(description = "후행 작업 ID (새 작업 생성 시 자기 자신은 0, 수정 시 0 금지)", example = "2")
        Long toId
) {
}
