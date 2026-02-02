package me.gg.pinit.pinittask.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;

public record DateWithOffset(
        @NotNull
        @Schema(description = "날짜", example = "2024-03-01")
        LocalDate date,
        @Schema(description = "UTC 기준 오프셋(+HH:mm). IANA `zoneId`로 계산된 값을 명시하고 싶을 때 선택적으로 포함", example = "+09:00")
        ZoneOffset offset,
        @Schema(description = "IANA 시간대 ID (필수)", example = "Asia/Seoul")
        ZoneId zoneId
) {
    public DateWithOffset {
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(zoneId, "zoneId must not be null");
    }

    public DateWithOffset(LocalDate date, ZoneId zoneId) {
        this(date, null, zoneId);
    }

    public static DateWithOffset from(ZonedDateTime zonedDateTime) {
        Objects.requireNonNull(zonedDateTime, "zonedDateTime must not be null");
        return new DateWithOffset(zonedDateTime.toLocalDate(), zonedDateTime.getOffset(), zonedDateTime.getZone());
    }

    public ZoneId resolveZoneId(ZoneId fallbackZoneId) {
        Objects.requireNonNull(fallbackZoneId, "fallbackZoneId must not be null");
        return zoneId == null ? fallbackZoneId : zoneId;
    }
}
