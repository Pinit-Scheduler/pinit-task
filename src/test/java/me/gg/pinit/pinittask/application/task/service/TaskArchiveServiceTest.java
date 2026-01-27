package me.gg.pinit.pinittask.application.task.service;

import me.gg.pinit.pinittask.application.member.service.MemberService;
import me.gg.pinit.pinittask.domain.task.model.Task;
import me.gg.pinit.pinittask.domain.task.repository.TaskRepository;
import me.gg.pinit.pinittask.domain.task.vo.ImportanceConstraint;
import me.gg.pinit.pinittask.domain.task.vo.TemporalConstraint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskArchiveServiceTest {

    @Mock
    TaskRepository taskRepository;
    @Mock
    MemberService memberService;

    Clock fixedClock;
    TaskArchiveService service;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2025-01-10T00:00:00Z"), ZoneOffset.UTC);
        service = new TaskArchiveService(taskRepository, memberService, fixedClock);
    }

    @Test
    void getCompletedArchive_buildsCutoffAndNextCursor() {
        ZoneOffset offset = ZoneOffset.of("+09:00");
        when(memberService.findZoneOffsetOfMember(1L)).thenReturn(offset);

        Task t1 = new Task(1L, "a", "a", new TemporalConstraint(ZonedDateTime.of(2025, 1, 9, 0, 0, 0, 0, offset), Duration.ZERO), new ImportanceConstraint(1, 1));
        Task t2 = new Task(1L, "b", "b", new TemporalConstraint(ZonedDateTime.of(2025, 1, 8, 0, 0, 0, 0, offset), Duration.ZERO), new ImportanceConstraint(1, 1));
        Task t3 = new Task(1L, "c", "c", new TemporalConstraint(ZonedDateTime.of(2025, 1, 7, 0, 0, 0, 0, offset), Duration.ZERO), new ImportanceConstraint(1, 1));
        ReflectionTestUtils.setField(t1, "id", 10L);
        ReflectionTestUtils.setField(t2, "id", 9L);
        ReflectionTestUtils.setField(t3, "id", 8L);

        when(taskRepository.findCompletedArchiveByCursor(anyLong(), any(), any(), anyLong(), any(Pageable.class)))
                .thenReturn(List.of(t1, t2, t3));

        TaskArchiveService.CursorPage page = service.getCompletedArchive(1L, 2, null, null);

        assertThat(page.hasNext()).isTrue();
        assertThat(page.nextCursor()).isEqualTo("2025-01-08|9");
        assertThat(page.cutoffDate()).isEqualTo(LocalDate.of(2025, 1, 9));

        ArgumentCaptor<LocalDate> cutoffCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> cursorDateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<Long> cursorIdCaptor = ArgumentCaptor.forClass(Long.class);
        verify(taskRepository).findCompletedArchiveByCursor(eq(1L), cutoffCaptor.capture(), cursorDateCaptor.capture(), cursorIdCaptor.capture(), any(Pageable.class));
        assertThat(cutoffCaptor.getValue()).isEqualTo(LocalDate.of(2025, 1, 9));
        assertThat(cursorDateCaptor.getValue()).isEqualTo(LocalDate.of(2025, 1, 10));
        assertThat(cursorIdCaptor.getValue()).isEqualTo(Long.MAX_VALUE);
    }

    @Test
    void getCompletedArchive_prefersRequestOffset() {
        ZoneOffset requestOffset = ZoneOffset.of("+02:00");
        when(taskRepository.findCompletedArchiveByCursor(anyLong(), any(), any(), anyLong(), any(Pageable.class)))
                .thenReturn(List.of());
        TaskArchiveService.CursorPage page = service.getCompletedArchive(2L, 1, null, requestOffset);

        assertThat(page.cutoffDate()).isEqualTo(LocalDate.of(2025, 1, 9));
        verifyNoInteractions(memberService);
    }

    @Test
    void getCompletedArchive_rejectsInvalidSize() {
        assertThatThrownBy(() -> service.getCompletedArchive(1L, 0, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size는 1 이상 100 이하이어야 합니다.");
    }

    @Test
    void getCompletedArchive_rejectsBadCursor() {
        when(memberService.findZoneOffsetOfMember(1L)).thenReturn(ZoneOffset.UTC);
        assertThatThrownBy(() -> service.getCompletedArchive(1L, 20, "bad-cursor", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("커서는 'yyyy-MM-dd|id' 형식이어야 합니다.");
    }
}
