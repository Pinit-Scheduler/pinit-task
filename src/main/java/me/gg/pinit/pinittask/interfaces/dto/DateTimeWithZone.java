package me.gg.pinit.pinittask.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

public record DateTimeWithZone(
        @NotNull
        @Schema(description = "지역 시각", example = "2024-03-01T18:00:00")
        LocalDateTime dateTime,
        @NotNull
        @Schema(description = "시간대 ID", example = "Asia/Seoul")
        ZoneId zoneId
) {
    public static DateTimeWithZone from(ZonedDateTime zonedDateTime) {
        Objects.requireNonNull(zonedDateTime, "zonedDateTime must not be null");
        return new DateTimeWithZone(zonedDateTime.toLocalDateTime(), zonedDateTime.getZone());
    }
}
