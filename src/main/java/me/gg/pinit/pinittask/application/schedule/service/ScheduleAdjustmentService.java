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
        if (command.hasTaskId()) {
            throw new IllegalArgumentException("작업과 연결된 일정은 작업 API를 통해 생성해야 합니다.");
        }
        if (!command.getAddDependencies().isEmpty() || !command.getRemoveDependencies().isEmpty()) {
            throw new IllegalArgumentException("작업이 없는 일정에는 의존 관계를 설정할 수 없습니다.");
        }
        return scheduleService.addSchedule(command.buildSchedule(null));
    }

    /**
     * V0 레거시: 일정 생성 시 Task를 함께 생성/연결하고 의존관계도 저장.
     */
    @Transactional
    public Schedule createScheduleLegacy(Long memberId, ScheduleDependencyAdjustCommand command) {
        List<Dependency> addedDependencies = command.getAddDependencies();
        dependencyService.assertNoCycle(memberId, List.of(), addedDependencies);

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
        dependencyService.assertNoCycle(memberId, removedDependencies, addedDependencies);

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
