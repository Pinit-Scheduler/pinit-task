package me.gg.pinit.pinittask.domain.statistics.repository;

import me.gg.pinit.pinittask.domain.statistics.model.Statistics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface StatisticsRepository extends JpaRepository<Statistics, Long> {
    Optional<Statistics> findByMemberIdAndStartOfWeek(Long memberId, ZonedDateTime startOfWeek);
}
