package me.gg.pinit.pinittask.application.task.service;

import lombok.RequiredArgsConstructor;
import me.gg.pinit.pinittask.application.member.service.MemberService;
import me.gg.pinit.pinittask.domain.task.model.Task;
import me.gg.pinit.pinittask.domain.task.repository.TaskRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class TaskArchiveService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final String CURSOR_DELIMITER = "|";
    private static final Pattern DATE_WITH_OPTIONAL_OFFSET_PATTERN = Pattern.compile("(?<date>\\d{4}-\\d{2}-\\d{2})(?<offset>[+-]\\d{2}:?\\d{2})?");

    private final TaskRepository taskRepository;
    private final MemberService memberService;
    private final Clock clock;

    @Transactional(readOnly = true)
    public CursorPage getCompletedArchive(Long ownerId, Integer sizeParam, String cursor, ZoneOffset requestOffset) {
        int size = resolveSize(sizeParam);
        ZoneOffset effectiveOffset = resolveOffset(ownerId, requestOffset);
        LocalDate cutoffDate = LocalDate.now(clock.withZone(effectiveOffset)).minusDays(1);

        Cursor decoded = decodeCursor(cursor, cutoffDate);
        Pageable pageable = PageRequest.of(0, size + 1);
        List<Task> tasks = taskRepository.findCompletedArchiveByCursor(
                ownerId,
                cutoffDate,
                decoded.deadline(),
                decoded.id(),
                pageable
        );

        boolean hasNext = tasks.size() > size;
        List<Task> content = hasNext ? tasks.subList(0, size) : tasks;
        String nextCursor = hasNext ? encodeCursor(content.getLast()) : null;
        return new CursorPage(content, nextCursor, hasNext, cutoffDate, effectiveOffset);
    }

    private int resolveSize(Integer sizeParam) {
        int size = sizeParam == null ? DEFAULT_PAGE_SIZE : sizeParam;
        if (size <= 0 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size는 1 이상 100 이하이어야 합니다.");
        }
        return size;
    }

    private ZoneOffset resolveOffset(Long ownerId, ZoneOffset requestOffset) {
        if (requestOffset != null) {
            return requestOffset;
        }
        return memberService.findZoneOffsetOfMember(ownerId);
    }

    private String encodeCursor(Task task) {
        return task.getTemporalConstraint().getDeadlineDate() + CURSOR_DELIMITER + task.getId();
    }

    private Cursor decodeCursor(String cursor, LocalDate cutoffDate) {
        if (cursor == null || cursor.isBlank()) {
            return new Cursor(cutoffDate.plusDays(1), Long.MAX_VALUE);
        }
        String[] parts = cursor.split("\\|");
        if (parts.length != 2) {
            throw new IllegalArgumentException("커서는 'yyyy-MM-dd|id' 형식이어야 합니다.");
        }
        LocalDate deadline = parseDate(parts[0]);
        long id = parseId(parts[1]);
        return new Cursor(deadline, id);
    }

    private LocalDate parseDate(String raw) {
        Matcher matcher = DATE_WITH_OPTIONAL_OFFSET_PATTERN.matcher(raw);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("커서는 'yyyy-MM-dd|id' 형식이어야 합니다.");
        }
        return LocalDate.parse(matcher.group("date"));
    }

    private long parseId(String raw) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("커서의 ID가 올바르지 않습니다.", e);
        }
    }

    public record CursorPage(List<Task> tasks, String nextCursor, boolean hasNext, LocalDate cutoffDate,
                             ZoneOffset offset) {
    }

    private record Cursor(LocalDate deadline, long id) {
    }
}
