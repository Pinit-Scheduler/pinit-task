package me.gg.pinit.pinittask.domain.task.repository;

import me.gg.pinit.pinittask.domain.task.model.Task;
import me.gg.pinit.pinittask.domain.task.vo.ImportanceConstraint;
import me.gg.pinit.pinittask.domain.task.vo.TemporalConstraint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TaskRepositoryTest {

    @Autowired
    TaskRepository taskRepository;

    @Test
    void findNextByCursor_ordersByDateThenId() {
        Task t1 = new Task(1L, "A", "A-desc",
                new TemporalConstraint(ZonedDateTime.of(2024, 1, 2, 18, 0, 0, 0, ZoneOffset.ofHours(9)), Duration.ZERO),
                new ImportanceConstraint(3, 3));
        Task t2 = new Task(1L, "B", "B-desc",
                new TemporalConstraint(ZonedDateTime.of(2024, 1, 2, 8, 0, 0, 0, ZoneOffset.ofHours(9)), Duration.ZERO),
                new ImportanceConstraint(4, 5));
        Task t3 = new Task(1L, "C", "C-desc",
                new TemporalConstraint(ZonedDateTime.of(2024, 1, 3, 9, 0, 0, 0, ZoneOffset.UTC), Duration.ZERO),
                new ImportanceConstraint(5, 8));

        taskRepository.saveAll(List.of(t1, t2, t3));

        List<Task> firstPage = taskRepository.findNextByCursor(1L, false, LocalDate.of(2024, 1, 1), 0L, PageRequest.of(0, 10));
        assertThat(firstPage).extracting(Task::getId)
                .containsExactly(t1.getId(), t2.getId(), t3.getId());

        List<Task> afterFirst = taskRepository.findNextByCursor(1L, false, t1.getTemporalConstraint().getDeadlineDate(), t1.getId(), PageRequest.of(0, 10));
        assertThat(afterFirst).extracting(Task::getId)
                .containsExactly(t2.getId(), t3.getId());
    }
}
