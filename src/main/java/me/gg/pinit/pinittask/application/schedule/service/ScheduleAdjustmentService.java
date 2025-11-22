package me.gg.pinit.pinittask.application.schedule.service;

import me.gg.pinit.pinittask.application.dependency.service.DependencyService;
import me.gg.pinit.pinittask.application.schedule.dto.ScheduleDependencyAdjustCommand;
import me.gg.pinit.pinittask.domain.dependency.model.Dependency;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ScheduleAdjustmentService {

    private final DependencyService dependencyService;
    private final ScheduleService scheduleService;

    public ScheduleAdjustmentService(DependencyService dependencyService, ScheduleService scheduleService) {
        this.dependencyService = dependencyService;
        this.scheduleService = scheduleService;
    }

    @Transactional
    public Schedule createSchedule(Long memberId, ScheduleDependencyAdjustCommand command) {
        Schedule temporalSchedule = command.getTemporalSchedule();
        List<Dependency> addedDependencies = command.getAddDependencies();
        dependencyService.checkCycle(memberId, List.of(), addedDependencies);

        Schedule saved = scheduleService.addSchedule(temporalSchedule);
        dependencyService.saveAll(addedDependencies);
        return saved;
    }

    @Transactional
    public Schedule adjustSchedule(Long memberId, ScheduleDependencyAdjustCommand command) {
        List<Dependency> removedDependencies = command.getRemoveDependencies();
        List<Dependency> addedDependencies = command.getAddDependencies();
        dependencyService.checkCycle(memberId, removedDependencies, addedDependencies);

        Schedule updatedSchedule = scheduleService.updateSchedule(memberId, command.getScheduleId(), command.getSchedulePatch());
        dependencyService.deleteAll(removedDependencies);
        dependencyService.saveAll(addedDependencies);
        return updatedSchedule;
    }
}
