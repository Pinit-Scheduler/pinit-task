package me.gg.pinit.pinittask.application.dependency.service;

import me.gg.pinit.pinittask.domain.dependency.model.Dependency;
import me.gg.pinit.pinittask.domain.dependency.model.Graph;
import me.gg.pinit.pinittask.domain.dependency.repository.DependencyRepository;
import me.gg.pinit.pinittask.domain.dependency.repository.FromToPair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DependencyService {
    private final DependencyRepository dependencyRepository;

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
    public List<Long> getNextTaskIds(Long memberId, Long taskId) {
        List<Dependency> allByOwnerId = dependencyRepository.findAllByOwnerId(memberId);
        Graph graph = Graph.of(allByOwnerId);
        return graph.getNextTaskIds(taskId);
    }

    @Transactional(readOnly = true)
    public List<Long> getPreviousTaskIds(Long memberId, Long taskId) {
        List<Dependency> allByOwnerId = dependencyRepository.findAllByOwnerId(memberId);
        Graph graph = Graph.of(allByOwnerId);
        return graph.getPreviousTaskIds(taskId);
    }

    @Transactional
    public void saveAll(List<Dependency> dependencies) {
        dependencyRepository.saveAll(dependencies);
    }

    @Transactional
    public void deleteAll(List<Dependency> dependencies) {
        List<FromToPair> fromToPairs = dependencies.stream()
                .map(dependency -> new FromToPair(dependency.getFromId(), dependency.getToId()))
                .toList();
        dependencyRepository.deleteByFromToPairs(fromToPairs);
    }

    @Transactional
    public void deleteWithTaskId(Long taskId) {
        dependencyRepository.deleteAllRelatedToTask(taskId);
    }
}
