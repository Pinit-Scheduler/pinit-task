package me.gg.pinit.pinittask.infrastructure.events.schedule.handler;

import me.gg.pinit.pinittask.domain.schedule.event.ScheduleCanceledEvent;
import me.gg.pinit.pinittask.domain.schedule.event.ScheduleCompletedEvent;
import me.gg.pinit.pinittask.domain.schedule.event.ScheduleStartedEvent;
import me.gg.pinit.pinittask.infrastructure.events.RabbitEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScheduleIntegratedEventHandlerTest {

    @Mock
    RabbitEventPublisher rabbitEventPublisher;

    @Test
    void startedHandler_publishesEvent() {
        ScheduleStartedIntegratedEventHandler handler = new ScheduleStartedIntegratedEventHandler(rabbitEventPublisher);
        ScheduleStartedEvent event = new ScheduleStartedEvent(10L, 1L, "NOT_STARTED");

        handler.on(event);

        verify(rabbitEventPublisher).publish(event);
    }

    @Test
    void completedHandler_publishesEvent() {
        ScheduleCompletedIntegratedEventHandler handler = new ScheduleCompletedIntegratedEventHandler(rabbitEventPublisher);
        ScheduleCompletedEvent event = new ScheduleCompletedEvent(11L, 1L, "IN_PROGRESS");

        handler.on(event);

        verify(rabbitEventPublisher).publish(event);
    }

    @Test
    void canceledHandler_publishesEvent() {
        ScheduleCanceledIntegratedEventHandler handler = new ScheduleCanceledIntegratedEventHandler(rabbitEventPublisher);
        ScheduleCanceledEvent event = new ScheduleCanceledEvent(12L, 1L, "COMPLETED");

        handler.on(event);

        verify(rabbitEventPublisher).publish(event);
    }
}
