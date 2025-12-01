package me.gg.pinit.pinittask.domain.schedule.repository;

import jakarta.persistence.LockModeType;
import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT s FROM Schedule s WHERE s.id = :scheduleId")
    Optional<Schedule> findByIdForUpdate(Long scheduleId);

    @Query("SELECT s FROM Schedule s WHERE s.ownerId = :ownerId AND s.designatedStartTime.dateTime >= :start AND s.designatedStartTime.dateTime < :end AND s.designatedStartTime.zoneId = :zoneId")
    List<Schedule> findAllByOwnerIdAndDesignatedStartTimeBetween(Long ownerId, LocalDateTime start, LocalDateTime end, String zoneId);
}
