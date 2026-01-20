package me.gg.pinit.pinittask.application.dependency.service;

import me.gg.pinit.pinittask.domain.dependency.model.Dependency;
import me.gg.pinit.pinittask.domain.dependency.repository.DependencyRepository;
import me.gg.pinit.pinittask.domain.dependency.repository.FromToPair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DependencyServiceTest {

    @InjectMocks
    DependencyService dependencyService;

    @Mock
    DependencyRepository dependencyRepository;

    @Test
    @DisplayName("사이클 발생 - 새 의존관계 추가로 1->2->3->1 형성")
    void checkCycle_true_whenAddedDependencyCreatesCycle() {
        Long memberId = 10L;
        List<Dependency> existing = Arrays.asList(
                new Dependency(1L, 2L),
                new Dependency(2L, 3L)
        );
        when(dependencyRepository.findAllByOwnerId(memberId)).thenReturn(existing);
        List<Dependency> removed = Collections.emptyList();
        List<Dependency> added = List.of(new Dependency(3L, 1L));
        boolean hasCycle = dependencyService.checkCycle(memberId, removed, added);
        assertTrue(hasCycle);
    }

    @Test
    @DisplayName("사이클 없음 - 비순환 그래프에 비순환 간선 추가")
    void checkCycle_false_whenNoCycleAfterAddition() {
        Long memberId = 11L;
        List<Dependency> existing = Arrays.asList(
                new Dependency(1L, 2L),
                new Dependency(2L, 3L)
        );
        when(dependencyRepository.findAllByOwnerId(memberId)).thenReturn(existing);
        List<Dependency> removed = Collections.emptyList();
        List<Dependency> added = List.of(new Dependency(4L, 1L));
        boolean hasCycle = dependencyService.checkCycle(memberId, removed, added);
        assertFalse(hasCycle);
    }

    @Test
    @DisplayName("사이클 제거 - 기존 사이클 간선 제거")
    void checkCycle_false_whenCycleEdgeRemoved() {
        Long memberId = 12L;
        Dependency cycleEdge = new Dependency(3L, 1L);
        List<Dependency> existing = Arrays.asList(
                new Dependency(1L, 2L),
                new Dependency(2L, 3L),
                cycleEdge
        );
        when(dependencyRepository.findAllByOwnerId(memberId)).thenReturn(existing);
        List<Dependency> removed = List.of(cycleEdge);
        List<Dependency> added = Collections.emptyList();
        boolean hasCycle = dependencyService.checkCycle(memberId, removed, added);
        assertFalse(hasCycle);
    }

    @Test
    @DisplayName("다음 작업 ID 조회 - 1에서 출발")
    void getNextTaskIds_returnsOutgoingTargets() {
        Long memberId = 20L;
        List<Dependency> existing = Arrays.asList(
                new Dependency(1L, 2L),
                new Dependency(1L, 3L),
                new Dependency(2L, 4L)
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
                new Dependency(1L, 4L),
                new Dependency(2L, 4L),
                new Dependency(3L, 5L)
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
        List<Dependency> deps = Arrays.asList(new Dependency(10L, 11L), new Dependency(11L, 12L));
        dependencyService.saveAll(deps);
        verify(dependencyRepository, times(1)).saveAll(deps);
    }

    @Test
    @DisplayName("deleteAll FromToPair 변환 및 호출")
    void deleteAll_convertsAndDelegates() {
        List<Dependency> deps = Arrays.asList(new Dependency(100L, 101L), new Dependency(101L, 102L));
        List<FromToPair> expectedPairs = Arrays.asList(new FromToPair(100L, 101L), new FromToPair(101L, 102L));
        when(dependencyRepository.deleteByFromToPairs(expectedPairs)).thenReturn(expectedPairs.size());
        dependencyService.deleteAll(deps);
        verify(dependencyRepository, times(1)).deleteByFromToPairs(expectedPairs);
    }

    @Test
    @DisplayName("특정 작업 관련 의존 삭제 호출")
    void deleteWithTaskId_delegates() {
        when(dependencyRepository.deleteAllRelatedToTask(777L)).thenReturn(0);
        dependencyService.deleteWithTaskId(777L);
        verify(dependencyRepository, times(1)).deleteAllRelatedToTask(777L);
    }
}
