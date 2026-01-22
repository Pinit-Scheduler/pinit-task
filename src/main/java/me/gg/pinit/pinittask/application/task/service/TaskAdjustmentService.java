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
        List<Dependency> addedDependencies = command.getAddDependencies();
        dependencyService.checkCycle(memberId, List.of(), addedDependencies);

        Task saved = taskService.createTask(command.buildTask());
        dependencyService.saveAll(addedDependencies);
        return saved;
    }

    @Transactional
    public Task updateTask(Long memberId, TaskDependencyAdjustCommand command) {
        List<Dependency> removedDependencies = command.getRemoveDependencies();
        List<Dependency> addedDependencies = command.getAddDependencies();
        dependencyService.checkCycle(memberId, removedDependencies, addedDependencies);

        Task updated = taskService.updateTask(memberId, command.getTaskId(), command.getTaskPatch());
        dependencyService.deleteAll(removedDependencies);
        dependencyService.saveAll(addedDependencies);
        return updated;
    }
}
