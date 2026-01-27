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

    @Test
    void findCompletedArchiveByCursor_filtersCompletedAndCutoffAndOrdersDesc() {
        Task tAfterCutoff = completedTask(1L, ZonedDateTime.of(2025, 1, 10, 0, 0, 0, 0, ZoneOffset.UTC));
        Task t1 = completedTask(1L, ZonedDateTime.of(2025, 1, 7, 0, 0, 0, 0, ZoneOffset.UTC));
        Task t2 = completedTask(1L, ZonedDateTime.of(2025, 1, 6, 0, 0, 0, 0, ZoneOffset.UTC));
        Task t3 = new Task(1L, "uncompleted", "no", new TemporalConstraint(ZonedDateTime.of(2025, 1, 4, 0, 0, 0, 0, ZoneOffset.UTC), Duration.ZERO), new ImportanceConstraint(1, 1));
        Task tOtherOwner = completedTask(2L, ZonedDateTime.of(2025, 1, 6, 0, 0, 0, 0, ZoneOffset.UTC));

        taskRepository.saveAll(List.of(tAfterCutoff, t1, t2, t3, tOtherOwner));

        List<Task> result = taskRepository.findCompletedArchiveByCursor(
                1L,
                LocalDate.of(2025, 1, 9),
                LocalDate.of(2025, 1, 10),
                Long.MAX_VALUE,
                PageRequest.of(0, 10)
        );

        assertThat(result).extracting(Task::getId)
                .containsExactly(t1.getId(), t2.getId());
    }

    @Test
    void findCompletedArchiveByCursor_respectsCursorForSameDeadline() {
        Task t1 = completedTask(1L, ZonedDateTime.of(2025, 2, 1, 0, 0, 0, 0, ZoneOffset.UTC));
        Task t2 = completedTask(1L, ZonedDateTime.of(2025, 2, 1, 0, 0, 0, 0, ZoneOffset.UTC));
        taskRepository.saveAll(List.of(t1, t2));

        List<Task> firstPage = taskRepository.findCompletedArchiveByCursor(
                1L,
                LocalDate.of(2025, 2, 2),
                LocalDate.of(2025, 2, 2),
                Long.MAX_VALUE,
                PageRequest.of(0, 1)
        );
        assertThat(firstPage).hasSize(1);

        Task last = firstPage.getLast();
        List<Task> secondPage = taskRepository.findCompletedArchiveByCursor(
                1L,
                LocalDate.of(2025, 2, 2),
                last.getTemporalConstraint().getDeadlineDate(),
                last.getId(),
                PageRequest.of(0, 1)
        );

        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.getFirst().getId()).isNotEqualTo(last.getId());
    }

    @Test
    void findCompletedArchiveByCursor_includesCutoff_excludesAfterCutoff() {
        LocalDate cutoff = LocalDate.of(2025, 1, 9);
        Task afterCutoff = completedTask(1L, ZonedDateTime.of(2025, 1, 10, 0, 0, 0, 0, ZoneOffset.UTC)); // should be excluded
        Task cutoffTask = completedTask(1L, ZonedDateTime.of(2025, 1, 9, 0, 0, 0, 0, ZoneOffset.UTC));     // should be included
        Task beforeCutoff = completedTask(1L, ZonedDateTime.of(2025, 1, 8, 0, 0, 0, 0, ZoneOffset.UTC));   // should be included
        taskRepository.saveAll(List.of(afterCutoff, cutoffTask, beforeCutoff));

        List<Task> result = taskRepository.findCompletedArchiveByCursor(
                1L,
                cutoff,
                cutoff.plusDays(1),
                Long.MAX_VALUE,
                PageRequest.of(0, 10)
        );

        assertThat(result).extracting(Task::getId)
                .containsExactly(cutoffTask.getId(), beforeCutoff.getId());
    }

    @Test
    void findCompletedArchiveByCursor_skipsAlreadySeenWhenCursorMatchesDateAndId() {
        LocalDate cutoff = LocalDate.of(2025, 3, 1);
        Task first = completedTask(1L, ZonedDateTime.of(2025, 3, 1, 0, 0, 0, 0, ZoneOffset.UTC));
        Task second = completedTask(1L, ZonedDateTime.of(2025, 3, 1, 0, 0, 0, 0, ZoneOffset.UTC));
        taskRepository.saveAll(List.of(first, second));

        // Order is deadline DESC, id DESC, so larger id comes first
        Long maxId = Math.max(first.getId(), second.getId());
        Long minId = Math.min(first.getId(), second.getId());

        List<Task> pageAfterCursor = taskRepository.findCompletedArchiveByCursor(
                1L,
                cutoff,
                cutoff,
                maxId,
                PageRequest.of(0, 10)
        );

        assertThat(pageAfterCursor).extracting(Task::getId)
                .containsExactly(minId);
    }

    private Task completedTask(Long ownerId, ZonedDateTime deadline) {
        Task task = new Task(ownerId, "done", "desc", new TemporalConstraint(deadline, Duration.ZERO), new ImportanceConstraint(1, 1));
        task.markCompleted();
        return task;
    }
}
