package me.gg.pinit.pinittask.application.task.service;

import lombok.RequiredArgsConstructor;
import me.gg.pinit.pinittask.application.dependency.service.DependencyService;
import me.gg.pinit.pinittask.application.events.DomainEventPublisher;
import me.gg.pinit.pinittask.application.schedule.service.ScheduleService;
import me.gg.pinit.pinittask.domain.events.DomainEvent;
import me.gg.pinit.pinittask.domain.events.DomainEvents;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.domain.task.exception.TaskNotFoundException;
import me.gg.pinit.pinittask.domain.task.model.Task;
import me.gg.pinit.pinittask.domain.task.patch.TaskPatch;
import me.gg.pinit.pinittask.domain.task.repository.TaskRepository;
import me.gg.pinit.pinittask.interfaces.task.dto.TaskCursorPageResponse;
import me.gg.pinit.pinittask.interfaces.task.dto.TaskResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {
    private static final String CURSOR_DELIMITER = "|";
    private static final DateTimeFormatter CURSOR_DEADLINE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final TaskRepository taskRepository;
    private final DependencyService dependencyService;
    private final ScheduleService scheduleService;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional(readOnly = true)
    public Task getTask(Long ownerId, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("해당 작업을 찾을 수 없습니다."));
        validateOwner(ownerId, task);
        return task;
    }

    @Transactional(readOnly = true)
    public Page<Task> getTasks(Long ownerId, Pageable pageable, boolean readyOnly) {
        if (readyOnly) {
            return taskRepository.findAllByOwnerIdAndInboundDependencyCountAndCompletedFalse(ownerId, 0, pageable);
        }
        return taskRepository.findAllByOwnerId(ownerId, pageable);
    }

    @Deprecated
    @Transactional(readOnly = true)
    public TaskCursorPageResponse getTasksByCursor(Long ownerId, int size, String cursor, boolean readyOnly) {
        CursorPage cursorPage = loadTasksByCursor(ownerId, size, cursor, readyOnly);
        var dependencyInfoMap = dependencyService.getDependencyInfoForTasks(ownerId, cursorPage.tasks().stream().map(Task::getId).toList());
        List<TaskResponse> data = cursorPage.tasks().stream()
                .map(task -> TaskResponse.from(task, dependencyInfoMap.get(task.getId())))
                .toList();
        return TaskCursorPageResponse.of(data, cursorPage.nextCursor(), cursorPage.hasNext());
    }

    @Transactional(readOnly = true)
    public CursorPage getTasksByCursorPage(Long ownerId, int size, String cursor, boolean readyOnly) {
        return loadTasksByCursor(ownerId, size, cursor, readyOnly);
    }

    @Transactional(readOnly = true)
    public List<Task> findTasksByIds(Long ownerId, List<Long> taskIds) {
        return taskRepository.findAllById(taskIds).stream()
                .filter(task -> task.getOwnerId().equals(ownerId))
                .toList();
    }


    @Transactional
    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTask(Long ownerId, Long taskId, TaskPatch patch) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("해당 작업을 찾을 수 없습니다."));
        validateOwner(ownerId, task);
        task.patch(patch);
        return task;
    }

    @Transactional
    public void deleteTask(Long ownerId, Long taskId, boolean deleteSchedules) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("해당 작업을 찾을 수 없습니다."));
        validateOwner(ownerId, task);
        if (deleteSchedules) {
            scheduleService.deleteSchedulesByTaskId(ownerId, taskId);
        } else {
            scheduleService.detachSchedulesByTaskId(ownerId, taskId);
        }
        dependencyService.deleteWithTaskId(taskId);
        taskRepository.delete(task);
    }

    @Transactional
    public void markCompleted(Long ownerId, Long taskId) {
        Task task = getTask(ownerId, taskId);
        syncScheduleOnTaskCompletion(ownerId, task);
        task.markCompleted();
        publishEvents();
    }

    @Transactional
    public void markIncomplete(Long ownerId, Long taskId) {
        Task task = getTask(ownerId, taskId);
        task.markIncomplete();
        publishEvents();
    }

    private CursorPage loadTasksByCursor(Long ownerId, int size, String cursor, boolean readyOnly) {
        Cursor decoded = decodeCursor(cursor);
        List<Task> tasks = taskRepository.findNextByCursor(
                ownerId,
                readyOnly,
                decoded.deadline(),
                decoded.id(),
                PageRequest.of(0, size)
        );
        boolean hasNext = tasks.size() == size;
        String nextCursor = hasNext ? encodeCursor(tasks.getLast()) : null;
        return new CursorPage(tasks, nextCursor, hasNext);
    }

    private void validateOwner(Long ownerId, Task task) {
        if (!task.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Member does not own the task");
        }
    }

    private void syncScheduleOnTaskCompletion(Long ownerId, Task task) {
        Optional<Schedule> maybeSchedule = scheduleService.findByTaskId(task.getId());
        if (maybeSchedule.isEmpty()) {
            return;
        }
        Schedule schedule = maybeSchedule.get();
        if (!schedule.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Member does not own the schedule");
        }
        if (schedule.isInProgress() || schedule.isSuspended()) {
            throw new IllegalStateException("해당 작업과 연결된 일정이 현재 진행중이어서 작업을 완료할 수 없습니다. 일정을 통해 작업을 완료해주세요.");
        }
        if (!schedule.isCompleted()) {
            schedule.finish(ZonedDateTime.now(schedule.getDesignatedStartTime().getZone()));
        }
    }

    private void publishEvents() {
        Deque<DomainEvent> queue = DomainEvents.getEventsAndClear();
        queue.forEach(domainEventPublisher::publish);
    }

    private String encodeCursor(Task task) {
        LocalDate deadline = task.getTemporalConstraint().getDeadlineDate();
        return deadline.atStartOfDay().format(CURSOR_DEADLINE_FORMAT) + CURSOR_DELIMITER + task.getId();
    }

    private Cursor decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return new Cursor(LocalDate.MIN, 0L);
        }
        String[] parts = cursor.split("\\|");
        if (parts.length != 2) {
            throw new IllegalArgumentException("잘못된 커서 형식입니다.");
        }
        try {
            LocalDate deadline = parseDeadline(parts[0]);
            Long id = Long.parseLong(parts[1]);
            return new Cursor(deadline, id);
        } catch (Exception e) {
            throw new IllegalArgumentException("잘못된 커서 값입니다.", e);
        }
    }

    private LocalDate parseDeadline(String raw) {
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(raw).toLocalDate();
        } catch (DateTimeParseException e) {
            return LocalDateTime.parse(appendMissingSeconds(raw)).toLocalDate();
        }
    }

    private String appendMissingSeconds(String raw) {
        if (raw.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}$")) {
            return raw + ":00";
        }
        throw new IllegalArgumentException("잘못된 커서 값입니다.");
    }


    private record Cursor(LocalDate deadline, Long id) {
    }

    public record CursorPage(List<Task> tasks, String nextCursor, boolean hasNext) {
    }
}
