package me.gg.pinit.pinittask.domain.schedule.model;

import me.gg.pinit.pinittask.domain.events.DomainEvent;
import me.gg.pinit.pinittask.domain.events.DomainEvents;
import me.gg.pinit.pinittask.domain.schedule.event.ScheduleCompletedEvent;
import me.gg.pinit.pinittask.domain.schedule.exception.IllegalTransitionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Deque;

import static me.gg.pinit.pinittask.domain.schedule.model.ScheduleUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InProgressStateTest {

    @AfterEach
    void tearDown() {
        DomainEvents.getEventsAndClear();
    }

    @Test
    void start() {
        //given
        Schedule schedule = getInProgressSchedule();

        //when, then
        assertThatThrownBy(() -> schedule.start(RESTART_TIME))
                .isInstanceOf(IllegalTransitionException.class)
                .hasMessage("진행 중인 일정을 다시 시작할 수 없습니다.");

    }

    @Test
    void suspend() {
        //given
        Schedule schedule = getInProgressSchedule();

        //when
        schedule.suspend(SUSPEND_TIME);

        //then
        assertThat(schedule.isSuspended()).isTrue();
        assertThat(schedule.getHistory().getElapsedTime()).isEqualTo(Duration.between(START_TIME, SUSPEND_TIME));
        assertThat(schedule.getHistory().getStartTime()).isNull();
    }

    @Test
    void cancel() {
        //given
        Schedule schedule = getInProgressSchedule();

        //when
        schedule.cancel();

        //then
        assertThat(schedule.isNotStarted()).isTrue();
        assertThat(schedule.getHistory().getElapsedTime()).isEqualTo(Duration.ZERO);
        assertThat(schedule.getHistory().getStartTime()).isNull();
    }

    @Test
    void finish() {
        //given
        Schedule schedule = getInProgressSchedule();

        //when
        schedule.finish(FINISH_TIME);

        //then
        Deque<DomainEvent> eventsAndClear = DomainEvents.getEventsAndClear();
        assertThat(eventsAndClear.size()).isEqualTo(1);

        DomainEvent event = eventsAndClear.peek();
        assertThat(event).isInstanceOf(ScheduleCompletedEvent.class);

        if (event instanceof ScheduleCompletedEvent completedEvent) {
            assertThat(completedEvent.getDuration()).isEqualTo(Duration.between(START_TIME, FINISH_TIME));
            assertThat(completedEvent.getOwnerId()).isEqualTo(schedule.getOwnerId());
            assertThat(completedEvent.getTaskType()).isEqualTo(schedule.getTemporalConstraint().getTaskType());
        }

        assertThat(schedule.isCompleted()).isTrue();
        assertThat(schedule.getHistory().getElapsedTime()).isEqualTo(Duration.between(START_TIME, FINISH_TIME));
        assertThat(schedule.getHistory().getStartTime()).isNull();
    }
}