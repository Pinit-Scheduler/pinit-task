package me.gg.pinit.pinittask.domain.datetime;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ZonedDateAttributeTest {

    @Test
    void preservesZoneIdAndComputesOffsetFromRules() {
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        LocalDate date = LocalDate.of(2026, 2, 1);

        ZonedDateAttribute attribute = ZonedDateAttribute.of(date, zoneId);

        ZonedDateTime zoned = attribute.toZonedDateTime();
        assertThat(zoned.getZone()).isEqualTo(zoneId);
        assertThat(attribute.getOffset()).isEqualTo(ZoneOffset.of("+09:00"));
    }

    @Test
    void recalculatesOffsetForDstDates() {
        ZoneId zoneId = ZoneId.of("America/New_York");

        ZonedDateAttribute winter = ZonedDateAttribute.of(LocalDate.of(2026, 3, 8), zoneId); // DST switch day
        ZonedDateAttribute summer = ZonedDateAttribute.of(LocalDate.of(2026, 7, 1), zoneId);

        assertThat(winter.getOffset()).isEqualTo(ZoneOffset.of("-05:00"));
        assertThat(summer.getOffset()).isEqualTo(ZoneOffset.of("-04:00"));
        assertThat(winter.getZoneId()).isEqualTo(zoneId);
        assertThat(summer.getZoneId()).isEqualTo(zoneId);
    }
}
