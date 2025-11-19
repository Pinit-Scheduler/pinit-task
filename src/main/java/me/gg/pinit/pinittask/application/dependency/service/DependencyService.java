package me.gg.pinit.pinittask.application.dependency.service;

import me.gg.pinit.pinittask.domain.dependency.model.Dependency;
import me.gg.pinit.pinittask.domain.dependency.model.Graph;
import me.gg.pinit.pinittask.domain.dependency.repository.DependencyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DependencyService {
    private DependencyRepository dependencyRepository;

    public DependencyService(DependencyRepository dependencyRepository) {
        this.dependencyRepository = dependencyRepository;
    }

    @Transactional(readOnly = true)
    public boolean checkCycle(Long memberId, List<Dependency> removedDependencies, List<Dependency> addedDependencies) {
        List<Dependency> allByOwnerId = dependencyRepository.findAllByOwnerId(memberId);
        Graph graph = Graph.of(allByOwnerId);
        return graph.hasCycle(removedDependencies, addedDependencies);
    }

    @Transactional(readOnly = true)
    public boolean isBeforeCompleted(Long memberId, Long scheduleId) {
        List<Dependency> allByOwnerId = dependencyRepository.findAllByOwnerId(memberId);
        Graph graph = Graph.of(allByOwnerId);
        return graph.isBeforeCompleted(scheduleId);
    }

    @Transactional(readOnly = true)
    public List<Long> getNextScheduleIds(Long memberId, Long scheduleId) {
        List<Dependency> allByOwnerId = dependencyRepository.findAllByOwnerId(memberId);
        Graph graph = Graph.of(allByOwnerId);
        return graph.getNextScheduleIds(scheduleId);
    }

    @Transactional(readOnly = true)
    public List<Long> getPreviousScheduleIds(Long memberId, Long scheduleId) {
        List<Dependency> allByOwnerId = dependencyRepository.findAllByOwnerId(memberId);
        Graph graph = Graph.of(allByOwnerId);
        return graph.getPreviousScheduleIds(scheduleId);
    }

    @Transactional
    public void saveAll(List<Dependency> dependencies) {
        dependencyRepository.saveAll(dependencies);
    }

    @Transactional
    public void deleteWithScheduleId(Long scheduleId) {
        dependencyRepository.deleteAllRelatedToSchedule(scheduleId);
    }
}
