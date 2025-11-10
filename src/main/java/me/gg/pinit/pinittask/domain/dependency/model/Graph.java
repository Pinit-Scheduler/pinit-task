package me.gg.pinit.pinittask.domain.dependency.model;

import me.gg.pinit.pinittask.domain.dependency.exception.ScheduleNotFoundException;

import java.util.*;

public class Graph {
    private Map<Long, Integer> internalId = new HashMap<>();
    private Map<Integer, Long> externalId = new HashMap<>();
    private List<Integer>[] adjList;
    private int[] indegree;
    private int size;

    private Graph() {
    }

    /**
     * 그래프 생성
     * 1. 의존 관계를 매핑하며, 정점의 개수를 센다.
     * 2. 정점의 개수만큼 리스트를 생성한다.
     * 3. 리스트 내에 의존 관계를 매핑한다. 이때, Indegree도 함께 계산해둔다.
     * 4. 완성된 그래프를 반환한다.
     *
     * @param dependencies
     * @return
     */
    public static Graph of(List<Dependency> dependencies) {
        Graph graph = new Graph();
        graph.setCompressedMap(dependencies);
        graph.initializeAdjList();
        graph.connectEdges(dependencies);
        return graph;
    }

    public boolean hasCycle() {
        return false;
    }

    public List<Long> getNextScheduleIds(Long fromScheduleId) {
        return adjList[internalScheduleIdFor(fromScheduleId)].stream()
                .map(this::externalScheduleIdFor)
                .toList();
    }

    public boolean isCycleContained() {
        int[] indeg = getIndegree();
        Queue<Integer> q = initializeQueueForCycleCheck(indeg);
        return checkCycle(q, indeg);
    }

    private boolean checkCycle(Queue<Integer> q, int[] indeg) {
        int removed = 0;
        while (!q.isEmpty()) {
            int u = q.poll();
            removed++;
            for (int v : adjList[u]) {
                if (--indeg[v] == 0) q.add(v);
            }
        }
        return removed != size;
    }

    private Queue<Integer> initializeQueueForCycleCheck(int[] indeg) {
        Queue<Integer> q = new ArrayDeque<>();
        for (int i = 0; i < size; i++) if (indeg[i] == 0) q.add(i);
        return q;
    }

    private int[] getIndegree() {
        int[] indeg = new int[size];
        for (int u = 0; u < size; u++)
            for (int v : adjList[u]) indeg[v]++;
        return indeg;
    }

    private void setCompressedMap(List<Dependency> dependencies) {
        for (Dependency dependency : dependencies) {
            Long fromId = dependency.getFrom().getId();
            Long toId = dependency.getTo().getId();
            insertToMap(fromId);
            insertToMap(toId);
        }
    }

    private void initializeAdjList() {
        adjList = new ArrayList[size];
        for (int i = 0; i < size; i++) {
            adjList[i] = new ArrayList<>();
        }
    }

    private void connectEdges(List<Dependency> dependencies) {
        for (Dependency dependency : dependencies) {
            Long fromId = dependency.getFrom().getId();
            Long toId = dependency.getTo().getId();
            insertEdge(fromId, toId);
        }
    }

    private void insertEdge(Long fromScheduleId, Long toScheduleId) {
        Integer fromInternalId = internalScheduleIdFor(fromScheduleId);
        Integer toInternalId = internalScheduleIdFor(toScheduleId);
        adjList[fromInternalId].add(toInternalId);
    }

    private void insertToMap(Long scheduleId) {
        if (!internalId.containsKey(scheduleId)) {
            int id = size++;
            internalId.put(scheduleId, id);
            externalId.put(id, scheduleId);
        }
    }

    private Integer internalScheduleIdFor(Long scheduleId) {
        return internalId.computeIfAbsent(scheduleId, k -> {
            throw new ScheduleNotFoundException("일정을 찾을 수 없습니다.");
        });
    }

    private Long externalScheduleIdFor(Integer internalId) {
        return externalId.computeIfAbsent(internalId, k -> {
            throw new ScheduleNotFoundException("일정을 찾을 수 없습니다.");
        });
    }
}
/**
 * 1. 입력의 형식을 우선 파악한다.
 * 2. 사이클을 검사한다.
 * <p>
 * 필요한 정보
 * 이전 일정이 모두 완료되었는지 -> 바로 이전 일정만 봐도 충분함
 * <p>
 * 현재 정점의 이전 일정과 다음 일정 가장 가까운 시작시간, 가장 가까운 종료 시간 확인
 * <p>
 * 그래프
 */

/**
 * 스케줄 삭제 시, 의존은 나중에 삭제되도록
 * 이벤트로 분리한다? 트랜잭션 경계 끊기?
 * - 의존이 삭제되지 않았다고 스케줄을 삭제하는 것을 롤백하는 것은 문제가 있다.
 * - 확실히 의존과 스케줄은 별도의 도메인으로 분리되어야 한다.
 * <p>
 * 따라서, 스케줄 참조하는 동안 NPE가 발생하는 문제는 스케줄 쪽에서 잘 처리하는 방식으로 가야 한다.
 */