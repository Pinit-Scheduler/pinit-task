package me.gg.pinit.pinittask.application.task.dto;

import me.gg.pinit.pinittask.application.schedule.dto.DependencyDto;
import me.gg.pinit.pinittask.domain.dependency.model.Dependency;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TaskDependencyAdjustCommandTest {

    @Test
    void resolvesSelfPlaceholderForZeroIds() {
        Long ownerId = 1L;
        Long selfId = 99L;
        TaskDependencyAdjustCommand command = new TaskDependencyAdjustCommand(
                null,
                ownerId,
                "t",
                "d",
                ZonedDateTime.now(),
                5,
                3,
                List.of(),
                List.of(
                        new DependencyDto(null, 0L, 5L),     // fromId=0 -> self
                        new DependencyDto(null, 4L, 0L)      // toId=0   -> self
                )
        );

        List<Dependency> deps = command.getAddDependencies(selfId);

        assertThat(deps)
                .extracting(Dependency::getFromId, Dependency::getToId)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(selfId, 5L),
                        org.assertj.core.groups.Tuple.tuple(4L, selfId)
                );
    }

    @Test
    void validateNoPlaceholderForUpdate_rejectsZeroIds() {
        TaskDependencyAdjustCommand command = new TaskDependencyAdjustCommand(
                10L,
                1L,
                "t",
                "d",
                ZonedDateTime.now(),
                5,
                3,
                List.of(),
                List.of(new DependencyDto(null, 0L, 2L))
        );

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, command::validateNoPlaceholderForUpdate);
    }
}
