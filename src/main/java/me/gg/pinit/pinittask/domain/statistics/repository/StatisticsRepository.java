package me.gg.pinit.pinittask.domain.statistics.repository;

import me.gg.pinit.pinittask.domain.statistics.model.Statistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface StatisticsRepository extends JpaRepository<Statistics, Long> {
    @Query("SELECT s FROM Statistics s WHERE s.memberId = :memberId AND s.startOfWeek.dateTime = :startOfWeek AND s.startOfWeek.zoneId = :zoneId")
    Optional<Statistics> findByMemberIdAndStartOfWeek(@Param("memberId") Long memberId, @Param("startOfWeek") LocalDateTime startOfWeek, @Param("zoneId") String zoneId);
}
