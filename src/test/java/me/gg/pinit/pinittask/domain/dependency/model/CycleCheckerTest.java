package me.gg.pinit.pinittask.domain.dependency.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static me.gg.pinit.pinittask.domain.dependency.model.GraphUtils.getDependenciesSample;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CycleCheckerTest {
    @Test
    void isCycleContained_NoCycle() {
        //given
        List<Dependency> dependenciesSample = getDependenciesSample();
        Graph graph = Graph.of(dependenciesSample);

        //when
        boolean hasCycle = graph.hasCycle(List.of(), List.of());

        //then
        assertFalse(hasCycle);
    }

    @Test
    void isCycleContained_Cycle() {
        //given
        long taskA = 1L;
        long taskB = 2L;
        long taskC = 3L;

        List<Dependency> dependencies = new ArrayList<>();
        dependencies.add(new Dependency(taskA, taskB));
        dependencies.add(new Dependency(taskB, taskC));

        Graph graph = Graph.of(dependencies);

        //when
        boolean hasCycle = graph.hasCycle(List.of(), List.of(new Dependency(taskC, taskA)));

        //then
        assertTrue(hasCycle);
    }

}
