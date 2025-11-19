package me.gg.pinit.pinittask.domain.dependency.repository;

import me.gg.pinit.pinittask.domain.dependency.model.Dependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DependencyRepository extends JpaRepository<Dependency, Long> {
    List<Dependency> findAllByOwnerId(Long ownerId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Dependency d WHERE d.from.id = :scheduleId OR d.to.id = :scheduleId")
    int deleteAllRelatedToSchedule(@Param("scheduleId") Long scheduleId);
}
