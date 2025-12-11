package me.gg.pinit.pinittask.domain.schedule.model;

import me.gg.pinit.pinittask.domain.events.DomainEvent;
import me.gg.pinit.pinittask.domain.events.DomainEvents;
import me.gg.pinit.pinittask.domain.schedule.exception.IllegalTransitionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Deque;

import static me.gg.pinit.pinittask.domain.schedule.model.ScheduleUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SuspendedStateTest {

    @AfterEach
    void tearDown() {
        DomainEvents.getEventsAndClear();
    }

    @Test
    void start() {
        //given
        Schedule schedule = getSuspendedSchedule();

        //when
        schedule.start(RESTART_TIME);

        //then
        Deque<DomainEvent> eventsAndClear = DomainEvents.getEventsAndClear();
        assertThat(eventsAndClear).hasSize(1);
        assertThat(schedule.isInProgress()).isTrue();
        assertThat(schedule.getHistory().getStartTime()).isEqualTo(RESTART_TIME);
    }

    @Test
    void suspend() {
        //given
        Schedule schedule = getSuspendedSchedule();

        //when, then
        assertThatThrownBy(() -> schedule.suspend(SUSPEND_TIME))
                .isInstanceOf(IllegalTransitionException.class)
                .hasMessage("일시정지된 일정을 다시 일시정지할 수 없습니다.");
    }

    @Test
    void cancel() {
        //given
        Schedule schedule = getSuspendedSchedule();

        //when
        schedule.cancel();

        //then
        Deque<DomainEvent> eventsAndClear = DomainEvents.getEventsAndClear();
        assertThat(eventsAndClear).hasSize(1);
        assertThat(schedule.isNotStarted()).isTrue();
        assertThat(schedule.getHistory().getElapsedTime()).isEqualTo(Duration.between(START_TIME, SUSPEND_TIME));
    }

    @Test
    void finish() {
        //given
        Schedule schedule = getSuspendedSchedule();

        //when, then
        assertThatThrownBy(() -> schedule.finish(FINISH_TIME))
                .isInstanceOf(IllegalTransitionException.class)
                .hasMessage("일시정지된 일정을 즉시 완료할 수 없습니다.");
    }
}