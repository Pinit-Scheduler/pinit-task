package me.gg.pinit.pinittask.domain.dependency.model;

import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static me.gg.pinit.pinittask.domain.dependency.model.GraphUtils.getDependenciesFromSchedule;
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
        after1.addDependency(now);
        after2.addDependency(now);

        List<Dependency> dependencies = getDependenciesFromSchedule(after1);
        dependencies.addAll(getDependenciesFromSchedule(after2));


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

    @Test
    void isCycleContained_NoCycle() {
        //given
        List<Dependency> dependenciesSample = getDependenciesSample();
        Graph graph = Graph.of(dependenciesSample);

        //when
        boolean hasCycle = graph.isCycleContained();

        //then
        assertFalse(hasCycle);
    }

    @Test
    void isCycleContained_Cycle() {
        //given
        Schedule scheduleA = getNotStartedSchedule(1L);
        Schedule scheduleB = getNotStartedSchedule(2L);
        Schedule scheduleC = getNotStartedSchedule(3L);

        scheduleB.addDependency(scheduleA);
        scheduleC.addDependency(scheduleB);
        scheduleA.addDependency(scheduleC); // 사이클 생성

        List<Dependency> dependencies = getDependenciesFromSchedule(scheduleA);
        dependencies.addAll(getDependenciesFromSchedule(scheduleB));
        dependencies.addAll(getDependenciesFromSchedule(scheduleC));

        Graph graph = Graph.of(dependencies);

        //when
        boolean hasCycle = graph.isCycleContained();

        //then
        assertTrue(hasCycle);
    }
}