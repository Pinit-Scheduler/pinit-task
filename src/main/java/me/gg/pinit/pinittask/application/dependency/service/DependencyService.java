package me.gg.pinit.pinittask.application.dependency.service;

import me.gg.pinit.pinittask.domain.dependency.model.Dependency;
import me.gg.pinit.pinittask.domain.dependency.model.Graph;
import me.gg.pinit.pinittask.domain.dependency.repository.DependencyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DependencyService {
    private DependencyRepository dependencyRepository;

    public DependencyService(DependencyRepository dependencyRepository) {
        this.dependencyRepository = dependencyRepository;
    }

    public boolean checkCycle(Long memberId, List<Dependency> removedDependencies, List<Dependency> addedDependencies) {
        List<Dependency> allByOwnerId = dependencyRepository.findAllByOwnerId(memberId);
        Graph graph = Graph.of(allByOwnerId);
        return graph.hasCycle(removedDependencies, addedDependencies);
    }

    public boolean isBeforeCompleted(Long memberId, Long scheduleId) {
        List<Dependency> allByOwnerId = dependencyRepository.findAllByOwnerId(memberId);
        Graph graph = Graph.of(allByOwnerId);
        return graph.isBeforeCompleted(scheduleId);
    }

    public List<Long> getNextScheduleIds(Long memberId, Long scheduleId) {
        List<Dependency> allByOwnerId = dependencyRepository.findAllByOwnerId(memberId);
        Graph graph = Graph.of(allByOwnerId);
        return graph.getNextScheduleIds(scheduleId);
    }

    public List<Long> getPreviousScheduleIds(Long memberId, Long scheduleId) {
        List<Dependency> allByOwnerId = dependencyRepository.findAllByOwnerId(memberId);
        Graph graph = Graph.of(allByOwnerId);
        return graph.getPreviousScheduleIds(scheduleId);
    }

    public void saveAll(List<Dependency> dependencies) {
        dependencyRepository.saveAll(dependencies);
    }
}
