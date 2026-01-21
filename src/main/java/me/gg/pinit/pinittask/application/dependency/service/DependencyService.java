package me.gg.pinit.pinittask.application.dependency.service;

import me.gg.pinit.pinittask.domain.dependency.model.Dependency;
import me.gg.pinit.pinittask.domain.dependency.model.Graph;
import me.gg.pinit.pinittask.domain.dependency.repository.DependencyRepository;
import me.gg.pinit.pinittask.domain.dependency.repository.FromToPair;
import me.gg.pinit.pinittask.domain.task.model.Task;
import me.gg.pinit.pinittask.domain.task.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DependencyService {
    private final DependencyRepository dependencyRepository;
    private final TaskRepository taskRepository;

    public DependencyService(DependencyRepository dependencyRepository, TaskRepository taskRepository) {
        this.dependencyRepository = dependencyRepository;
        this.taskRepository = taskRepository;
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
        updateInboundCount(dependencies, 1);
        dependencyRepository.saveAll(dependencies);
    }

    @Transactional
    public void deleteAll(List<Dependency> dependencies) {
        updateInboundCount(dependencies, -1);
        List<FromToPair> fromToPairs = dependencies.stream()
                .map(dependency -> new FromToPair(dependency.getFromId(), dependency.getToId()))
                .toList();
        dependencyRepository.deleteByFromToPairs(fromToPairs);
    }

    @Transactional
    public void deleteWithTaskId(Long taskId) {
        List<Dependency> outgoing = dependencyRepository.findAllByFromId(taskId);
        updateInboundCount(outgoing, -1);
        dependencyRepository.deleteAllRelatedToTask(taskId);
    }

    /**
     * 추가되는 의존관계에 맞춰, 연결된 Task들의 inbound Count를 조정해준다.
     *
     * @param dependencies
     * @param delta
     */
    private void updateInboundCount(List<Dependency> dependencies, int delta) {
        if (dependencies.isEmpty()) {
            return;
        }
        Map<Long, Long> toCount = dependencies.stream()
                .collect(Collectors.groupingBy(Dependency::getToId, Collectors.counting()));
        List<Task> tasks = taskRepository.findAllByIdInWithOptimisticLock(toCount.keySet());
        tasks.forEach(task -> task.adjustInboundDependencies(Math.toIntExact(toCount.get(task.getId()) * delta)));
    }
}
