package me.gg.pinit.pinittask.application.datetime;

import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;

@Service
public class DateTimeUtils {
    public ZonedDateTime lastMondayStart(ZonedDateTime point) {
        ZoneId zone = point.getZone();
        LocalDate monday = point.toLocalDate()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return monday.atStartOfDay(zone);
    }

    public ZonedDateTime toZonedDateTime(LocalDateTime localDateTime, ZoneId zoneId) {
        Objects.requireNonNull(localDateTime, "localDateTime must not be null");
        Objects.requireNonNull(zoneId, "zoneId must not be null");
        return ZonedDateTime.of(localDateTime, zoneId);
    }
}
