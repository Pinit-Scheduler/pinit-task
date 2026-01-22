package me.gg.pinit.pinittask.domain.dependency.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static me.gg.pinit.pinittask.domain.dependency.model.GraphUtils.getDependenciesSample;
import static org.junit.jupiter.api.Assertions.*;

class GraphTest {

    @Test
    void getNextTaskIds() {
        //given
        long now = 1L;
        long after1 = 2L;
        long after2 = 3L;

        List<Dependency> dependencies = new ArrayList<>();
        dependencies.add(new Dependency(1L, now, after1));
        dependencies.add(new Dependency(1L, now, after2));

        Graph graph = Graph.of(dependencies);

        //when
        List<Long> nextScheduleIds = graph.getNextTaskIds(now);

        //then
        assertEquals(2, nextScheduleIds.size());
        assertTrue(nextScheduleIds.contains(after1));
        assertTrue(nextScheduleIds.contains(after2));
    }

    @Test
    void of() {
        //given
        List<Dependency> dependenciesSample = getDependenciesSample();
        //when
        Graph graph = Graph.of(dependenciesSample);

        //then
        assertNotNull(graph);
    }
}
