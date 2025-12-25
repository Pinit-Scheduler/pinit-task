package me.gg.pinit.pinittask.application.statistics.service;

import me.gg.pinit.pinittask.application.datetime.DateTimeUtils;
import me.gg.pinit.pinittask.application.member.service.MemberService;
import me.gg.pinit.pinittask.domain.schedule.model.TaskType;
import me.gg.pinit.pinittask.domain.statistics.model.Statistics;
import me.gg.pinit.pinittask.domain.statistics.repository.StatisticsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    private final Long memberId = 1L;
    private final ZoneId zone = ZoneId.of("UTC");
    @Mock
    StatisticsRepository statisticsRepository;
    @Mock
    DateTimeUtils dateTimeUtils;
    @Mock
    MemberService memberService;
    @InjectMocks
    StatisticsService statisticsService;

    @Test
    void getStatistics() {
        //given
        ZonedDateTime now = ZonedDateTime.now(zone);
        ZonedDateTime mondayStart = now.minusDays(2).withHour(0).withMinute(0).withSecond(0).withNano(0);
        Statistics existing = new Statistics(memberId, mondayStart);
        when(memberService.findZoneOffsetOfMember(memberId)).thenReturn(ZoneOffset.UTC);
        when(dateTimeUtils.lastMondayStart(now, ZoneOffset.UTC)).thenReturn(mondayStart);
        when(statisticsRepository.findByMemberIdAndStartOfWeekDate(memberId, mondayStart.toLocalDate(), mondayStart.getZone().getId())).thenReturn(Optional.of(existing));

        //when
        Statistics result = statisticsService.getStatistics(memberId, now);

        //then
        assertThat(result).isSameAs(existing);
        verify(statisticsRepository).findByMemberIdAndStartOfWeekDate(memberId, mondayStart.toLocalDate(), mondayStart.getZone().getId());
    }

    @Test
    void removeElapsedTime() {
        //given
        ZonedDateTime startTime = ZonedDateTime.now(zone);
        ZonedDateTime mondayStart = startTime.minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        Statistics stats = new Statistics(memberId, mondayStart);
        stats.addDeepWorkDuration(Duration.ofMinutes(40));
        Duration rollback = Duration.ofMinutes(15);
        when(memberService.findZoneOffsetOfMember(memberId)).thenReturn(ZoneOffset.UTC);
        when(dateTimeUtils.lastMondayStart(startTime, ZoneOffset.UTC)).thenReturn(mondayStart);
        when(statisticsRepository.findByMemberIdAndStartOfWeekDate(memberId, mondayStart.toLocalDate(), mondayStart.getZone().getId())).thenReturn(Optional.of(stats));

        //when
        statisticsService.removeElapsedTime(memberId, TaskType.DEEP_WORK, rollback, startTime);

        //then
        assertThat(stats.getDeepWorkElapsedTime()).isEqualTo(Duration.ofMinutes(25));
        assertThat(stats.getTotalWorkElapsedTime()).isEqualTo(Duration.ofMinutes(25));
        verify(statisticsRepository).save(stats);
    }

    @Test
    void addElapsedTime() {
        //given
        ZonedDateTime startTime = ZonedDateTime.now(zone);
        ZonedDateTime mondayStart = startTime.minusDays(4).withHour(0).withMinute(0).withSecond(0).withNano(0);
        Duration duration = Duration.ofMinutes(45);
        when(memberService.findZoneOffsetOfMember(memberId)).thenReturn(ZoneOffset.UTC);
        when(dateTimeUtils.lastMondayStart(startTime, ZoneOffset.UTC)).thenReturn(mondayStart);
        when(statisticsRepository.findByMemberIdAndStartOfWeekDate(memberId, mondayStart.toLocalDate(), mondayStart.getZone().getId())).thenReturn(Optional.empty());

        //when
        statisticsService.addElapsedTime(memberId, TaskType.ADMIN_TASK, duration, startTime);

        //then
        verify(statisticsRepository).save(argThat(saved -> {
            assertThat(saved.getAdminWorkElapsedTime()).isEqualTo(duration);
            assertThat(saved.getDeepWorkElapsedTime()).isEqualTo(Duration.ZERO);
            assertThat(saved.getTotalWorkElapsedTime()).isEqualTo(duration);
            return true;
        }));
    }
}
