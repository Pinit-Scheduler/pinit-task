package me.gg.pinit.pinittask.domain.dependency.model;

import java.util.*;

public class CycleChecker {
    private final Map<Long, GraphNode> nodeMap;

    public CycleChecker(Map<Long, GraphNode> nodeMap) {
        this.nodeMap = nodeMap;
    }

    public boolean hasCycleAfterChanges(List<Dependency> removedDependencies,
                                        List<Dependency> addedDependencies) {
        for (Dependency dep : removedDependencies) {
            long from = dep.getFromId();
            long to = dep.getToId();
            GraphNode fromNode = nodeMap.computeIfAbsent(from, GraphNode::new);
            GraphNode toNode = nodeMap.computeIfAbsent(to, GraphNode::new);
            fromNode.removeNext(toNode);
            toNode.removePrevious(fromNode);
        }

        for (Dependency dep : addedDependencies) {
            long from = dep.getFromId();
            long to = dep.getToId();
            GraphNode fromNode = nodeMap.computeIfAbsent(from, GraphNode::new);
            GraphNode toNode = nodeMap.computeIfAbsent(to, GraphNode::new);
            fromNode.addNext(toNode);
            toNode.addPrevious(fromNode);
        }


        Map<Long, Integer> indegreeMap = new HashMap<>();
        for (GraphNode node : nodeMap.values()) {
            indegreeMap.put(node.getTaskId(), node.getIndegree());
        }
        Queue<Long> q = initializeZeroIndegreeQueue(indegreeMap);
        return checkCycle(q, indegreeMap);

    }

    private Queue<Long> initializeZeroIndegreeQueue(Map<Long, Integer> indegreeMap) {
        Queue<Long> ret = new ArrayDeque<>();
        for (Map.Entry<Long, Integer> entry : indegreeMap.entrySet()) {
            if (entry.getValue() == 0) {
                ret.add(entry.getKey());
            }
        }
        return ret;
    }

    private boolean checkCycle(Queue<Long> q, Map<Long, Integer> indegreeMap) {
        int removed = 0;
        while (!q.isEmpty()) {
            long u = q.poll();
            removed++;
            for (long v : nodeMap.get(u).getNextTasks()) {
                if (indegreeMap.merge(v, -1, Integer::sum) == 0) q.add(v);
            }
        }
        return removed != indegreeMap.size();
    }
}

/**
 * 사이클 체크
 * 삭제된 의존이 정점 하나를 제거할 가능성
 * 제거된 정점은 indegree 0이 되어 큐에 들어감 -> size 계산에 영향을 끼치지 않음
 * <p>
 * 어디가 null이 될지 모름 -> 예외 처리가 빡세다.
 */
