package me.gg.pinit.pinittask.domain.schedule.model;

import me.gg.pinit.pinittask.domain.events.DomainEvent;
import me.gg.pinit.pinittask.domain.events.DomainEvents;
import me.gg.pinit.pinittask.domain.schedule.event.ScheduleCompletedEvent;
import me.gg.pinit.pinittask.domain.schedule.exception.IllegalTransitionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Deque;

import static me.gg.pinit.pinittask.domain.schedule.model.ScheduleUtils.ENROLLED_TIME;
import static me.gg.pinit.pinittask.domain.schedule.model.ScheduleUtils.getNotStartedSchedule;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotStartedStateTest {

    @AfterEach
    void tearDown() {
        DomainEvents.getEventsAndClear();
    }

    @Test
    void start() {
        //given
        Schedule schedule = getNotStartedSchedule();

        //when
        schedule.start(ENROLLED_TIME.plusHours(1));

        //then
        assertThat(schedule.isInProgress()).isTrue();
        assertThat(schedule.getHistory().getStartTime()).isEqualTo(ENROLLED_TIME.plusHours(1));
    }

    @Test
    void suspend() {
        //given
        Schedule schedule = getNotStartedSchedule();

        //when, then
        assertThatThrownBy(() -> schedule.suspend(ENROLLED_TIME.plusHours(1)))
                .isInstanceOf(IllegalTransitionException.class)
                .hasMessage("시작되지 않은 일정을 일시정지할 수 없습니다.");
    }

    @Test
    void cancel() {
        //given
        Schedule schedule = getNotStartedSchedule();

        //when, then
        assertThatThrownBy(schedule::cancel)
                .isInstanceOf(IllegalTransitionException.class)
                .hasMessage("시작되지 않은 일정을 취소할 수 없습니다.");

    }

    @Test
    void finish() {
        //given
        Schedule schedule = getNotStartedSchedule();

        //when
        schedule.finish(ENROLLED_TIME.plusHours(1));

        //then
        Deque<DomainEvent> eventsAndClear = DomainEvents.getEventsAndClear();
        assertThat(eventsAndClear).hasSize(1);
        DomainEvent event = eventsAndClear.peek();

        if (event instanceof ScheduleCompletedEvent completedEvent) {
            assertThat(completedEvent.getDuration()).isEqualTo(Duration.ZERO);
            assertThat(completedEvent.getOwnerId()).isEqualTo(schedule.getOwnerId());
            assertThat(completedEvent.getTaskType()).isEqualTo(schedule.getTemporalConstraint().getTaskType());
        }

        assertThat(event).isInstanceOf(me.gg.pinit.pinittask.domain.schedule.event.ScheduleCompletedEvent.class);
        assertThat(schedule.isCompleted()).isTrue();
    }
}