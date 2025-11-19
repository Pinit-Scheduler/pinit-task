package me.gg.pinit.pinittask.application.statistics.service;

import me.gg.pinit.pinittask.application.datetime.DateTimeUtils;
import me.gg.pinit.pinittask.domain.schedule.model.TaskType;
import me.gg.pinit.pinittask.domain.statistics.model.Statistics;
import me.gg.pinit.pinittask.domain.statistics.repository.StatisticsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.ZonedDateTime;

@Service
public class StatisticsService {
    private final StatisticsRepository statisticsRepository;
    private final DateTimeUtils dateTimeUtils;

    public StatisticsService(StatisticsRepository statisticsRepository, DateTimeUtils dateTimeUtils) {
        this.statisticsRepository = statisticsRepository;
        this.dateTimeUtils = dateTimeUtils;
    }

    @Transactional(readOnly = true)
    public Statistics getStatistics(Long memberId, ZonedDateTime now) {
        ZonedDateTime startTime = dateTimeUtils.lastMondayStart(now);
        return statisticsRepository.findByMemberIdAndStartOfWeek(memberId, startTime).orElseGet(() -> new Statistics(memberId, startTime));
    }

    @Transactional
    public void removeElapsedTime(Long ownerId, TaskType taskType, Duration duration, ZonedDateTime startTime) {
        ZonedDateTime dateTime = dateTimeUtils.lastMondayStart(startTime);
        Statistics statistics = statisticsRepository.findByMemberIdAndStartOfWeek(ownerId, dateTime).orElseGet(() -> new Statistics(ownerId, dateTime));
        taskType.rollback(statistics, duration);
        statisticsRepository.save(statistics);
    }

    @Transactional
    public void addElapsedTime(Long ownerId, TaskType taskType, Duration duration, ZonedDateTime startTime) {
        ZonedDateTime dateTime = dateTimeUtils.lastMondayStart(startTime);
        Statistics statistics = statisticsRepository.findByMemberIdAndStartOfWeek(ownerId, dateTime).orElseGet(() -> new Statistics(ownerId, dateTime));
        taskType.record(statistics, duration);
        statisticsRepository.save(statistics);
    }
}
/**
 * n주차라고 명명하면 너무 까다로워짐
 * 특정 날짜를 기준으로 하는 것이 좀 더 변하지 않는(안정적인)지시 방식임
 */