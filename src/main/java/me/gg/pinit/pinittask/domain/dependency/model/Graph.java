package me.gg.pinit.pinittask.domain.dependency.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {
    private final Map<Long, GraphNode> nodeMap = new HashMap<>();
    private CycleChecker cycleChecker;
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
        for (Dependency dependency : dependencies) {
            Long fromId = dependency.getFromId();
            Long toId = dependency.getToId();

            graph.nodeMap.putIfAbsent(fromId, new GraphNode(fromId));
            graph.nodeMap.putIfAbsent(toId, new GraphNode(toId));

            GraphNode fromNode = graph.nodeMap.get(fromId);
            GraphNode toNode = graph.nodeMap.get(toId);

            fromNode.addNext(toNode);
            toNode.addPrevious(fromNode);
        }
        graph.cycleChecker = new CycleChecker(graph.nodeMap);
        return graph;
    }

    public List<Long> getNextTaskIds(Long fromTaskId) {
        return nodeMap.getOrDefault(fromTaskId, new GraphNode(fromTaskId)).getNextTasks();
    }

    public List<Long> getPreviousTaskIds(Long toTaskId) {
        return nodeMap.getOrDefault(toTaskId, new GraphNode(toTaskId)).getPreviousTasks();
    }

    public boolean hasCycle(List<Dependency> removedDependencies, List<Dependency> addedDependencies) {
        return cycleChecker.hasCycleAfterChanges(removedDependencies, addedDependencies);
    }
}

/**
 * 1. 의존관계 추가/제거 대비
 * 2. 사이클 체크 - 의존관계 변화 반영 - 사이클 체커
 * 3. 이전 일정 완료 여부 체크 -
 * 전체 맵이 필요한가?
 * - 사이클 체크 -> 전체 맵이 필요함
 * - 이전/이후 일정 완료 여부 체크 -> 해당 노드와 그 옆 노드만 알면 됨
 * 조회/수정 단위는 반드시 해당 스케줄과 이전/이후 스케줄로 한정된다.
 * 전체 노드를 알 필요가 없다.
 *
 * 사이클 체크의 경우
 * 전체 맵이 필요하다.
 * 맵이 바뀔 경우를 대비할 수 있어야 한다.
 *
 * 일단 사이클 체크하고 함께 만들어두자.
 */