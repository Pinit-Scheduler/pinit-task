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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {
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
}
