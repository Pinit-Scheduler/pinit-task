package me.gg.pinit.pinittask.application.schedule.service;

import me.gg.pinit.pinittask.application.dependency.service.DependencyService;
import me.gg.pinit.pinittask.application.schedule.dto.ScheduleDependencyAdjustCommand;
import me.gg.pinit.pinittask.application.task.service.TaskService;
import me.gg.pinit.pinittask.domain.dependency.model.Dependency;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import me.gg.pinit.pinittask.domain.task.model.Task;
import me.gg.pinit.pinittask.domain.task.patch.TaskPatch;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ScheduleAdjustmentService {

    private final DependencyService dependencyService;
    private final ScheduleService scheduleService;
    private final TaskService taskService;

    public ScheduleAdjustmentService(DependencyService dependencyService, ScheduleService scheduleService, TaskService taskService) {
        this.dependencyService = dependencyService;
        this.scheduleService = scheduleService;
        this.taskService = taskService;
    }

    @Transactional
    public Schedule createSchedule(Long memberId, ScheduleDependencyAdjustCommand command) {
        List<Dependency> addedDependencies = command.getAddDependencies();
        dependencyService.checkCycle(memberId, List.of(), addedDependencies);

        Task task = command.hasTaskId()
                ? taskService.getTask(memberId, command.getTaskId())
                : taskService.createTask(command.buildTask());
        Schedule saved = scheduleService.addSchedule(command.buildSchedule(task.getId()));
        dependencyService.saveAll(addedDependencies);
        return saved;
    }

    @Transactional
    public Schedule adjustSchedule(Long memberId, ScheduleDependencyAdjustCommand command) {
        Schedule current = scheduleService.getSchedule(memberId, command.getScheduleId());
        if (current.getTaskId() == null) {
            return scheduleService.updateSchedule(memberId, command.getScheduleId(), command.getSchedulePatch());
        }
        List<Dependency> removedDependencies = command.getRemoveDependencies();
        List<Dependency> addedDependencies = command.getAddDependencies();
        dependencyService.checkCycle(memberId, removedDependencies, addedDependencies);

        TaskPatch taskPatch = command.getTaskPatch();
        if (current.getTaskId() != null) {
            taskService.updateTask(memberId, current.getTaskId(), taskPatch);
        }
        Schedule updatedSchedule = scheduleService.updateSchedule(memberId, command.getScheduleId(), command.getSchedulePatch());
        dependencyService.deleteAll(removedDependencies);
        dependencyService.saveAll(addedDependencies);
        return updatedSchedule;
    }
}
