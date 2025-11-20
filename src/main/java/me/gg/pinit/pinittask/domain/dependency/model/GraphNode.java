package me.gg.pinit.pinittask.domain.dependency.model;

import lombok.Getter;

import java.util.*;

public class GraphNode {
    @Getter
    private Long scheduleId;
    private Set<GraphNode> next = new HashSet<>();
    private Set<GraphNode> previous = new HashSet<>();

    public GraphNode(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public void addNext(GraphNode next) {
        this.next.add(next);
    }

    public void addPrevious(GraphNode previous) {
        this.previous.add(previous);
    }

    public void removeNext(GraphNode next) {
        this.next.remove(next);
    }

    public void removePrevious(GraphNode previous) {
        this.previous.remove(previous);
    }

    public List<Long> getNextSchedules() {
        List<Long> result = new ArrayList<>();
        for (GraphNode node : next) {
            result.add(node.getScheduleId());
        }
        return result;
    }

    public List<Long> getPreviousSchedules() {
        List<Long> result = new ArrayList<>();
        for (GraphNode node : previous) {
            result.add(node.getScheduleId());
        }
        return result;
    }


    public int getIndegree() {
        return previous.size();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GraphNode node = (GraphNode) o;
        return Objects.equals(scheduleId, node.scheduleId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(scheduleId);
    }

}
