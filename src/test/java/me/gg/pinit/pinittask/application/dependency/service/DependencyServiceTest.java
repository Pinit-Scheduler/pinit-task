package me.gg.pinit.pinittask.application.dependency.service;

import me.gg.pinit.pinittask.domain.dependency.model.Dependency;
import me.gg.pinit.pinittask.domain.dependency.repository.DependencyRepository;
import me.gg.pinit.pinittask.domain.dependency.repository.FromToPair;
import me.gg.pinit.pinittask.domain.task.model.Task;
import me.gg.pinit.pinittask.domain.task.model.TaskUtils;
import me.gg.pinit.pinittask.domain.task.repository.TaskRepository;
import me.gg.pinit.pinittask.domain.task.vo.ImportanceConstraint;
import me.gg.pinit.pinittask.domain.task.vo.TemporalConstraint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DependencyServiceTest {

    @InjectMocks
    DependencyService dependencyService;

    @Mock
    DependencyRepository dependencyRepository;
    @Mock
    TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        lenient().when(taskRepository.findAllByIdInWithOptimisticLock(anyCollection())).thenReturn(Collections.emptyList());
        lenient().when(dependencyRepository.findAllByFromId(anyLong())).thenReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("사이클 발생 - 새 의존관계 추가로 1->2->3->1 형성")
    void checkCycle_true_whenAddedDependencyCreatesCycle() {
        Long memberId = 10L;
        List<Dependency> existing = Arrays.asList(
                new Dependency(memberId, 1L, 2L),
                new Dependency(memberId, 2L, 3L)
        );
        when(dependencyRepository.findAllByOwnerId(memberId)).thenReturn(existing);
        List<Dependency> removed = Collections.emptyList();
        List<Dependency> added = List.of(new Dependency(memberId, 3L, 1L));
        assertThrows(IllegalStateException.class, () -> dependencyService.assertNoCycle(memberId, removed, added));
    }

    @Test
    @DisplayName("사이클 없음 - 비순환 그래프에 비순환 간선 추가")
    void checkCycle_false_whenNoCycleAfterAddition() {
        Long memberId = 11L;
        List<Dependency> existing = Arrays.asList(
                new Dependency(memberId, 1L, 2L),
                new Dependency(memberId, 2L, 3L)
        );
        when(dependencyRepository.findAllByOwnerId(memberId)).thenReturn(existing);
        List<Dependency> removed = Collections.emptyList();
        List<Dependency> added = List.of(new Dependency(memberId, 4L, 1L));
        assertDoesNotThrow(() -> dependencyService.assertNoCycle(memberId, removed, added));
    }

    @Test
    @DisplayName("사이클 제거 - 기존 사이클 간선 제거")
    void checkCycle_false_whenCycleEdgeRemoved() {
        Long memberId = 12L;
        Dependency cycleEdge = new Dependency(memberId, 3L, 1L);
        List<Dependency> existing = Arrays.asList(
                new Dependency(memberId, 1L, 2L),
                new Dependency(memberId, 2L, 3L),
                cycleEdge
        );
        when(dependencyRepository.findAllByOwnerId(memberId)).thenReturn(existing);
        List<Dependency> removed = List.of(cycleEdge);
        List<Dependency> added = Collections.emptyList();
        assertDoesNotThrow(() -> dependencyService.assertNoCycle(memberId, removed, added));
    }

    @Test
    @DisplayName("다음 작업 ID 조회 - 1에서 출발")
    void getNextTaskIds_returnsOutgoingTargets() {
        Long memberId = 20L;
        List<Dependency> existing = Arrays.asList(
                new Dependency(memberId, 1L, 2L),
                new Dependency(memberId, 1L, 3L),
                new Dependency(memberId, 2L, 4L)
        );
        when(dependencyRepository.findAllByOwnerId(memberId)).thenReturn(existing);
        List<Long> nextIds = dependencyService.getNextTaskIds(memberId, 1L);
        assertEquals(2, nextIds.size());
        assertTrue(nextIds.containsAll(Set.of(2L, 3L)));
    }

    @Test
    @DisplayName("다음 작업 ID 없음 - 출발 노드 미존재")
    void getNextTaskIds_empty_whenNodeNotExists() {
        Long memberId = 21L;
        when(dependencyRepository.findAllByOwnerId(memberId)).thenReturn(Collections.emptyList());
        List<Long> nextIds = dependencyService.getNextTaskIds(memberId, 999L);
        assertTrue(nextIds.isEmpty());
    }

    @Test
    @DisplayName("이전 작업 ID 조회 - 4로 도달")
    void getPreviousTaskIds_returnsIncomingSources() {
        Long memberId = 30L;
        List<Dependency> existing = Arrays.asList(
                new Dependency(memberId, 1L, 4L),
                new Dependency(memberId, 2L, 4L),
                new Dependency(memberId, 3L, 5L)
        );
        when(dependencyRepository.findAllByOwnerId(memberId)).thenReturn(existing);
        List<Long> prevIds = dependencyService.getPreviousTaskIds(memberId, 4L);
        assertEquals(2, prevIds.size());
        assertTrue(prevIds.containsAll(Set.of(1L, 2L)));
    }

    @Test
    @DisplayName("이전 작업 ID 없음 - 대상 노드 미존재")
    void getPreviousTaskIds_empty_whenNodeNotExists() {
        Long memberId = 31L;
        when(dependencyRepository.findAllByOwnerId(memberId)).thenReturn(Collections.emptyList());
        List<Long> prevIds = dependencyService.getPreviousTaskIds(memberId, 555L);
        assertTrue(prevIds.isEmpty());
    }

    @Test
    @DisplayName("saveAll 호출 위임")
    void saveAll_delegatesToRepository() {
        List<Dependency> deps = Arrays.asList(new Dependency(0L, 10L, 11L), new Dependency(0L, 11L, 12L));
        dependencyService.saveAll(deps);
        verify(dependencyRepository, times(1)).saveAll(deps);
    }

    @Test
    @DisplayName("deleteAll FromToPair 변환 및 호출")
    void deleteAll_convertsAndDelegates() {
        List<Dependency> deps = Arrays.asList(new Dependency(0L, 100L, 101L), new Dependency(0L, 101L, 102L));
        List<FromToPair> expectedPairs = Arrays.asList(new FromToPair(100L, 101L), new FromToPair(101L, 102L));
        when(dependencyRepository.deleteByFromToPairs(expectedPairs)).thenReturn(expectedPairs.size());
        dependencyService.deleteAll(deps);
        verify(dependencyRepository, times(1)).deleteByFromToPairs(expectedPairs);
    }

    @Test
    @DisplayName("특정 작업 관련 의존 삭제 호출")
    void deleteWithTaskId_delegates() {
        when(dependencyRepository.findAllByFromId(777L)).thenReturn(Collections.emptyList());
        when(dependencyRepository.deleteAllRelatedToTask(777L)).thenReturn(0);
        dependencyService.deleteWithTaskId(777L);
        verify(dependencyRepository).findAllByFromId(777L);
        verify(dependencyRepository, times(1)).deleteAllRelatedToTask(777L);
    }

    @Test
    @DisplayName("saveAll 시 대상 Task의 inboundDependencyCount 증가")
    void saveAll_increasesInboundCount() {
        Task target = buildTaskWithId(10L);
        when(taskRepository.findAllByIdInWithOptimisticLock(Set.of(10L))).thenReturn(List.of(target));

        dependencyService.saveAll(List.of(new Dependency(0L, 1L, 10L)));

        assertEquals(1, target.getInboundDependencyCount());
        verify(taskRepository).findAllByIdInWithOptimisticLock(Set.of(10L));
    }

    @Test
    @DisplayName("deleteAll 시 대상 Task의 inboundDependencyCount 감소")
    void deleteAll_decreasesInboundCount() {
        Task target = buildTaskWithId(20L);
        ReflectionTestUtils.setField(target, "inboundDependencyCount", 2);
        when(taskRepository.findAllByIdInWithOptimisticLock(Set.of(20L))).thenReturn(List.of(target));

        dependencyService.deleteAll(List.of(new Dependency(0L, 1L, 20L), new Dependency(0L, 2L, 20L)));

        assertEquals(0, target.getInboundDependencyCount());
        verify(taskRepository).findAllByIdInWithOptimisticLock(Set.of(20L));
    }

    @Test
    @DisplayName("deleteWithTaskId 시 fromId에 연결된 toId들의 inboundDependencyCount 감소")
    void deleteWithTaskId_decreasesInboundForOutgoingEdges() {
        Task to1 = buildTaskWithId(30L);
        Task to2 = buildTaskWithId(31L);
        ReflectionTestUtils.setField(to1, "inboundDependencyCount", 1);
        ReflectionTestUtils.setField(to2, "inboundDependencyCount", 2);
        when(dependencyRepository.findAllByFromId(99L)).thenReturn(List.of(new Dependency(0L, 99L, 30L), new Dependency(0L, 99L, 31L), new Dependency(0L, 99L, 31L)));
        when(taskRepository.findAllByIdInWithOptimisticLock(Set.of(30L, 31L))).thenReturn(List.of(to1, to2));
        when(dependencyRepository.deleteAllRelatedToTask(99L)).thenReturn(0);

        dependencyService.deleteWithTaskId(99L);

        assertEquals(0, to1.getInboundDependencyCount());
        assertEquals(0, to2.getInboundDependencyCount());
    }

    private Task buildTaskWithId(Long id) {
        Task task = new Task(1L, "title", "desc", new TemporalConstraint(TaskUtils.DEADLINE_TIME, Duration.ZERO), new ImportanceConstraint(5, 5));
        ReflectionTestUtils.setField(task, "id", id);
        return task;
    }
}
