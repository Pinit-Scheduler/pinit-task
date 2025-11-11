package me.gg.pinit.pinittask.domain.schedule.repository;

import me.gg.pinit.pinittask.domain.schedule.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
}
