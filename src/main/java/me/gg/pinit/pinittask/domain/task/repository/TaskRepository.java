package me.gg.pinit.pinittask.domain.task.repository;

import jakarta.persistence.LockModeType;
import me.gg.pinit.pinittask.domain.task.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByOwnerId(Long ownerId);

    Page<Task> findAllByOwnerId(Long ownerId, Pageable pageable);

    Page<Task> findAllByOwnerIdAndInboundDependencyCount(Long ownerId, int inboundDependencyCount, Pageable pageable);

    Page<Task> findAllByOwnerIdAndInboundDependencyCountAndCompletedFalse(Long ownerId, int inboundDependencyCount, Pageable pageable);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("select t from Task t where t.id in :ids")
    List<Task> findAllByIdInWithOptimisticLock(@Param("ids") Collection<Long> ids);

    @Query("""
            SELECT t FROM Task t
            WHERE t.ownerId = :ownerId
              AND (:readyOnly = false OR (t.inboundDependencyCount = 0 AND t.completed = false))
              AND (t.completed = false OR t.temporalConstraint.deadline.date >= :activeDate)
              AND (
                    t.temporalConstraint.deadline.date > :cursorDate
                    OR (t.temporalConstraint.deadline.date = :cursorDate AND t.id > :cursorId)
                  )
            ORDER BY t.temporalConstraint.deadline.date ASC, t.id ASC
            """)
    List<Task> findNextByCursor(@Param("ownerId") Long ownerId,
                                @Param("readyOnly") boolean readyOnly,
                                @Param("cursorDate") LocalDate cursorDate,
                                @Param("cursorId") Long cursorId,
                                @Param("activeDate") LocalDate activeDate,
                                Pageable pageable);

    @Query(value = """
            SELECT t FROM Task t
            WHERE t.ownerId = :ownerId
              AND (t.completed = false OR t.temporalConstraint.deadline.date >= :activeDate)
            ORDER BY t.temporalConstraint.deadline.date ASC, t.id ASC
            """,
            countQuery = """
                    SELECT count(t) FROM Task t
                    WHERE t.ownerId = :ownerId
                      AND (t.completed = false OR t.temporalConstraint.deadline.date >= :activeDate)
                    """)
    Page<Task> findCurrentByOwnerId(@Param("ownerId") Long ownerId,
                                    @Param("activeDate") LocalDate activeDate,
                                    Pageable pageable);

    @Query("""
            SELECT t FROM Task t
            WHERE t.ownerId = :ownerId
              AND t.temporalConstraint.deadline.date = :deadlineDate
            ORDER BY t.id ASC
            """)
    List<Task> findAllByOwnerIdAndDeadlineDate(@Param("ownerId") Long ownerId,
                                               @Param("deadlineDate") LocalDate deadlineDate);

    @Query("""
            SELECT t FROM Task t
            WHERE t.ownerId = :ownerId
              AND t.completed = true
              AND t.temporalConstraint.deadline.date <= :cutoffDate
              AND (
                    t.temporalConstraint.deadline.date < :cursorDate
                    OR (t.temporalConstraint.deadline.date = :cursorDate AND t.id < :cursorId)
                  )
            ORDER BY t.temporalConstraint.deadline.date DESC, t.id DESC
            """)
    /**
     * 커서 기반 완료 아카이브 조회.
     *
     * @param ownerId    회원 ID (파티션 키)
     * @param cutoffDate 포함되는 마감일 상한 (예: now-1@offset)
     * @param cursorDate 커서가 가리키는 마감일. 이 날짜보다 과거 데이터만 조회하며,
     *                   같은 날짜일 때는 cursorId 미만만 조회한다.
     * @param cursorId   커서가 가리키는 작업 ID (마감일 동일 시 tie-breaker)
     * @param pageable   페이지 정보(서비스에서 size+1로 전달)
     */
    List<Task> findCompletedArchiveByCursor(@Param("ownerId") Long ownerId,
                                            @Param("cutoffDate") LocalDate cutoffDate,
                                            @Param("cursorDate") LocalDate cursorDate,
                                            @Param("cursorId") Long cursorId,
                                            Pageable pageable);
}
