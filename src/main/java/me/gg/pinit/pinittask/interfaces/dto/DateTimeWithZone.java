package me.gg.pinit.pinittask.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

public record DateTimeWithZone(
        @NotNull
        @Schema(description = "사용자 로컬 시각(오프셋 없이). `toISOString()`이 아닌 로컬 기준 문자열을 사용", example = "2026-02-01T10:00:00")
        LocalDateTime dateTime,
        @NotNull
        @Schema(description = "IANA 시간대 ID", example = "Asia/Seoul")
        ZoneId zoneId
) {
    public static DateTimeWithZone from(ZonedDateTime zonedDateTime) {
        Objects.requireNonNull(zonedDateTime, "zonedDateTime must not be null");
        return new DateTimeWithZone(zonedDateTime.toLocalDateTime(), zonedDateTime.getZone());
    }

    public static DateTimeWithZone from(Instant instant, ZoneId zoneId) {
        Objects.requireNonNull(instant, "instant must not be null");
        Objects.requireNonNull(zoneId, "zoneId must not be null");
        return from(ZonedDateTime.ofInstant(instant, zoneId));
    }
}
