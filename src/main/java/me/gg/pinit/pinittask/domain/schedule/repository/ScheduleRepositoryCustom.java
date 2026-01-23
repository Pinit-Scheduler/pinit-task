package me.gg.pinit.pinittask.domain.schedule.repository;

import me.gg.pinit.pinittask.domain.schedule.model.Schedule;

import java.time.Instant;
import java.util.List;

public interface ScheduleRepositoryCustom {
    List<Schedule> findAllByOwnerIdAndDesignatedStartTimeBetween(Long ownerId, Instant start, Instant end);
}
