package me.gg.pinit.pinittask.domain.statistics.repository;

import me.gg.pinit.pinittask.domain.statistics.model.Statistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface StatisticsRepository extends JpaRepository<Statistics, Long> {
    @Query("SELECT s FROM Statistics s WHERE s.memberId = :memberId AND s.startOfWeekDate.date = :startOfWeekDate AND s.startOfWeekDate.offsetId = :startOfWeekOffsetId")
    Optional<Statistics> findByMemberIdAndStartOfWeekDate(@Param("memberId") Long memberId, @Param("startOfWeekDate") LocalDate startOfWeekDate, @Param("startOfWeekOffsetId") String startOfWeekOffsetId);
}
