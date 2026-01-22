package me.gg.pinit.pinittask.application.statistics.event.handler;

import me.gg.pinit.pinittask.application.schedule.service.ScheduleService;
import me.gg.pinit.pinittask.application.statistics.service.StatisticsService;
import me.gg.pinit.pinittask.domain.schedule.event.ScheduleCompletedEvent;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.domain.schedule.model.ScheduleType;
import me.gg.pinit.pinittask.domain.schedule.vo.ScheduleHistory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.ZonedDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleCompletedEventHandlerTest {
    private final Long scheduleId = 11L;
    private final Long ownerId = 22L;
    @Mock
    StatisticsService statisticsService;
    @Mock
    ScheduleService scheduleService;
    @Mock
    Schedule schedule;
    @InjectMocks
    ScheduleCompletedEventHandler handler;

    @Test
    void addsElapsedTimeForCompletedSchedule() {
        //given
        ZonedDateTime designatedStart = ZonedDateTime.now();
        ScheduleHistory history = new ScheduleHistory(null, Duration.ofMinutes(45));
        ScheduleCompletedEvent event = new ScheduleCompletedEvent(scheduleId, ownerId, "IN_PROGRESS");

        when(scheduleService.getSchedule(ownerId, scheduleId)).thenReturn(schedule);
        when(schedule.getScheduleType()).thenReturn(ScheduleType.DEEP_WORK);
        when(schedule.getHistory()).thenReturn(history);
        when(schedule.getDesignatedStartTime()).thenReturn(designatedStart);

        //when
        handler.on(event);

        //then
        verify(statisticsService).addElapsedTime(ownerId, ScheduleType.DEEP_WORK, history.getElapsedTime(), designatedStart);
    }
}
