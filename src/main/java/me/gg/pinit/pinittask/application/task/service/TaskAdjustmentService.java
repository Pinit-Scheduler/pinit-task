package me.gg.pinit.pinittask.application.task.service;

import lombok.RequiredArgsConstructor;
import me.gg.pinit.pinittask.application.dependency.service.DependencyService;
import me.gg.pinit.pinittask.application.task.dto.TaskDependencyAdjustCommand;
import me.gg.pinit.pinittask.domain.dependency.model.Dependency;
import me.gg.pinit.pinittask.domain.task.model.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskAdjustmentService {
    private final DependencyService dependencyService;
    private final TaskService taskService;

    @Transactional
    public Task createTask(Long memberId, TaskDependencyAdjustCommand command) {
        Task saved = taskService.createTask(command.buildTask());
        List<Dependency> addedDependencies = command.getAddDependencies(saved.getId());
        dependencyService.assertNoCycle(memberId, List.of(), addedDependencies);
        dependencyService.saveAll(addedDependencies);
        return saved;
    }

    @Transactional
    public Task updateTask(Long memberId, TaskDependencyAdjustCommand command) {
        Long taskId = command.getTaskId();
        if (taskId == null) {
            throw new IllegalArgumentException("taskId는 null일 수 없습니다.");
        }
        command.validateNoPlaceholderForUpdate();
        List<Dependency> removedDependencies = command.getRemoveDependencies(taskId);
        List<Dependency> addedDependencies = command.getAddDependencies(taskId);
        dependencyService.assertNoCycle(memberId, removedDependencies, addedDependencies);

        Task updated = taskService.updateTask(memberId, taskId, command.getTaskPatch());
        dependencyService.deleteAll(removedDependencies);
        dependencyService.saveAll(addedDependencies);
        return updated;
    }
}
