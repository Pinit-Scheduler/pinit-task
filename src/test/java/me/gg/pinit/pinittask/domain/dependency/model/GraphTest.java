package me.gg.pinit.pinittask.domain.dependency.model;

import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static me.gg.pinit.pinittask.domain.dependency.model.GraphUtils.getDependenciesSample;
import static me.gg.pinit.pinittask.domain.schedule.model.ScheduleUtils.getNotStartedSchedule;
import static org.junit.jupiter.api.Assertions.*;

class GraphTest {

    @Test
    void getNextScheduleIds() {
        //given
        Schedule now = getNotStartedSchedule(1L);
        Schedule after1 = getNotStartedSchedule(2L);
        Schedule after2 = getNotStartedSchedule(3L);

        List<Dependency> dependencies = new ArrayList<>();
        dependencies.add(new Dependency(now.getId(), after1.getId()));
        dependencies.add(new Dependency(now.getId(), after2.getId()));

        Graph graph = Graph.of(dependencies);

        //when
        List<Long> nextScheduleIds = graph.getNextScheduleIds(now.getId());

        //then
        assertEquals(2, nextScheduleIds.size());
        assertTrue(nextScheduleIds.contains(after1.getId()));
        assertTrue(nextScheduleIds.contains(after2.getId()));
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