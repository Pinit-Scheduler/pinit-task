package me.gg.pinit.pinittask.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;

public record DateWithOffset(
        @NotNull
        @Schema(description = "날짜", example = "2024-03-01")
        LocalDate date,
        @NotNull
        @Schema(description = "UTC 기준 오프셋(+HH:mm)", example = "+09:00")
        ZoneOffset offset
) {
    public static DateWithOffset from(ZonedDateTime zonedDateTime) {
        Objects.requireNonNull(zonedDateTime, "zonedDateTime must not be null");
        return new DateWithOffset(zonedDateTime.toLocalDate(), zonedDateTime.getOffset());
    }
}
