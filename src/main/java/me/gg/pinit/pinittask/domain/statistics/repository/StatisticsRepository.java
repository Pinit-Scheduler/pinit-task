package me.gg.pinit.pinittask.domain.statistics.repository;

import me.gg.pinit.pinittask.domain.statistics.model.Statistics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatisticsRepository extends JpaRepository<Statistics, Long> {
}
