package me.gg.pinit.pinittask.domain.schedule.model;

import me.gg.pinit.pinittask.domain.events.DomainEvent;
import me.gg.pinit.pinittask.domain.events.DomainEvents;
import me.gg.pinit.pinittask.domain.schedule.event.ScheduleCanceledEvent;
import me.gg.pinit.pinittask.domain.schedule.exception.IllegalTransitionException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Deque;

import static me.gg.pinit.pinittask.domain.schedule.model.ScheduleUtils.FINISH_TIME;
import static me.gg.pinit.pinittask.domain.schedule.model.ScheduleUtils.getCompletedSchedule;

class CompletedStateTest {

    @AfterEach
    void tearDown() {
        DomainEvents.getEventsAndClear();
    }

    @Test
    void start() {
        //given
        Schedule schedule = getCompletedSchedule();

        //when, then
        Assertions.assertThatThrownBy(() -> schedule.start(FINISH_TIME.plusHours(3)))
                .isInstanceOf(IllegalTransitionException.class)
                .hasMessage("완료된 일정을 다시 시작할 수 없습니다.");
    }

    @Test
    void suspend() {
        //given
        Schedule schedule = getCompletedSchedule();

        //when, then
        Assertions.assertThatThrownBy(() -> schedule.suspend(FINISH_TIME))
                .isInstanceOf(IllegalTransitionException.class)
                .hasMessage("완료된 일정을 일시정지할 수 없습니다.");

    }

    @Test
    void cancel() {
        //given
        Schedule schedule = getCompletedSchedule();

        //when
        schedule.cancel();

        //then
        Assertions.assertThat(schedule.isNotStarted()).isTrue();

        Deque<DomainEvent> eventsAndClear = DomainEvents.getEventsAndClear();
        Assertions.assertThat(eventsAndClear.size()).isEqualTo(1);

        DomainEvent event = eventsAndClear.pollFirst();
        Assertions.assertThat(event).isInstanceOf(ScheduleCanceledEvent.class);

        if (event instanceof ScheduleCanceledEvent canceledEvent) {
            Assertions.assertThat(canceledEvent.getOwnerId()).isEqualTo(schedule.getOwnerId());
            Assertions.assertThat(canceledEvent.getDuration()).isEqualTo(schedule.getHistory().getElapsedTime());
            Assertions.assertThat(canceledEvent.getTaskType()).isEqualTo(schedule.getTemporalConstraint().getTaskType());
        }
    }

    @Test
    void finish() {
        //given
        Schedule schedule = getCompletedSchedule();

        //when, then
        Assertions.assertThatThrownBy(() -> schedule.finish(FINISH_TIME))
                .isInstanceOf(IllegalTransitionException.class)
                .hasMessage("완료된 일정을 다시 완료할 수 없습니다.");

    }
}