package me.gg.pinit.pinittask.interfaces.web;

import me.gg.pinit.pinittask.application.datetime.DateTimeUtils;
import me.gg.pinit.pinittask.application.schedule.service.ScheduleService;
import me.gg.pinit.pinittask.application.schedule.service.ScheduleStateChangeService;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.interfaces.dto.DateTimeWithZone;
import me.gg.pinit.pinittask.interfaces.dto.ScheduleSimpleRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleControllerV1Test {

    @Mock
    ScheduleService scheduleService;
    @Mock
    ScheduleStateChangeService scheduleStateChangeService;
    DateTimeUtils dateTimeUtils = new DateTimeUtils();

    @InjectMocks
    ScheduleControllerV1 controller;

    Long memberId;

    @BeforeEach
    void setUp() {
        memberId = 1L;
        controller = new ScheduleControllerV1(dateTimeUtils, scheduleService, scheduleStateChangeService);
    }

    @Test
    void createSchedule_returnsCreatedResponse() throws Exception {
        ScheduleSimpleRequest request = new ScheduleSimpleRequest(
                "title",
                "desc",
                new DateTimeWithZone(LocalDateTime.of(2024, 1, 1, 9, 0), ZoneId.of("UTC"))
        );
        Schedule saved = new Schedule(memberId, null, request.title(), request.description(),
                dateTimeUtils.toZonedDateTime(request.date().dateTime(), request.date().zoneId()));
        setScheduleId(saved, 99L);
        when(scheduleService.addSchedule(any(Schedule.class))).thenReturn(saved);

        ResponseEntity<?> response = controller.createSchedule(memberId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        verify(scheduleService).addSchedule(any(Schedule.class));
    }

    @Test
    void deleteSchedule_deletesScheduleOnly() throws Exception {
        Long scheduleId = 77L;

        ResponseEntity<Void> response = controller.deleteSchedule(memberId, scheduleId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(scheduleService).deleteSchedule(memberId, scheduleId);
    }

    @Test
    void getSchedules_returnsScheduleList() {
        ZonedDateTime zdt = ZonedDateTime.of(LocalDateTime.of(2024, 1, 2, 9, 0), ZoneId.of("Asia/Seoul"));
        Schedule schedule = new Schedule(memberId, null, "title", "desc", zdt);
        when(scheduleService.getScheduleList(eq(memberId), any())).thenReturn(List.of(schedule));

        var result = controller.getSchedules(memberId, zdt.toLocalDateTime(), zdt.getZone());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("title");
        ArgumentCaptor<ZonedDateTime> zdtCaptor = ArgumentCaptor.forClass(ZonedDateTime.class);
        verify(scheduleService).getScheduleList(eq(memberId), zdtCaptor.capture());
        assertThat(zdtCaptor.getValue().toLocalDate()).isEqualTo(zdt.toLocalDate());
    }

    private void setScheduleId(Schedule schedule, Long id) throws Exception {
        Field idField = Schedule.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(schedule, id);
    }
}
