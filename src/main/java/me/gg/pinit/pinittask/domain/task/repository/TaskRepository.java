package me.gg.pinit.pinittask.domain.task.repository;

import jakarta.persistence.LockModeType;
import me.gg.pinit.pinittask.domain.task.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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
              AND (
                    t.temporalConstraint.deadline.dateTime > :cursorTime
                    OR (t.temporalConstraint.deadline.dateTime = :cursorTime AND t.id > :cursorId)
                  )
            ORDER BY t.temporalConstraint.deadline.dateTime ASC, t.id ASC
            """)
    List<Task> findNextByCursor(@Param("ownerId") Long ownerId,
                                @Param("readyOnly") boolean readyOnly,
                                @Param("cursorTime") LocalDateTime cursorTime,
                                @Param("cursorId") Long cursorId,
                                Pageable pageable);
}
