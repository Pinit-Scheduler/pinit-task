package me.gg.pinit.pinittask.interfaces.dto;

import me.gg.pinit.pinittask.application.datetime.DateTimeUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskCreateRequestTest {

    @Test
    void toCommand_requiresPlaceholderZeroForEachDependency() {
        TaskCreateRequest req = new TaskCreateRequest(
                "t",
                "d",
                new DateTimeWithZone(LocalDateTime.now(), ZoneId.of("UTC")),
                5,
                3,
                List.of(new DependencyRequest(10L, 20L)) // neither is 0
        );

        assertThrows(IllegalArgumentException.class,
                () -> req.toCommand(null, 1L, new DateTimeUtils()));
    }
}
