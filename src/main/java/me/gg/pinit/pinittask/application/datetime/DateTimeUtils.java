package me.gg.pinit.pinittask.application.datetime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;

@Service
@Slf4j
public class DateTimeUtils {
    public ZonedDateTime lastMondayStart(ZonedDateTime point, ZoneOffset offset) {
        ZonedDateTime converted = point.withZoneSameInstant(offset);
        LocalDate monday = converted.toLocalDate()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return monday.atStartOfDay(offset);
    }

    public ZonedDateTime toZonedDateTime(LocalDateTime localDateTime, ZoneId zoneId) {
        Objects.requireNonNull(localDateTime, "localDateTime must not be null");
        Objects.requireNonNull(zoneId, "zoneId must not be null");
        return ZonedDateTime.of(localDateTime, zoneId);
    }

    public ZonedDateTime toStartOfDay(LocalDate date, ZoneOffset offset) {
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(offset, "offset must not be null");
        return date.atStartOfDay(offset);
    }
}
