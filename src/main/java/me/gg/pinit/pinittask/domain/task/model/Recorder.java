package me.gg.pinit.pinittask.domain.task.model;

import me.gg.pinit.pinittask.domain.statistics.model.Statistics;

import java.time.Duration;

public interface Recorder {
    /**
     * 특정 사용자의 특정 주차 통계에 경과 시간 추가
     * 통계는 반드시 완료된 일정에 대해서만 기록되어야 함
     * 해당 일정이 완료되기 전까지는 해당 일정이 집중 작업인지, 행정 작업인지, 빠른 작업인지 알 수 없기 때문
     *
     * @param statistics 해당 주차 통계
     * @param duration   추가할 경과 시간
     */
    void record(Statistics statistics, Duration duration);

    void rollback(Statistics statistics, Duration duration);
}
