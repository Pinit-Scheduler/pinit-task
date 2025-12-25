package me.gg.pinit.pinittask.application.dependency.event;

import me.gg.pinit.pinittask.application.dependency.service.DependencyService;
import me.gg.pinit.pinittask.domain.schedule.event.ScheduleDeletedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScheduleDeletedEventHandlerTest {
    @Mock
    DependencyService dependencyService;
    @InjectMocks
    ScheduleDeletedEventHandler handler;

    @Test
    void deletesDependenciesAfterScheduleRemoval() {
        //given
        ScheduleDeletedEvent event = new ScheduleDeletedEvent(77L, 9L);

        //when
        handler.on(event);

        //then
        verify(dependencyService).deleteWithScheduleId(77L);
    }
}
