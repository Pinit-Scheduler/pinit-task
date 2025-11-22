package me.gg.pinit.pinittask.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record DependencyRequest(
        @NotNull
        @Schema(description = "선행 일정 ID", example = "1")
        Long fromId,
        @NotNull
        @Schema(description = "후행 일정 ID", example = "2")
        Long toId
) {
}
