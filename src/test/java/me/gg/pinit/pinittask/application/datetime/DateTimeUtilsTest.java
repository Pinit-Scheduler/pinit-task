package me.gg.pinit.pinittask.application.datetime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

class DateTimeUtilsTest {

    @Test
    void lastMondayStart() {
        DateTimeUtils dateTimeUtils = new DateTimeUtils();
        ZonedDateTime zonedDateTime = dateTimeUtils.lastMondayStart(ZonedDateTime.of(LocalDateTime.of(2025, 11, 19, 10, 30, 0), ZoneId.of("Asia/Seoul")), ZoneOffset.of("+09:00"));

        Assertions.assertThat(zonedDateTime).isNotNull();
        Assertions.assertThat(zonedDateTime.getYear()).isEqualTo(2025);
        Assertions.assertThat(zonedDateTime.getMonthValue()).isEqualTo(11);
        Assertions.assertThat(zonedDateTime.getDayOfMonth()).isEqualTo(17);
        Assertions.assertThat(zonedDateTime.getHour()).isEqualTo(0);
        Assertions.assertThat(zonedDateTime.getMinute()).isEqualTo(0);
        Assertions.assertThat(zonedDateTime.getSecond()).isEqualTo(0);
        Assertions.assertThat(zonedDateTime.getZone()).isEqualTo(ZoneId.of("+09:00"));
    }
}