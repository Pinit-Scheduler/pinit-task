package me.gg.pinit.pinittask.application.datetime;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;

@Service
public class DateTimeUtils {
    public ZonedDateTime lastMondayStart(ZonedDateTime point) {
        ZoneId zone = point.getZone();
        LocalDate monday = point.toLocalDate()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return monday.atStartOfDay(zone);
    }
}
