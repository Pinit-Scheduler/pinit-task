package me.gg.pinit.pinittask.interfaces.web;

import me.gg.pinit.pinittask.application.datetime.DateTimeUtils;
import me.gg.pinit.pinittask.application.dependency.service.DependencyService;
import me.gg.pinit.pinittask.application.schedule.service.ScheduleService;
import me.gg.pinit.pinittask.application.task.service.TaskAdjustmentService;
import me.gg.pinit.pinittask.application.task.service.TaskService;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.domain.schedule.model.ScheduleType;
import me.gg.pinit.pinittask.domain.task.model.Task;
import me.gg.pinit.pinittask.domain.task.vo.ImportanceConstraint;
import me.gg.pinit.pinittask.domain.task.vo.TemporalConstraint;
import me.gg.pinit.pinittask.interfaces.dto.DateTimeWithZone;
import me.gg.pinit.pinittask.interfaces.task.TaskControllerV1;
import me.gg.pinit.pinittask.interfaces.task.dto.TaskCursorPageResponse;
import me.gg.pinit.pinittask.interfaces.task.dto.TaskScheduleRequest;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskControllerV1Test {

    @Mock
    DateTimeUtils dateTimeUtils;
    @Mock
    TaskAdjustmentService taskAdjustmentService;
    @Mock
    TaskService taskService;
    @Mock
    ScheduleService scheduleService;
    @Mock
    DependencyService dependencyService;

    @InjectMocks
    TaskControllerV1 controller;

    Long memberId;

    @BeforeEach
    void setUp() {
        memberId = 1L;
    }

    @Test
    void deleteTask_forwardsFlagToService() {
        Long taskId = 10L;

        ResponseEntity<Void> response = controller.deleteTask(memberId, taskId, true);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(taskService).deleteTask(memberId, taskId, true);
    }

    @Test
    void createScheduleFromTask_usesExistingTaskAndAddsSchedule() throws Exception {
        Long taskId = 11L;
        Task task = buildTask(memberId);
        setTaskId(task, taskId);
        TaskScheduleRequest request = new TaskScheduleRequest(
                null,
                null,
                new DateTimeWithZone(LocalDateTime.of(2024, 1, 1, 10, 0), ZoneId.of("UTC")),
                ScheduleType.DEEP_WORK
        );
        ZonedDateTime targetTime = ZonedDateTime.of(request.date().dateTime(), request.date().zoneId());
        when(taskService.getTask(memberId, taskId)).thenReturn(task);
        when(dateTimeUtils.toZonedDateTime(request.date().dateTime(), request.date().zoneId())).thenReturn(targetTime);
        Schedule saved = new Schedule(memberId, taskId, task.getTitle(), task.getDescription(), targetTime, ScheduleType.DEEP_WORK);
        when(scheduleService.addSchedule(any(Schedule.class))).thenReturn(saved);

        ResponseEntity<?> response = controller.createScheduleFromTask(memberId, taskId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(taskService).getTask(memberId, taskId);
        ArgumentCaptor<Schedule> scheduleCaptor = ArgumentCaptor.forClass(Schedule.class);
        verify(scheduleService).addSchedule(scheduleCaptor.capture());
        Schedule schedule = scheduleCaptor.getValue();
        assertThat(schedule.getTaskId()).isEqualTo(taskId);
        assertThat(schedule.getDesignatedStartTime()).isEqualTo(targetTime);
    }

    @Test
    void getTasksByCursor_delegatesToServiceAndReturnsBody() {
        TaskCursorPageResponse expected = TaskCursorPageResponse.of(List.of(), "next", true);
        when(taskService.getTasksByCursor(memberId, 15, "c1", true)).thenReturn(expected);

        TaskCursorPageResponse resp = controller.getTasksByCursor(memberId, 15, "c1", true);

        assertThat(resp).isSameAs(expected);
        verify(taskService).getTasksByCursor(memberId, 15, "c1", true);
    }

    private Task buildTask(Long ownerId) {
        return new Task(
                ownerId,
                "title",
                "desc",
                new TemporalConstraint(ZonedDateTime.now().plusDays(1), Duration.ZERO),
                new ImportanceConstraint(5, 5)
        );
    }

    private void setTaskId(Task task, Long id) throws Exception {
        Field idField = Task.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(task, id);
    }
}
