package me.gg.pinit.pinittask.domain.dependency.repository;

import me.gg.pinit.pinittask.domain.dependency.model.Dependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DependencyRepository extends JpaRepository<Dependency, Long>, DependencyRepositoryCustom {
    List<Dependency> findAllByOwnerId(Long ownerId);

    List<Dependency> findAllByFromId(Long fromId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Dependency d WHERE d.fromId = :taskId OR d.toId = :taskId")
    int deleteAllRelatedToTask(@Param("taskId") Long taskId);
}
