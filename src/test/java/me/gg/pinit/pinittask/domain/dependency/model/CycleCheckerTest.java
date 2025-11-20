package me.gg.pinit.pinittask.domain.dependency.model;

import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static me.gg.pinit.pinittask.domain.dependency.model.GraphUtils.getDependenciesSample;
import static me.gg.pinit.pinittask.domain.schedule.model.ScheduleUtils.getNotStartedSchedule;
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
        Schedule scheduleA = getNotStartedSchedule(1L);
        Schedule scheduleB = getNotStartedSchedule(2L);
        Schedule scheduleC = getNotStartedSchedule(3L);

        List<Dependency> dependencies = new ArrayList<>();
        dependencies.add(new Dependency(scheduleA.getId(), scheduleB.getId()));
        dependencies.add(new Dependency(scheduleB.getId(), scheduleC.getId()));

        Graph graph = Graph.of(dependencies);

        //when
        boolean hasCycle = graph.hasCycle(List.of(), List.of(new Dependency(scheduleC.getId(), scheduleA.getId())));

        //then
        assertTrue(hasCycle);
    }

}