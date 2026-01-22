package me.gg.pinit.pinittask.domain.task.repository;

import jakarta.persistence.LockModeType;
import me.gg.pinit.pinittask.domain.task.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
