package me.gg.pinit.pinittask.domain.schedule.vo;

import me.gg.pinit.pinittask.domain.schedule.exception.StartNotRecordedException;
import me.gg.pinit.pinittask.domain.schedule.exception.TimeOrderReversedException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ScheduleHistoryTest {

    @Test
    void recordStart() {
        //given
        ScheduleHistory scheduleHistory = new ScheduleHistory(null, Duration.ZERO);
        ZonedDateTime startTime = ZonedDateTime.of(2025, 10, 1, 10, 0, 0, 0, Clock.systemDefaultZone().getZone());

        //when
        scheduleHistory = scheduleHistory.recordStart(startTime);

        //then
        assertThat(scheduleHistory.getStartTime()).isEqualTo(startTime);
    }
    @Test
    void recordStart_시작시간_null(){
        //given
        ScheduleHistory scheduleHistory = new ScheduleHistory(null, Duration.ZERO);
        ZonedDateTime startTime = null;

        //when, then
        assertThatThrownBy(() -> scheduleHistory.recordStart(startTime))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void recordStop() {
        //given
        ScheduleHistory scheduleHistory = new ScheduleHistory(null, Duration.ZERO);
        scheduleHistory = scheduleHistory.recordStart(ZonedDateTime.of(2025, 10, 1, 10, 0, 0, 0, Clock.systemDefaultZone().getZone()));
        //when
        scheduleHistory = scheduleHistory.recordStop(ZonedDateTime.of(2025, 10, 1, 12, 0, 0, 0, Clock.systemDefaultZone().getZone()));

        //then
        assertThat(scheduleHistory.getElapsedTime()).isEqualTo(Duration.ofHours(2));
        assertThat(scheduleHistory.getStartTime()).isNull();
    }

    @Test
    void recordStop_기존_시간_null(){
        //given
        ScheduleHistory scheduleHistory = new ScheduleHistory(null, Duration.ZERO);

        //when, then
        assertThatThrownBy(() -> scheduleHistory.recordStop(ZonedDateTime.of(2025, 10, 1, 10, 0, 0, 0, Clock.systemDefaultZone().getZone())))
                .isInstanceOf(StartNotRecordedException.class)
                .hasMessageContaining("시작 시간이 기록되지 않았습니다.");
    }

    @Test
    void recordStop_시작_시간보다_종료_시간이_앞선_경우(){
        //given
        ScheduleHistory scheduleHistory = new ScheduleHistory(null, Duration.ZERO);
        ScheduleHistory recorded = scheduleHistory.recordStart(ZonedDateTime.of(2025, 10, 1, 10, 0, 0, 0, Clock.systemDefaultZone().getZone()));

        //when, then
        assertThatThrownBy(() -> recorded.recordStop(ZonedDateTime.of(2025, 10, 1, 9, 0, 0, 0, Clock.systemDefaultZone().getZone())))
                .isInstanceOf(TimeOrderReversedException.class)
                .hasMessageContaining("종료 시간은 시작 시간 이후여야 합니다.");
    }

    @Test
    void zero() {
        //given
        ScheduleHistory zeroHistory = ScheduleHistory.zero();
        //when
        ScheduleHistory expectedHistory = new ScheduleHistory(null, Duration.ZERO);
        //then
        assertThat(zeroHistory).isEqualTo(expectedHistory);
    }
}